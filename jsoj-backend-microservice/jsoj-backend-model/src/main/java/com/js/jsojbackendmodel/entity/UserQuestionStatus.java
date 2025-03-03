package com.js.jsojbackendmodel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 用户-题目状态表
 * @TableName user_question_status
 */
@TableName(value = "user_question_status")
@Data
public class UserQuestionStatus {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 题目Id
     */
    private Long questionId;

    /**
     * 该用户与这道题的状态(1-尝试过  2-已解决)
     */
    private Integer questionStatus;

    /**
     * 创建时间
     */
    private Date createTime;
}