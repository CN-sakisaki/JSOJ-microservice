package com.js.jsojbackendjudgeservice.judge;

import com.js.jsojbackendjudgeservice.judge.strategy.JudgeContext;
import com.js.jsojbackendjudgeservice.judge.strategy.JudgeStrategy;
import com.js.jsojbackendjudgeservice.judge.strategy.impl.JavaLanguageJudgeStrategy;
import com.js.jsojbackendmodel.codesandbox.JudgeInfo;
import com.js.jsojbackendmodel.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

/**
 * 判题管理（简化调用）
 *
 * @author JianShang
 * @version 1.0.0
 * @time 2024-10-24 04:48:21
 */
@Service
public class JudgeManager {

    /**
     * 执行判题
     *
     * @param judgeContext
     * @return {@link JudgeInfo }
     */
    public JudgeInfo doJudge(JudgeContext judgeContext) {
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();
        if ("java".equals(language)) {
            JudgeStrategy judgeStrategy = new JavaLanguageJudgeStrategy();
            return judgeStrategy.doJudge(judgeContext);
        } else {
            JudgeStrategy judgeStrategy = new JavaLanguageJudgeStrategy();
            return judgeStrategy.doJudge(judgeContext);
        }
    }
}
