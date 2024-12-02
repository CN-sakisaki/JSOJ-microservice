package com.js.jsojbackendserviceclient.service;

import com.js.jsojbackendmodel.entity.QuestionSubmit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 判题服务
 *
 * @author JianShang
 * @version 1.0.0
 * @time 2024-10-24 02:44:33
 */
@FeignClient(name = "jsoj-backend-judge-service", path = "/api/judge/inner")
public interface JudgeFeignClient {

    /**
     * 判题
     *
     * @param questionSubmitId 题目提交的id
     * @return {@link QuestionSubmit }
     */
    @GetMapping("/do")
    QuestionSubmit doJudge(@RequestParam("questionSubmitId") long questionSubmitId);
}
