package com.js.jsojbackendmodel.dto.question;

import com.baomidou.mybatisplus.annotation.TableField;
import com.js.jsojbackendmodel.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @author JianShang
 * @version 1.0.0
 * @description 查询请求
 * @date 2024-10-15 03:35:48
 * @see PageRequest
 * */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表（json 数组）
     */
    private List<String> tags;

    /**
     * 创建用户 id
     */
    private Long userId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}