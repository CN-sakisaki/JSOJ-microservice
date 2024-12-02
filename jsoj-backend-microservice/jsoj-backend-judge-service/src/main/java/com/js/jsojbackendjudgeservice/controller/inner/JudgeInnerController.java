package com.js.jsojbackendjudgeservice.controller.inner;

import com.js.jsojbackendjudgeservice.judge.JudgeService;
import com.js.jsojbackendmodel.entity.QuestionSubmit;
import com.js.jsojbackendserviceclient.service.JudgeFeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author JianShang
 * @version 1.0.0
 * @time 2024-11-04 03:02:39
 */
@RestController
@RequestMapping("/inner")
public class JudgeInnerController implements JudgeFeignClient {

    @Resource
    private JudgeService judgeService;

    @Override
    @GetMapping("/do")
    public QuestionSubmit doJudge(long questionSubmitId) {
        return judgeService.doJudge(questionSubmitId);
    }
}
