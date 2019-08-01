package com.example.oauth2.common.config;

import com.example.oauth2.common.filter.LocaleRequestContextFilter;
import com.example.oauth2.common.i18n.LocaleUtil;
import com.example.oauth2.common.util.SecurityExceptionUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import javax.servlet.Servlet;

/**
 * 自定义配置
 * <p>
 * Created by mike on 2019-07-28
 */
@Configuration
@ConditionalOnClass(Servlet.class)
public class CustomConfig {

    /**
     * web响应异常转换器
     */
    @Bean
    public WebResponseExceptionTranslator webResponseExceptionTranslator() {
        return new CustomWebResponseExceptionTranslator();
    }

    /**
     * OAuth2 AccessDeniedHandler
     */
    @Bean
    public AccessDeniedHandler oAuth2AccessDeniedHandler(WebResponseExceptionTranslator exceptionTranslator) {
        OAuth2AccessDeniedHandler accessDeniedHandler = new OAuth2AccessDeniedHandler();
        accessDeniedHandler.setExceptionTranslator(exceptionTranslator);
        return accessDeniedHandler;
    }

    /**
     * OAuth2 AuthenticationEntryPoint
     */
    @Bean
    public AuthenticationEntryPoint oAuth2AuthenticationEntryPoint(WebResponseExceptionTranslator exceptionTranslator) {
        OAuth2AuthenticationEntryPoint authenticationEntryPoint = new OAuth2AuthenticationEntryPoint();
        authenticationEntryPoint.setExceptionTranslator(exceptionTranslator);
        return authenticationEntryPoint;
    }

    @Bean
    public LocaleUtil localeUtil(LocaleChangeInterceptor localeChangeInterceptor,
                                 LocaleResolver localeResolver) {
        return new LocaleUtil(localeChangeInterceptor, localeResolver);
    }

    @Bean
    public SecurityExceptionUtil securityExceptionUtil() {
        return new SecurityExceptionUtil();
    }

    @Bean
    public OncePerRequestFilter localeRequestContextFilter() {
        return new LocaleRequestContextFilter();
    }
}
