package com.js.jsojbackendmodel.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 * @author sakisaki
 * @date 2025/2/22 19:10
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;

    private String userEmail;

}
