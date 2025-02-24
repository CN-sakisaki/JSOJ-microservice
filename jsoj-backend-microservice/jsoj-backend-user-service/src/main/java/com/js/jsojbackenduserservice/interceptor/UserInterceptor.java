package com.js.jsojbackenduserservice.interceptor;

import com.js.jsojbackendcommon.common.ErrorCode;
import com.js.jsojbackendcommon.common.UserContext;
import com.js.jsojbackendcommon.exception.ThrowUtils;
import com.js.jsojbackendmodel.entity.User;
import com.js.jsojbackendserviceclient.service.UserFeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户上下文工具类
 * @author sakisaki
 * @date 2025/2/23 22:58
 */
@Component
public class UserInterceptor implements HandlerInterceptor {

    private final UserFeignClient userFeignClient;

    public UserInterceptor(UserFeignClient userFeignClient) {
        this.userFeignClient = userFeignClient;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 从请求头获取网关传递的用户信息
        String userId = request.getHeader("X-User-Id");
        User user = userFeignClient.getById(Long.parseLong(userId));
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        UserContext.setUser(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求完成后清理上下文，防止内存泄漏
        UserContext.clear();
    }
}
