package com.example.oauth2.common.filter;

import com.example.oauth2.common.i18n.LocaleUtil;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LocaleRequestContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request, response);
        LocaleUtil.resolveLocale(request, response);
        RequestContextHolder.setRequestAttributes(requestAttributes, false);
        try {
            filterChain.doFilter(request, response);
        } finally {
            resetContextHolders();
            requestAttributes.requestCompleted();
        }
    }

    private void resetContextHolders() {
        LocaleContextHolder.resetLocaleContext();
        RequestContextHolder.resetRequestAttributes();
    }

}