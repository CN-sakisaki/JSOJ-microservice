package com.js.jsojbackendquestionservice.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.js.jsojbackendcommon.utils.JacksonUtils;
import com.js.jsojbackendmodel.dto.question.JudgeCase;
import com.js.jsojbackendmodel.dto.question.JudgeConfig;
import com.js.jsojbackendmodel.entity.Question;
import com.js.jsojbackendmodel.vo.admin.QuestionAdminVO;
import com.js.jsojbackendmodel.vo.user.QuestionUserVO;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Question工具类
 * @author sakisaki
 * @date 2025/3/3 23:33
 */
public class QuestionHelper {

    /**
     * 提取公共的Question对象设置逻辑
     *
     * @param question 要设置的Question对象
     * @param tags 标签列表
     * @param judgeCase 判题用例列表
     * @param judgeConfig 判题配置
     */
    public static void setQuestionProperties(Question question, List<String> tags, List<JudgeCase> judgeCase, JudgeConfig judgeConfig) {
        if (tags != null) {
            question.setTags(JSONUtil.toJsonStr(tags));
        }
        if (judgeCase != null) {
            question.setJudgeCase(JSONUtil.toJsonStr(judgeCase));
        }
        if (judgeConfig != null) {
            question.setJudgeConfig(JSONUtil.toJsonStr(judgeConfig));
        }
    }

    /**
     * 将 Question 转换为目标 VO 对象
     *
     * @param question 题目实体
     * @param vo       目标 VO 对象
     * @param <T>      目标 VO 类型
     */
    public static <T> void convertToVO(Question question, T vo) {
        // 复制普通字段
        BeanUtil.copyProperties(question, vo, "tags", "judgeCase", "judgeConfig");

        // 处理 tags（JSON 字符串 -> List<String>）
        if (StringUtils.isNotBlank(question.getTags())) {
            List<String> tags = JacksonUtils.toList(question.getTags(), String.class);
            if (vo instanceof QuestionUserVO) {
                ((QuestionUserVO) vo).setTags(tags);
            } else if (vo instanceof QuestionAdminVO) {
                ((QuestionAdminVO) vo).setTags(tags);
            }
        }

        // 处理 judgeCase（JSON 字符串 -> List<JudgeCase>）
        if (StringUtils.isNotBlank(question.getJudgeCase())) {
            List<JudgeCase> judgeCase = JacksonUtils.toList(question.getJudgeCase(), JudgeCase.class);
            if (vo instanceof QuestionUserVO) {
                ((QuestionUserVO) vo).setJudgeCase(judgeCase);
            } else if (vo instanceof QuestionAdminVO) {
                ((QuestionAdminVO) vo).setJudgeCase(judgeCase);
            }
        }

        // 处理 judgeConfig（JSON 字符串 -> JudgeConfig）
        if (StringUtils.isNotBlank(question.getJudgeConfig())) {
            JudgeConfig judgeConfig = JacksonUtils.toObject(question.getJudgeConfig(), JudgeConfig.class);
            if (vo instanceof QuestionUserVO) {
                ((QuestionUserVO) vo).setJudgeConfig(judgeConfig);
            } else if (vo instanceof QuestionAdminVO) {
                ((QuestionAdminVO) vo).setJudgeConfig(judgeConfig);
            }
        }
    }
}
