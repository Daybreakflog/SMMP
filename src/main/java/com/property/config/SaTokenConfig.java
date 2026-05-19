package com.property.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    private static final String[] EXCLUDE_PATHS = {
            "/auth/login",
            "/auth/sms-login",
            "/auth/sms/send",
            "/auth/captcha",
            "/actuator/**",
            "/doc.html",
            "/doc.html/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/webjars/**",
            "/druid/**",
            "/favicon.ico",
            "/ws/**"
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle ->
                SaRouter.match("/**")
                        .notMatch(EXCLUDE_PATHS)
                        .check(r -> StpUtil.checkLogin())
        )).addPathPatterns("/**");
    }
}
