package com.js.jsojbackendserviceclient.service;

import com.js.jsojbackendmodel.entity.Question;
import com.js.jsojbackendmodel.entity.QuestionSubmit;
import com.js.jsojbackendserviceclient.component.QuestionFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 题目内部服务接口
 * @author sakisaki
 * @date 2025/2/22 15:05
 */
@FeignClient(name = "jsoj-backend-question-service", path = "/api/question/inner", fallback = QuestionFeignClientFallback.class)
public interface QuestionFeignClient {

    /**
     * 根据题目id获取信息
     * @param questionId 题目Id
     * @return Question
     */
    @GetMapping("/get/id")
    Question getQuestionById(@RequestParam("questionId") Long questionId);

    /**
     * 根据题目提交id获取记录
     * @param questionSubmitId 提交记录Id
     * @return QuestionSubmit
     */
    @GetMapping("/question_submit/get/id")
    QuestionSubmit getQuestionSubmitById(@RequestParam("questionSubmitId") Long questionSubmitId);

    /**
     * 更加题目提交记录
     * @param questionSubmit 提交信息
     * @return boolean
     */
    @PostMapping("/question_submit/update")
    boolean updateQuestionSubmitById(@RequestBody QuestionSubmit questionSubmit);
}
