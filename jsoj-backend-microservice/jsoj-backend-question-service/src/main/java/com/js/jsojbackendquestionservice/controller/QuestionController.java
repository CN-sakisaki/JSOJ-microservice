package com.js.jsojbackendquestionservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.js.jsojbackendcommon.annotation.AuthCheck;
import com.js.jsojbackendcommon.annotation.ExcludeMethod;
import com.js.jsojbackendcommon.common.BaseResponse;
import com.js.jsojbackendcommon.common.ErrorCode;
import com.js.jsojbackendcommon.common.ResultUtils;
import com.js.jsojbackendcommon.exception.ThrowUtils;
import com.js.jsojbackendmodel.constant.UserConstant;
import com.js.jsojbackendmodel.dto.question.QuestionAddRequest;
import com.js.jsojbackendmodel.dto.question.QuestionDeleteRequest;
import com.js.jsojbackendmodel.dto.question.QuestionQueryRequest;
import com.js.jsojbackendmodel.dto.question.QuestionUpdateRequest;
import com.js.jsojbackendmodel.dto.userQuestionStatus.UserQuestionStatusQueryRequest;
import com.js.jsojbackendmodel.entity.Question;
import com.js.jsojbackendmodel.entity.UserQuestionStatus;
import com.js.jsojbackendmodel.vo.admin.QuestionAdminVO;
import com.js.jsojbackendmodel.vo.user.QuestionUserVO;
import com.js.jsojbackendmodel.vo.user.UserQuestionStatusVO;
import com.js.jsojbackendquestionservice.service.QuestionService;
import com.js.jsojbackendquestionservice.service.QuestionSubmitService;
import com.js.jsojbackendquestionservice.service.UserQuestionStatusService;
import com.js.jsojbackendserviceclient.service.UserFeignClient;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianShang
 * @version 1.0.0
 * @description 题目接口
 * @date 2024-10-15 05:53:33
 */
