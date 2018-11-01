package com.zlikun.jee.web;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用 SqlInjectionConfiguration 自动配置，仅适用于 SpringBoot 架构
 *
 * @author zlikun
 * @date 2018/11/1 13:44
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(SqlInjectionFilterConfiguration.class)
public @interface EnableSqlInjectionFilter {


}
