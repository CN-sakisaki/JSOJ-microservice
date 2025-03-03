package com.js.jsojbackendmodel.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 添加用户请求
 *
 * @author sakisaki
 * @date 2025/2/22 19:10
 */
@Data
public class UserAddRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 手机号
     */
    private String userPhone;

    /**
     * 邮箱
     */
    private String userEmail;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户角色: user, admin
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}