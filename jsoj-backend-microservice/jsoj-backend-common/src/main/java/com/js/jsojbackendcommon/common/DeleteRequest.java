package com.js.jsojbackendcommon.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除请求
 * @author sakisaki
 * @date 2025/2/22 14:31
 */
@Data
public class DeleteRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    private Long id;
}