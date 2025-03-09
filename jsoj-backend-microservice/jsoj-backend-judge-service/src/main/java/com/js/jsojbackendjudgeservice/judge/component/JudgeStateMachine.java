package com.js.jsojbackendjudgeservice.judge.component;

import com.js.jsojbackendcommon.common.ErrorCode;
import com.js.jsojbackendcommon.exception.BusinessException;
import com.js.jsojbackendmodel.entity.QuestionSubmit;
import com.js.jsojbackendmodel.enums.QuestionStatusEnum;
import com.js.jsojbackendserviceclient.service.QuestionFeignClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 状态机组件
 * @author sakisaki
 * @date 2025/3/9 15:02
 */
@Component
public class JudgeStateMachine {

    @Resource
    private QuestionFeignClient questionFeignClient;

    /**
     * 状态变更模板方法
     * @param questionSubmitId 题目Id
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     * @param maxRetry
     */
    public void updateStatusWithRetry(Long questionSubmitId, QuestionStatusEnum oldStatus, QuestionStatusEnum newStatus, int maxRetry) throws InterruptedException {
        for (int i = 0; i < maxRetry; i++) {
            QuestionSubmit current = questionFeignClient.getQuestionSubmitById(questionSubmitId);
            if (current.getStatus().equals(oldStatus.getValue())) {
                QuestionSubmit update = new QuestionSubmit();
                update.setId(questionSubmitId);
                update.setStatus(newStatus.getValue());
                if (questionFeignClient.updateQuestionSubmitById(update)) {
                    return;
                }
            }
            // 指数退避更佳
            Thread.sleep(500);
        }
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "状态更新失败");
    }
}