@RestController
@RequestMapping("/")
@Slf4j
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserQuestionStatusService userQuestionStatusService;

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private QuestionSubmitService questionSubmitService;

    // region 增删改题目

    /**
     * 创建题目（管理员）
     *
     * @param questionAddRequest 题目创建信息
     * @return 创建题目的提示
     */
    @PostMapping("/add")
    @Operation(summary = "创建题目（管理员）")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> addQuestion(@RequestBody QuestionAddRequest questionAddRequest) {
        ThrowUtils.throwIf(questionAddRequest == null, ErrorCode.PARAMS_ERROR);
        Boolean result = questionService.addQuestion(questionAddRequest);
        return ResultUtils.success(result);
    }

    /**
     * 更新（仅管理员）
     *
     * @param questionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @Operation(summary = "更新（仅管理员）")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest) {
        ThrowUtils.throwIf(questionUpdateRequest == null || questionUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Boolean result = questionService.updateQuestion(questionUpdateRequest);
        return ResultUtils.success(result);
    }

    /**
     * 删除（仅管理员）
     *
     * @param questionDeleteRequest
     * @return
     */
    @PostMapping("/delete")
    @Operation(summary = "删除（仅管理员）")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteQuestion(@RequestBody QuestionDeleteRequest questionDeleteRequest) {
        ThrowUtils.throwIf(questionDeleteRequest == null, ErrorCode.PARAMS_ERROR);
        Boolean result = questionService.deleteQuestion(questionDeleteRequest);
        return ResultUtils.success(result);
    }

    // endregion

    // region 根据Id获取题目相关接口

    /**
     * 根据 id 获取 (用户)
     *
     * @param id 题目Id
     * @return 脱敏后的题目
     */
    @GetMapping("/get/user/vo")
    @ExcludeMethod
    @Operation(summary = "根据 id 获取 (用户)")
    public BaseResponse<QuestionUserVO> getQuestionUserVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Question question = questionService.getById(id);
        ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR);
        QuestionUserVO questionUserVO = questionService.getQuestionUserVOById(question);
        return ResultUtils.success(questionUserVO);
    }

    /**
     * 根据 id 获取 (管理员)
     *
     * @param id 题目Id
     * @return 脱敏后的题目
     */
    @GetMapping("/get/admin/vo")
    @Operation(summary = "根据 id 获取 (管理员)")
    public BaseResponse<QuestionAdminVO> getQuestionAdminVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Question question = questionService.getById(id);
        ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR);
        QuestionAdminVO questionAdminVO = questionService.getQuestionAdminVOById(question);
        return ResultUtils.success(questionAdminVO);
    }

    // endregion

    // region 分页获取题目相关接口

    /**
     * 分页获取封装列表（用户）
     *
     * @param questionQueryRequest 分页查询封装类
     * @return 脱敏后的所有题目
     */
    @PostMapping("/list/page/user")
    @Operation(summary = "分页获取封装列表（用户）")
    public BaseResponse<Page<QuestionUserVO>> listQuestionUserVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Question> questionPage = questionService.page(new Page<>(current, size), questionService.getQueryWrapper(questionQueryRequest));
        Page<QuestionUserVO> questionVOPage = questionService.getQuestionVOPage(questionPage);
        return ResultUtils.success(questionVOPage);
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param questionQueryRequest 分页查询封装类
     * @return 题目QuestionAdminVO
     */
    @PostMapping("/list/page/admin")
    @Operation(summary = "分页获取列表（仅管理员）")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionAdminVO>> listQuestionAdminVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        Page<Question> questionPage = questionService.page(new Page<>(current, size), questionService.getQueryWrapper(questionQueryRequest));
        Page<QuestionAdminVO> questionAdminVOPage = questionService.getQuestionAdminVOPage(questionPage);
        return ResultUtils.success(questionAdminVOPage);
    }

    // endregion

    // region 题目与用户状态相关

    /**
     * 用户与题目的状态分页获取
     * @param userQuestionStatusQueryRequest 分页查询封装类
     * @return 用户与题目的状态封装类 UserQuestionStatusVO
     */
    @PostMapping("/list/page/userQuestionStatus")
    @AuthCheck
    public BaseResponse<Page<UserQuestionStatusVO>> listUserQuestionStatusVOByPage(@RequestBody UserQuestionStatusQueryRequest userQuestionStatusQueryRequest) {
        long current = userQuestionStatusQueryRequest.getCurrent();
        long size = userQuestionStatusQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 执行分页查询
        Page<UserQuestionStatus> userQuestionStatusPage = userQuestionStatusService.page(new Page<>(current, size), userQuestionStatusService.getQueryWrapper(userQuestionStatusQueryRequest));
        Page<UserQuestionStatusVO> userQuestionStatusVOPage = userQuestionStatusService.getUserQuestionVOPage(userQuestionStatusPage);
        return ResultUtils.success(userQuestionStatusVOPage);
    }

    // endregion

    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param request
     * @return 提交记录的 id
     */
    // @PostMapping("/question_submit/do")
    // public BaseResponse<Long> doSubmitQuestion(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest,
    //                                            HttpServletRequest request) {
    //     if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
    //         throw new BusinessException(ErrorCode.PARAMS_ERROR);
    //     }
    //     // 登录
    //     final User loginUser = userFeignClient.getLoginUser(request);
    //     long questionSubmitId = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, loginUser);
    //     return ResultUtils.success(questionSubmitId);
    // }

    /**
     * 分页获取题目提交列表（除了管理员外，其他普通用户只能看到非答案、提交的代码等公开信息）
     *
     * @param questionSubmitQueryRequest
     * @param request
     * @return
     */
    // @PostMapping("/question_submit/list/page")
    // public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
    //                                                                      HttpServletRequest request) {
    //     long current = questionSubmitQueryRequest.getCurrent();
    //     long pageSize = questionSubmitQueryRequest.getPageSize();
    //
    //     // 从数据库中查询到原始的题目提交信息
    //     Page<QuestionSubmit> questionSubmitPage = questionSubmitService.page(new Page<>(current, pageSize), questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
    //     final User loginUer = userFeignClient.getLoginUser(request);
    //     // 返回脱敏信息
    //     Page<QuestionSubmitVO> questionSubmitVOPage = questionSubmitService.getQuestionSubmitVOPage(questionSubmitPage, loginUer);
    //     return ResultUtils.success(questionSubmitVOPage);
    // }
}
