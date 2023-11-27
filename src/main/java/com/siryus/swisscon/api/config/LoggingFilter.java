package com.siryus.swisscon.api.config;

import com.naturalprogrammer.spring.lemon.commons.security.LemonPrincipal;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

@Component
public class LoggingFilter implements Filter {

    private static final Integer CORRELATION_ID_LENGTH = 5;

    private static final String CORRELATION_ID = "correlationId";
    private static final String USER_ID_KEY = "userId";
    private static final String USERNAME_KEY = "username";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        MDC.put(CORRELATION_ID, generateCorrelationId());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (hasExtractablePrincipal(authentication)) {
            LemonPrincipal principal = ((LemonPrincipal) authentication.getPrincipal());
            MDC.put(USER_ID_KEY, principal.currentUser().getId());
            MDC.put(USERNAME_KEY, principal.getUsername());
        }

        filterChain.doFilter(servletRequest, servletResponse);
        MDC.remove(USER_ID_KEY);
        MDC.remove(USERNAME_KEY);
        MDC.remove(CORRELATION_ID);
    }

    private boolean hasExtractablePrincipal(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof LemonPrincipal;
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // Do nothing
    }

    @Override
    public void destroy() {
        // Do nothing
    }

    private String generateCorrelationId() {
        return RandomStringUtils.random(CORRELATION_ID_LENGTH, true, true ).toUpperCase();
    }
}
