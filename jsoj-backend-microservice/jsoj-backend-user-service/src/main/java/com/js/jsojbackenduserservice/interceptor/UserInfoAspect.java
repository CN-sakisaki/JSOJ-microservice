package com.js.jsojbackenduserservice.interceptor;

import com.js.jsojbackendcommon.common.ErrorCode;
import com.js.jsojbackendcommon.common.UserContext;
import com.js.jsojbackendcommon.exception.BusinessException;
import com.js.jsojbackendcommon.exception.ThrowUtils;
import com.js.jsojbackendmodel.entity.User;
import com.js.jsojbackendserviceclient.service.UserFeignClient;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 根据请求头获取用户信息记录于当前线程
 *
 * @author sakisaki
 * @date 2025/2/24 21:20
 */
@Aspect
@Component
@Order(1)
public class UserInfoAspect {

    private final UserFeignClient userFeignClient;

    public UserInfoAspect(UserFeignClient userFeignClient) {
        this.userFeignClient = userFeignClient;
    }

    // 定义切入点：拦截所有 Controller 层的请求处理方法
    @Pointcut("execution(* com.js.jsojbackenduserservice.controller..*.*(..))" +
            "&& !within(com.js.jsojbackenduserservice.controller.inner..*)")
    public void controllerMethods() {
    }

    /**
     * 在执行请求前记录当前用户信息
     *
     * @param joinPoint 请求
     */
    @Before("controllerMethods()")
    public void extractUserInfo(JoinPoint joinPoint) {
        // 获取当前请求对象
        ServletRequestAttributes attributes = (ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes();
        if (attributes == null) return;

        HttpServletRequest request = attributes.getRequest();

        // 从请求头获取用户ID
        String userIdHeader = request.getHeader("X-User-Id");
        ThrowUtils.throwIf(userIdHeader == null, ErrorCode.OPERATION_ERROR);
        try {
            long userId = Long.parseLong(userIdHeader);
            User user = userFeignClient.getById(userId);
            UserContext.setUser(user);
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID格式错误");
        }
    }

    /**
     * 请求完成后清理上下文
     */
    @AfterReturning("controllerMethods()")
    public void clearContext() {
        UserContext.clear();
    }


}
