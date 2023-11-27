package com.siryus.swisscon.api.config;

import com.siryus.swisscon.api.auth.AuthException;
import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public final class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final CustomExceptionHandlerResolver customExceptionHandlerResolver;

    public CustomAuthenticationEntryPoint(CustomExceptionHandlerResolver customExceptionHandlerResolver) {
        this.customExceptionHandlerResolver = customExceptionHandlerResolver;
    }

    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        LocalizedResponseStatusException exception = authException.getCause() instanceof LocalizedResponseStatusException ?
                ((LocalizedResponseStatusException) authException.getCause()) :
                AuthException.unauthorizedRequest(authException.getMessage());
        customExceptionHandlerResolver.handleLocalizedResponseStatusException(exception, request, response, null);
    }
}
