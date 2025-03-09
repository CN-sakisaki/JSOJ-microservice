package com.js.jsojbackendquestionservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.js.jsojbackendcommon.common.ErrorCode;
import com.js.jsojbackendcommon.exception.ThrowUtils;
import com.js.jsojbackendcommon.utils.SqlUtils;
import com.js.jsojbackendmodel.constant.CommonConstant;
import com.js.jsojbackendmodel.dto.questionsubmit.QuestionSubmitAddRequest;
import com.js.jsojbackendmodel.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.js.jsojbackendmodel.entity.Question;
import com.js.jsojbackendmodel.entity.QuestionSubmit;
import com.js.jsojbackendmodel.entity.User;
import com.js.jsojbackendmodel.enums.QuestionStatusEnum;
import com.js.jsojbackendmodel.enums.QuestionSubmitLanguageEnum;
import com.js.jsojbackendmodel.vo.user.QuestionSubmitVO;
import com.js.jsojbackendquestionservice.mapper.QuestionSubmitMapper;
import com.js.jsojbackendquestionservice.rabbitmq.QuestionSubmitMessageProducer;
import com.js.jsojbackendquestionservice.service.QuestionService;
import com.js.jsojbackendquestionservice.service.QuestionSubmitService;
import com.js.jsojbackendserviceclient.service.JudgeFeignClient;
import com.js.jsojbackendserviceclient.service.UserFeignClient;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author jsnlg
 * @description 针对表【question_submit(题目提交表)】的数据库操作Service实现
 * @createDate 2024-10-15 15:12:13
 */
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
        implements QuestionSubmitService {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    @Lazy
    private JudgeFeignClient judgeFeignClient;

    @Resource
    private QuestionSubmitMessageProducer questionSubmitMessageProducer;

    private static final String CODE_EXCHANGE = "code_exchange";
    private static final String ROUTING_KEY = "routingKey";

    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return long
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        // 校验
        validateLanguage(questionSubmitAddRequest.getLanguage());
        validateQuestion(questionSubmitAddRequest.getQuestionId());

        // 保存提交记录
        QuestionSubmit questionSubmit = buildQuestionSubmit(questionSubmitAddRequest, loginUser);
        boolean save = this.save(questionSubmit);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "提交题目失败");

        // 发送消息
        questionSubmitMessageProducer.sendMessage(CODE_EXCHANGE, ROUTING_KEY, String.valueOf(questionSubmit.getId()));
        return questionSubmit.getId();
    }

    /**
     * 校验语言合法性
     * @param language 语言类型
     */
    private void validateLanguage(String language) {
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        ThrowUtils.throwIf(languageEnum == null, ErrorCode.PARAMS_ERROR, "编程语言错误");
    }

    /**
     * 校验题目是否存在
     * @param questionId 题目Id
     */
    private void validateQuestion(Long questionId) {
        Question question = questionService.getById(questionId);
        ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR);
    }

    /**
     * 构建题目提交实体类
     * @param questionSubmitAddRequest 构建题目提交信息请求
     * @param loginUser 当前线程用户
     * @return 提交信息QuestionSubmit
     */
    private QuestionSubmit buildQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(loginUser.getId());
        questionSubmit.setQuestionId(questionSubmitAddRequest.getQuestionId());
        questionSubmit.setCode(questionSubmitAddRequest.getCode());
        questionSubmit.setLanguage(questionSubmitAddRequest.getLanguage());
        questionSubmit.setStatus(QuestionStatusEnum.WAITING.getValue());
        questionSubmit.setJudgeInfo("{}");
        return questionSubmit;
    }

    /**
     * 获取查询包装类（用户根据哪些字段查询，根据前端传来的请求对象）
     *
     * @param questionSubmitQueryRequest
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        if (questionSubmitQueryRequest == null) {
            return new QueryWrapper<>();
        }
        Long questionId = questionSubmitQueryRequest.getQuestionId();
        String language = questionSubmitQueryRequest.getLanguage();
        Integer status = questionSubmitQueryRequest.getStatus();
        Long userId = questionSubmitQueryRequest.getUserId();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();

        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();

        // 拼接查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(language), "language", language);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(QuestionStatusEnum.getEnumByValue(status) != null, "status", status);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

    /**
     * 获取查询封装类（单个）
     *
     * @param questionSubmit
     * @param loginUser
     */
    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
        // 脱敏：仅本人和管理员能看见自己（提交 userId 和登录用户 id 不同）提交的代码
        long userId = loginUser.getId();
        // 处理脱敏
        if (userId != questionSubmit.getUserId() && !userFeignClient.isAdmin(loginUser)) {
            questionSubmitVO.setCode(null);
        }
        return questionSubmitVO;
    }

    /**
     * 获取查询脱敏信息
     *
     * @param questionSubmitPage 题目提交分页
     * @param loginUser          直接获取到用户信息，减少查询数据库
     */
    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser) {
        // 1.获取所有提交记录
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        if (CollectionUtils.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }
        // // 2.根据 用户id 过滤
        // Set<Long> userIdSet = questionSubmitList.stream().map(QuestionSubmit::getUserId).collect(Collectors.toSet());
        // /*
        // 3.键为 userId, 值是一个包含对于用户信息的列表 List<User>
        // 即使 userIdSet 中没有重复的用户ID，如果 userService 返回的用户列表中存在多个具有相同ID的用户对象，那么这些用户对象将会被分组到同一个键下。
        // 结果将是 userIdUserListMap 中的某个键对应一个包含多个相同ID的用户对象的列表。
        //  */
        // Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        //
        // List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream().map(questionSubmit -> {
        //     QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
        //     Long userId = questionSubmit.getUserId();
        //     User user = null;
        //     if (userIdUserListMap.containsKey(userId)) {
        //         user = userIdUserListMap.get(userId).get(0);
        //     }
        //     questionSubmitVO.setUserVO(userService.getUserVO(user));
        //     return questionSubmitVO;
        // }).collect(Collectors.toList());

        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream()
                .map(questionSubmit -> getQuestionSubmitVO(questionSubmit, loginUser))
                // .map(new Function<QuestionSubmit, QuestionSubmitVO>() {
                //     @Override
                //     public QuestionSubmitVO apply(QuestionSubmit questionSubmit) {
                //         return getQuestionSubmitVO(questionSubmit, loginUser);
                //     }
                // })
                .collect(Collectors.toList());
        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;
    }
}
