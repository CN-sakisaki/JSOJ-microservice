package com.js.jsojbackendquestionservice.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.js.jsojbackendmodel.dto.userQuestionStatus.UserQuestionStatusQueryRequest;
import com.js.jsojbackendmodel.entity.UserQuestionStatus;
import com.js.jsojbackendmodel.vo.user.UserQuestionStatusVO;


/**
 * @author jianshang
 * @description 针对表【user_question_status(用户-题目状态表)】的数据库操作Service
 * @createDate 2025-03-03 20:31:09
 */
public interface UserQuestionStatusService extends IService<UserQuestionStatus> {

    /**
     * 构建查询条件
     * @param userQuestionStatusQueryRequest
     * @return Wrapper
     */
    Wrapper<UserQuestionStatus> getQueryWrapper(UserQuestionStatusQueryRequest userQuestionStatusQueryRequest);

    Page<UserQuestionStatusVO> getUserQuestionVOPage(Page<UserQuestionStatus> userQuestionStatusPage);
}
