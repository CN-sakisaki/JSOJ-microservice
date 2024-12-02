package com.js.jsojbackendjudgeservice.judge.strategy;


import com.js.jsojbackendmodel.codesandbox.JudgeInfo;
import com.js.jsojbackendmodel.dto.question.JudgeCase;
import com.js.jsojbackendmodel.entity.Question;
import com.js.jsojbackendmodel.entity.QuestionSubmit;
import lombok.Data;

import java.util.List;

/**
 * 上下文（用于定义在策略中传递的参数）
 *
 * @author JianShang
 * @version 1.0.0
 * @time 2024-10-24 04:11:14
 */
@Data
public class JudgeContext {
    private JudgeInfo judgeInfo;

    private List<String> inputList;

    private List<String> outputList;

    private List<JudgeCase> judgeCaseList;

    private Question question;

    private QuestionSubmit questionSubmit;
}
