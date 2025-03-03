package com.js.jsojbackenduserservice.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.js.jsojbackendmodel.dto.user.UserQueryRequest;
import com.js.jsojbackendmodel.entity.User;
import com.js.jsojbackendmodel.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户接口定义类
 *
 * @author sakisaki
 * @date 2025/2/22 15:14
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return long
     */
    long userRegister(String userPhone, String userPassword, String checkPassword, String userEmail);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @return LoginUserVO
     */
    UserVO userLogin(String userPhone, String userPassword);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);


    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 构建查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 根据refreshToken刷新token
     *
     * @param id 用户id
     * @return String
     */
    String baseRefreshTokenGetToken(long id);
}
