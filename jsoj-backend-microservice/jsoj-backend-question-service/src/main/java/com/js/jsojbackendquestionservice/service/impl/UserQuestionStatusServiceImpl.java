package com.js.jsojbackendquestionservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.js.jsojbackendmodel.constant.CommonConstant;
import com.js.jsojbackendmodel.dto.userQuestionStatus.UserQuestionStatusQueryRequest;
import com.js.jsojbackendmodel.entity.UserQuestionStatus;
import com.js.jsojbackendmodel.vo.user.UserQuestionStatusVO;
import com.js.jsojbackendquestionservice.mapper.UserQuestionStatusMapper;
import com.js.jsojbackendquestionservice.service.UserQuestionStatusService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author jianshang
 * @description 针对表【user_question_status(用户-题目状态表)】的数据库操作Service实现
 * @createDate 2025-03-03 20:31:09
 */
@Service
public class UserQuestionStatusServiceImpl extends ServiceImpl<UserQuestionStatusMapper, UserQuestionStatus>
        implements UserQuestionStatusService {

    /**
     * 构建查询条件
     * @param userQuestionStatusQueryRequest
     * @return Wrapper
     */
    @Override
    public Wrapper<UserQuestionStatus> getQueryWrapper(UserQuestionStatusQueryRequest userQuestionStatusQueryRequest) {
        QueryWrapper<UserQuestionStatus> queryWrapper = new QueryWrapper<>();
        if (userQuestionStatusQueryRequest == null) {
            return queryWrapper;
        }
        Long userId = userQuestionStatusQueryRequest.getUserId();
        Long questionId = userQuestionStatusQueryRequest.getQuestionId();
        String questionTitle = userQuestionStatusQueryRequest.getQuestionTitle();
        Integer questionStatus = userQuestionStatusQueryRequest.getQuestionStatus();
        Date createTime = userQuestionStatusQueryRequest.getCreateTime();
        String sortField = userQuestionStatusQueryRequest.getSortField();
        String sortOrder = userQuestionStatusQueryRequest.getSortOrder();

        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId)
                .eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId)
                .eq(ObjectUtils.isNotEmpty(questionStatus), "questionStatus", questionStatus)
                .eq(StringUtils.isNotBlank(questionTitle), "questionTitle", questionTitle);

        // 处理 createTime：只匹配年月日
        if (createTime != null) {
            queryWrapper.apply("DATE(`createTime`) = DATE({0})", createTime);
        }

        // 处理排序
        if (StringUtils.isNotBlank(sortField)) {
            if (CommonConstant.SORT_ORDER_ASC.equalsIgnoreCase(sortOrder)) {
                // 升序
                queryWrapper.orderByAsc(sortField);
            } else if (CommonConstant.SORT_ORDER_DESC.equalsIgnoreCase(sortOrder)) {
                // 降序
                queryWrapper.orderByDesc(sortField);
            } else {
                // 默认升序
                queryWrapper.orderByAsc(sortField);
            }
        }
        return queryWrapper;
    }

    @Override
    public Page<UserQuestionStatusVO> getUserQuestionVOPage(Page<UserQuestionStatus> userQuestionStatusPage) {
        List<UserQuestionStatus> records = userQuestionStatusPage.getRecords();
        ArrayList<UserQuestionStatusVO> userQuestionStatusVOList = new ArrayList<>();
        for (UserQuestionStatus userQuestionStatus : records) {
            UserQuestionStatusVO userQuestionStatusVO = new UserQuestionStatusVO();
            BeanUtil.copyProperties(userQuestionStatus, userQuestionStatusVO);
            userQuestionStatusVOList.add(userQuestionStatusVO);
        }
        // 构建分页结果
        Page<UserQuestionStatusVO> voPage = new Page<>(
                userQuestionStatusPage.getCurrent(),
                userQuestionStatusPage.getSize(),
                userQuestionStatusPage.getTotal()
        );
        voPage.setRecords(userQuestionStatusVOList);
        return voPage;
    }
}




