package com.example.watchaura.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限注解，用于标记 Controller 方法所需的角色
 * 使用方式：@RequiresRole({"Admin", "Quản lý"})
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {
    /**
     * 允许访问的角色数组
     * 如果用户拥有其中任何一个角色，即可访问
     */
    String[] value();

    /**
     * 是否要求用户已登录（默认true）
     * 如果为false，即使未登录也可访问（但不推荐在受保护路径使用）
     */
    boolean requireAuth() default true;
}
