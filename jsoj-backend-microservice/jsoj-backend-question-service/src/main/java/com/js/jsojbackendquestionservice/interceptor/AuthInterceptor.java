package com.js.jsojbackendquestionservice.interceptor;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.js.jsojbackendcommon.annotation.AuthCheck;
import com.js.jsojbackendcommon.common.ErrorCode;
import com.js.jsojbackendcommon.common.UserContext;
import com.js.jsojbackendcommon.exception.BusinessException;
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
     * @param joinPoint 切点
     * @param authCheck 权限校验注解
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 获取当前登录用户
        User user = UserContext.getUser();

        // 如果用户未登录，抛出未登录异常
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);

        // 获取 mustRole 参数
        String mustRole = authCheck.mustRole();

        // 如果 mustRole 不为空，检查用户是否具有该角色
        if (StringUtils.isNotBlank(mustRole)) {
            String userRole = user.getUserRole();
            if (!mustRole.equals(userRole)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }

        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}
