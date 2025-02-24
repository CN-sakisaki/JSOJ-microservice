package com.js.jsojbackendcommon.common;

import com.js.jsojbackendmodel.entity.User;


/**
 * 上下文工具类
 * @author sakisaki
 * @date 2025/2/23 22:56
 */
public class UserContext {
    private static final ThreadLocal<User> userHolder = new ThreadLocal<>();

    // 存储用户信息
    public static void setUser(User user) {
        userHolder.set(user);
    }

    // 清除上下文
    public static void clear() {
        userHolder.remove();
    }
}
