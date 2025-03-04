package com.js.jsojbackendquestionservice.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.js.jsojbackendcommon.common.ErrorCode;
import com.js.jsojbackendcommon.common.UserContext;
import com.js.jsojbackendcommon.exception.ThrowUtils;
import com.js.jsojbackendcommon.utils.SqlUtils;
import com.js.jsojbackendmodel.constant.CommonConstant;
import com.js.jsojbackendmodel.dto.question.*;
import com.js.jsojbackendmodel.entity.Question;
import com.js.jsojbackendmodel.entity.User;
import com.js.jsojbackendmodel.entity.UserQuestionStatus;
import com.js.jsojbackendmodel.vo.admin.QuestionAdminVO;
import com.js.jsojbackendmodel.vo.user.QuestionUserVO;
import com.js.jsojbackendquestionservice.mapper.QuestionMapper;
import com.js.jsojbackendquestionservice.mapper.UserQuestionStatusMapper;
import com.js.jsojbackendquestionservice.service.QuestionService;
import com.js.jsojbackendquestionservice.utils.QuestionHelper;
import com.js.jsojbackendserviceclient.service.UserFeignClient;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author JianShang
 * @version 1.0.0
 * @description 针对表【question(题目)】的数据库操作Service实现
 * @date 2024-10-16 08:29:29@see ServiceImpl
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
        implements QuestionService {

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private UserQuestionStatusMapper userQuestionStatusMapper;

    // region 增删改相关

    /**
     * 创建题目
     * @param questionAddRequest
     * @return 是否创建的成功
     */
    @Override
    public Boolean addQuestion(QuestionAddRequest questionAddRequest) {
        Question question = new Question();
        BeanUtils.copyProperties(questionAddRequest, question);
        // 校验添加题目的必要参数是否为空
        question.setTags(JSONUtil.toJsonStr(questionAddRequest.getTags()));
        question.setJudgeConfig(JSONUtil.toJsonStr(questionAddRequest.getJudgeConfig()));
        question.setJudgeCase(JSONUtil.toJsonStr(questionAddRequest.getJudgeCase()));
        this.validQuestion(question, true);
        Long userId = UserContext.getUser().getId();
        question.setUserId(userId);
        boolean result = this.save(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return true;
    }

    /**
     * 更新题目
     * @param questionUpdateRequest 题目更新信息
     * @return 是否更新的成功
     */
    @Override
    public Boolean updateQuestion(QuestionUpdateRequest questionUpdateRequest) {
        // 判断是否存在
        Question question = this.getById(questionUpdateRequest.getId());
        ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取需要更新的参数
        Long questionId = questionUpdateRequest.getId();
        String title = questionUpdateRequest.getTitle();
        String content = questionUpdateRequest.getContent();
        List<String> tags = questionUpdateRequest.getTags();
        String answer = questionUpdateRequest.getAnswer();
        List<JudgeCase> judgeCase = questionUpdateRequest.getJudgeCase();
        JudgeConfig judgeConfig = questionUpdateRequest.getJudgeConfig();
        // 构建更新信息
        UpdateWrapper<Question> questionUpdateWrapper = new UpdateWrapper<>();

        questionUpdateWrapper
                .eq(ObjectUtils.isNotEmpty(questionId), "id", questionId)
                .set(StringUtils.isNotBlank(title) && !title.equals(question.getTitle()), "title", title)
                .set(StringUtils.isNotBlank(content) && !content.equals(question.getContent()), "content", content)
                .set(StringUtils.isNotBlank(answer) && !answer.equals(question.getAnswer()), "answer", answer);

        if (CollectionUtils.isNotEmpty(tags)) {
            String tagsJson = JSONUtil.toJsonStr(tags);
            if (!tagsJson.equals(question.getTags())) {
                questionUpdateWrapper.set("tags", tagsJson);
            }
        }
        if (CollectionUtils.isNotEmpty(judgeCase)) {
            String judgeCaseJson = JSONUtil.toJsonStr(judgeCase);
            if (!judgeCaseJson.equals(question.getJudgeCase())) {
                questionUpdateWrapper.set("judgeCase", judgeCaseJson);
            }
        }
        if (Objects.nonNull(judgeConfig)) {
            String judgeConfigJson = JSONUtil.toJsonStr(judgeConfig);
            if (!judgeConfigJson.equals(question.getJudgeConfig())) {
                questionUpdateWrapper.set("judgeConfig", judgeConfigJson);
            }
        }
        boolean result = this.update(question, questionUpdateWrapper);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新失败");
        return true;
    }

    /**
     * 删除题目
     * @param questionDeleteRequest
     * @return 是否删除的成功
     */
    @Override
    @Transactional
    public Boolean deleteQuestion(QuestionDeleteRequest questionDeleteRequest) {
        List<Long> questionIds = questionDeleteRequest.getQuestionIds();
        // 检查 questionIds 中的 ID 是否在数据库中存在
        List<Question> existingQuestions = this.listByIds(questionIds);
        ThrowUtils.throwIf(existingQuestions.size() != questionIds.size(), ErrorCode.NOT_FOUND_ERROR, "部分题目 ID 不存在");
        boolean result = this.removeBatchByIds(questionIds);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "删除失败");
        return true;
    }

    // endregion 增删改结束

    // region 分页获取题目相关

    /**
     * 分页获取题目（用户）
     * @param questionPage 题目分页数据
     * @return QuestionUserVO
     */
    @Override
    public Page<QuestionUserVO> getQuestionVOPage(Page<Question> questionPage) {
        Page<QuestionUserVO> userVOPage = getQuestionVOPageInternal(questionPage, new Function<Question, QuestionUserVO>() {
            @Override
            public QuestionUserVO apply(Question question) {
                return getQuestionUserVO(question);
            }
        });
        return userVOPage;
    }

    /**
     * 分页获取题目（管理员）
     * @param questionPage 题目分页数据
     * @return QuestionAdminVO
     */
    @Override
    public Page<QuestionAdminVO> getQuestionAdminVOPage(Page<Question> questionPage) {
        Page<QuestionAdminVO> adminVOPage = getQuestionVOPageInternal(questionPage, question -> {
            return getQuestionAdminVO(question);
        });
        return adminVOPage;
    }

    /**
     * 公共方法：处理分页查询和用户状态赋值
     *
     * @param questionPage 题目分页数据
     * @param voConverter  VO 转换函数
     * @param <T>          VO 类型
     * @return 分页 VO 数据
     */
    private <T> Page<T> getQuestionVOPageInternal(Page<Question> questionPage, Function<Question, T> voConverter) {
        // 空值检查
        List<Question> questionList = questionPage.getRecords();
        Page<T> voPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        if (CollUtil.isEmpty(questionList)) {
            return voPage;
        }

        // 转换 VO 列表
        List<T> voList = questionList
                .stream()
                .map(voConverter)
                .collect(Collectors.toList());

        // 获取当前登录用户
        User user = UserContext.getUser();
        if (ObjectUtils.isNotEmpty(user)) {
            Long loginUserId = user.getId();

            // 获取题目 ID 列表
            List<Long> questionIds = questionList.stream()
                    .map(Question::getId)
                    .collect(Collectors.toList());

            // 批量查询用户与题目状态
            QueryWrapper<UserQuestionStatus> userQuestionStatusQueryWrapper = new QueryWrapper<>();
            userQuestionStatusQueryWrapper
                    .eq("userId", loginUserId)
                    .in("questionId", questionIds);
            List<UserQuestionStatus> userQuestionStatusList = userQuestionStatusMapper.selectList(userQuestionStatusQueryWrapper);

            // 将状态列表转换为 Map
            Map<Long, Integer> statusMap = userQuestionStatusList.stream()
                    .collect(Collectors.toMap(
                            UserQuestionStatus::getQuestionId,
                            UserQuestionStatus::getQuestionStatus
                    ));

            // 赋值状态到题目列表
            assignStatusToQuestions(questionList, statusMap);
        }

        // 设置 VO 列表到分页对象
        voPage.setRecords(voList);
        return voPage;
    }

    // endregion 分页获取题目结束

    // region 根据Id单独查询相关

    /**
     * 根据题目Id查询特定题目（用户）
     * @param question
     * @return QuestionUserVO
     */
    @Override
    public QuestionUserVO getQuestionUserVOById(Question question) {
        return getQuestionUserVO(question);
    }

    /**
     * 根据题目Id查询特定题目（管理员）
     * @param question
     * @return QuestionAdminVO
     */
    @Override
    public QuestionAdminVO getQuestionAdminVOById(Question question) {
        return getQuestionAdminVO(question);
    }

    @NotNull
    private static QuestionUserVO getQuestionUserVO(Question question) {
        QuestionUserVO questionUserVO = new QuestionUserVO();
        QuestionHelper.convertToVO(question, questionUserVO);
        return questionUserVO;
    }

    @NotNull
    private static QuestionAdminVO getQuestionAdminVO(Question question) {
        QuestionAdminVO questionAdminVO = new QuestionAdminVO();
        QuestionHelper.convertToVO(question, questionAdminVO);
        return questionAdminVO;
    }

    // endregion

    /**
     * 校验题目是否合法
     *
     * @param question
     * @param add
     * @see Question
     */
    private void validQuestion(Question question, boolean add) {
        ThrowUtils.throwIf(question == null, ErrorCode.PARAMS_ERROR);
        String title = question.getTitle();
        String content = question.getContent();
        String tags = question.getTags();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title, content, tags), ErrorCode.PARAMS_ERROR, "缺少必填参数");
        }
        // 有参数则校验
        ThrowUtils.throwIf(StringUtils.isNotBlank(title) && title.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");
        ThrowUtils.throwIf(StringUtils.isNotBlank(content) && content.length() > 8192, ErrorCode.PARAMS_ERROR, "内容过长");
    }

    /**
     * 获取查询包装类
     *
     * @param questionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        Long id = questionQueryRequest.getId();
        String title = questionQueryRequest.getTitle();
        String content = questionQueryRequest.getContent();
        Long userId = questionQueryRequest.getUserId();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();
        // 处理标签
        List<String> tags = questionQueryRequest.getTags();
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 匹配条件
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.ne(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        queryWrapper.eq("isDelete", false);
        return queryWrapper;
    }


    // region

    /**
     * 将状态赋值到题目列表中
     *
     * @param questions 题目列表
     * @param statusMap 状态映射表（题目 ID -> 状态）
     */
    private void assignStatusToQuestions(List<Question> questions, Map<Long, Integer> statusMap) {
        if (CollectionUtils.isEmpty(questions) || statusMap == null) {
            return;
        }

        for (Question question : questions) {
            Long questionId = question.getId();
            // 如果 statusMap 中存在该题目的状态，则赋值；否则保持默认状态 0
            // 确保未匹配的题目状态为 0
            question.setUserStatus(statusMap.getOrDefault(questionId, 0));
        }
    }

    // endregion
}
