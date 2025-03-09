package com.js.jsojbackendjudgeservice.judge;

import cn.hutool.json.JSONUtil;
import com.js.jsojbackendcommon.common.ErrorCode;
import com.js.jsojbackendcommon.exception.BusinessException;
import com.js.jsojbackendjudgeservice.judge.codesandbox.CodeSandBox;
import com.js.jsojbackendjudgeservice.judge.codesandbox.CodeSandBoxFactory;
import com.js.jsojbackendjudgeservice.judge.codesandbox.CodeSandBoxProxy;
import com.js.jsojbackendjudgeservice.judge.component.JudgeStateMachine;
import com.js.jsojbackendjudgeservice.judge.strategy.JudgeContext;
import com.js.jsojbackendmodel.codesandbox.ExecuteCodeRequest;
import com.js.jsojbackendmodel.codesandbox.ExecuteCodeResponse;
import com.js.jsojbackendmodel.codesandbox.JudgeInfo;
import com.js.jsojbackendmodel.dto.question.JudgeCase;
import com.js.jsojbackendmodel.entity.Question;
import com.js.jsojbackendmodel.entity.QuestionSubmit;
import com.js.jsojbackendmodel.enums.QuestionStatusEnum;
import com.js.jsojbackendserviceclient.service.QuestionFeignClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 *
 * @author sakisaki
 * @date 2025/3/9 20:11
 */
@Service
public class JudgeServiceImpl implements JudgeService {

    // 从配置文件中获取代码沙箱的类型，默认值为 "example"
    @Value("${codesandbox.type:example}")
    private String type;

    @Resource
    private QuestionFeignClient questionFeignClient;

    @Resource
    private JudgeManager judgeManager;

    @Resource
    private JudgeStateMachine judgeStateMachine;

    /**
     * 执行判题操作
     * @param questionSubmitId 题目提交记录的 ID
     * @return 判题完成后的题目提交记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionSubmit doJudge(long questionSubmitId) {
        // 1. 获取提交记录
        QuestionSubmit questionSubmit = questionFeignClient.getQuestionSubmitById(questionSubmitId);

        // 2. 获取题目详情
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionFeignClient.getQuestionById(questionId);

        // 3. 状态校验
        validateSubmission(questionSubmit, questionId);

        // 4. 更新为判题中状态（使用状态机）
        try {
            judgeStateMachine.updateStatusWithRetry(questionSubmitId,
                    QuestionStatusEnum.WAITING,
                    QuestionStatusEnum.RUNNING,
                    3);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {
            // 5. 构建沙箱请求（从题目中获取输入用例）
            List<JudgeCase> judgeCases = JSONUtil.toList(question.getJudgeCase(), JudgeCase.class);
            List<String> inputList = judgeCases.stream()
                    .map(JudgeCase::getInput)
                    .collect(Collectors.toList());

            ExecuteCodeRequest request = ExecuteCodeRequest.builder()
                    .code(questionSubmit.getCode())
                    .language(questionSubmit.getLanguage())
                    .inputList(inputList)
                    .build();

            // 6. 执行沙箱判题
            ExecuteCodeResponse response = executeWithTimeout(request);

            // 7. 构建判题上下文
            JudgeContext context = JudgeContext.builder()
                    .judgeInfo(response.getJudgeInfo())
                    // 使用本地计算的 inputList
                    .inputList(inputList)
                    .outputList(response.getOutputList())
                    .judgeCaseList(judgeCases)
                    .question(question)
                    .questionSubmit(questionSubmit)
                    .build();

            // 8. 策略判题
            JudgeInfo judgeInfo = judgeManager.doJudge(context);

            // 9. 保存结果
            saveJudgeResult(questionSubmitId, judgeInfo);
            return questionFeignClient.getQuestionSubmitById(questionSubmitId);
        } catch (TimeoutException e) {
            handleTimeout(questionSubmitId);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "判题超时");
        } catch (Exception e) {
            handleFailure(questionSubmitId);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "判题系统异常");
        }
    }

    /**
     * 校验提交信息合法性
     * @param submit 题目提交记录
     * @param questionId 题目 ID
     */
    private void validateSubmission(QuestionSubmit submit, Long questionId) {
        if (submit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
        }
        Question question = questionFeignClient.getQuestionById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        if (!submit.getStatus().equals(QuestionStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题中");
        }
    }

    /**
     * 带超时时间执行代码沙箱
     * @param request 执行代码的请求
     * @return 执行代码的响应
     * @throws TimeoutException 若执行超时抛出此异常
     */
    private ExecuteCodeResponse executeWithTimeout(ExecuteCodeRequest request) throws TimeoutException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<ExecuteCodeResponse> future = null;
        try {
            future = executor.submit(() -> {
                // 使用工厂模式创建代码沙箱实例，并使用代理包装
                CodeSandBox sandbox = new CodeSandBoxProxy(CodeSandBoxFactory.newInstance(type));
                return sandbox.executeCode(request);
            });
            // 可配置化，设置超时时间为 10 秒
            return future.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            // 超时则取消任务
            future.cancel(true);
            throw e;
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 关闭线程池
            executor.shutdownNow();
        }
    }

    /**
     * 持久化判题结果
     * @param submitId 题目提交记录的 ID
     * @param judgeInfo 判题信息
     */
    private void saveJudgeResult(Long submitId, JudgeInfo judgeInfo) {
        QuestionSubmit update = new QuestionSubmit();
        update.setId(submitId);
        update.setStatus(QuestionStatusEnum.SUCCEED.getValue());
        update.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        if (!questionFeignClient.updateQuestionSubmitById(update)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "结果保存失败");
        }
    }

    /**
     * 处理判题超时情况
     * @param submitId 题目提交记录的 ID
     */
    private void handleTimeout(Long submitId) {
        QuestionSubmit update = new QuestionSubmit();
        update.setId(submitId);
        update.setStatus(QuestionStatusEnum.FAILED.getValue());
        update.setJudgeInfo("{\"message\":\"执行超时\"}");
        questionFeignClient.updateQuestionSubmitById(update);
    }

    /**
     * 处理判题失败情况
     * @param submitId 题目提交记录的 ID
     */
    private void handleFailure(Long submitId) {
        QuestionSubmit update = new QuestionSubmit();
        update.setId(submitId);
        update.setStatus(QuestionStatusEnum.FAILED.getValue());
        update.setJudgeInfo("{\"message\":\"系统内部错误\"}");
        questionFeignClient.updateQuestionSubmitById(update);
    }
}