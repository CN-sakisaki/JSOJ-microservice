package com.js.jsojbackendquestionservice.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.js.jsojbackendmodel.dto.question.QuestionAddRequest;
import com.js.jsojbackendmodel.dto.question.QuestionDeleteRequest;
import com.js.jsojbackendmodel.dto.question.QuestionQueryRequest;
import com.js.jsojbackendmodel.dto.question.QuestionUpdateRequest;
import com.js.jsojbackendmodel.entity.Question;
import com.js.jsojbackendmodel.vo.admin.QuestionAdminVO;
import com.js.jsojbackendmodel.vo.user.QuestionUserVO;

/**
 * @author jsnlg
 * @description 针对表【question(题目)】的数据库操作Service
 * @createDate 2024-10-15 15:09:55
 */
public interface QuestionService extends IService<Question> {

    /**
     * 创建题目
     * @param questionAddRequest 创建题目的信息
     * @return 是否创建的成功
     */
    Boolean addQuestion(QuestionAddRequest questionAddRequest);

    /**
     * 更新题目
     * @param questionUpdateRequest 题目更新信息
     * @return 是否更新的成功
     */
    Boolean updateQuestion(QuestionUpdateRequest questionUpdateRequest);

    /**
     * 删除题目
     * @param questionDeleteRequest
     * @return 是否删除的成功
     */
    Boolean deleteQuestion(QuestionDeleteRequest questionDeleteRequest);

    /**
     * 分页获取题目（用户）
     *
     * @param questionPage
     * @return
     */
    Page<QuestionUserVO> getQuestionVOPage(Page<Question> questionPage);

    /**
     * 分页获取题目（管理员）
     * @param questionPage
     * @return
     */
    Page<QuestionAdminVO> getQuestionAdminVOPage(Page<Question> questionPage);

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest
     * @return
     */
    QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest);

    /**
     * 根据ID获取题目封装（用户）
     *
     * @param question
     * @return QuestionUserVO
     */
    QuestionUserVO getQuestionUserVOById(Question question);

    /**
     * 根据ID获取题目封装（管理员）
     *
     * @param question
     * @return QuestionAdminVO
     */
    QuestionAdminVO getQuestionAdminVOById(Question question);


}
