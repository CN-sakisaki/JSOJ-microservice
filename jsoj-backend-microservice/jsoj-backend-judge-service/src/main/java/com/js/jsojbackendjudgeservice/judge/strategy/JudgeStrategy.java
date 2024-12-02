package com.js.jsojbackendjudgeservice.judge.strategy;


import com.js.jsojbackendmodel.codesandbox.JudgeInfo;

/**
 * 判题策略
 *
 * @author JianShang
 * @version 1.0.0
 * @time 2024-10-24 04:10:31
 */
public interface JudgeStrategy {
    /**
     * 执行判题
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext);
}
