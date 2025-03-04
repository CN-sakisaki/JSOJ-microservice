package com.js.jsojbackendmodel.dto.userQuestionStatus;

import com.js.jsojbackendmodel.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author sakisaki
 * @date 2025/3/4 19:41
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQuestionStatusQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = -1910437784970098523L;

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
