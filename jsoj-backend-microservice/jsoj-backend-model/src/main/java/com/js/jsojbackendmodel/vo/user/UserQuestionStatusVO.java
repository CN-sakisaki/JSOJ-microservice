package com.js.jsojbackendmodel.vo.user;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author sakisaki
 * @date 2025/3/4 19:44
 */
@Data
public class UserQuestionStatusVO implements Serializable {

    private static final long serialVersionUID = -4456294302639512675L;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 题目Id
     */
    private Long questionId;

    /**
     * 题目名称
     */
    private String questionTitle;

    /**
     * 该用户与这道题的状态(1-尝试过  2-已解决)
     */
    private Integer questionStatus;

    /**
     * 创建时间
     */
    private Date createTime;
}
