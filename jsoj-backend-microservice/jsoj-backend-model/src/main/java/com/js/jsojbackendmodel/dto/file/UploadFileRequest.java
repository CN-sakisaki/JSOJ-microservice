package com.js.jsojbackendmodel.dto.file;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件上传请求
 * @author sakisaki
 * @date 2025/2/22 19:10
 */
@Data
public class UploadFileRequest implements Serializable {

    /**
     * 业务
     */
    private String biz;

    private static final long serialVersionUID = 1L;
}