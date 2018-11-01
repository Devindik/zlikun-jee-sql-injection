package com.zlikun.jee.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * 自动配置，依赖SpringBoot，如非SpringBoot架构，请自行配置 SqlInjectionFilter 过滤器
 *
 * @author zlikun
 * @date 2018/11/1 9:58
 */
@Configuration
@ConditionalOnClass(FilterRegistrationBean.class)
public class SqlInjectionFilterConfiguration {

    /**
     * 注册SQL注入过滤器
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(FilterRegistrationBean.class)
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new SqlInjectionFilter());
        registration.setUrlPatterns(Arrays.asList("/*"));
        registration.setName("sqlInjectionFilter");
        return registration;
    }

}
