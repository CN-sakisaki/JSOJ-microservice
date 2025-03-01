package com.js.jsojbackendcommon.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 排除特定的方法
 * @author sakisaki
 * @date 2025/3/1 00:04
 */
@Target(ElementType.METHOD) // 注解作用于方法
@Retention(RetentionPolicy.RUNTIME) // 注解在运行时保留
public @interface ExcludeMethod {
}
