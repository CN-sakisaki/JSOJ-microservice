package com.js.jsojbackenduserservice.interceptor;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.js.jsojbackendcommon.annotation.AuthCheck;
import com.js.jsojbackendcommon.common.ErrorCode;
import com.js.jsojbackendcommon.common.UserContext;
import com.js.jsojbackendcommon.exception.ThrowUtils;
import com.js.jsojbackendmodel.entity.User;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 权限拦截
 *
 * @author sakisaki
 * @date 2025/2/24 22:33
 */
@Aspect
@Component
@Order(2)
public class AuthInterceptor {

    /**
     * 执行拦截
     *
     * @param joinPoint
     * @param authCheck
     * @return
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        User user = UserContext.getUser();
        // 必须有所有权限才通过
        if (StringUtils.isNotBlank(mustRole)) {
            String userRole = user.getUserRole();
            ThrowUtils.throwIf(!mustRole.equals(userRole), ErrorCode.NO_AUTH_ERROR);
        }
        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}
