package com.siryus.swisscon.api.config;

import com.siryus.swisscon.api.auth.AuthException;
import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final CustomExceptionHandlerResolver customExceptionHandlerResolver;

    public CustomAuthenticationFailureHandler(CustomExceptionHandlerResolver customExceptionHandlerResolver) {
        this.customExceptionHandlerResolver = customExceptionHandlerResolver;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        LocalizedResponseStatusException exception = authException.getCause() instanceof LocalizedResponseStatusException ?
                ((LocalizedResponseStatusException) authException.getCause()) :
                AuthException.authenticationFailure(authException.getMessage());
        customExceptionHandlerResolver.handleLocalizedResponseStatusException(exception, request, response, null);
    }
}
