package com.siryus.swisscon.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naturalprogrammer.spring.lemon.exceptions.MultiErrorException;
import com.siryus.swisscon.api.exceptions.LocalizedReason;
import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;
import com.siryus.swisscon.api.util.validator.DTOValidator;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import static com.siryus.swisscon.api.auth.AuthException.NOT_AUTHORIZED;

@Component
@ControllerAdvice
public class CustomExceptionHandlerResolver extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = ResponseStatusException.class)
    protected ModelAndView handleResponseStatusException(
            ResponseStatusException ex, HttpServletRequest request, HttpServletResponse response, @Nullable Object handler)
            throws IOException {
        populateResponse(response, ex.getStatus().value(), ex.getReason());

        return new ModelAndView();
    }

    @ExceptionHandler(value = LocalizedResponseStatusException.class)
    protected ModelAndView handleLocalizedResponseStatusException(
            LocalizedResponseStatusException ex, HttpServletRequest request, HttpServletResponse response, @Nullable Object handler)
            throws IOException {
        populateLocalizedResponse(response, ex.getUid(), ex.getStatus().value(), ex.getLocalizedReason());

        return new ModelAndView();
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    protected ModelAndView handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request, HttpServletResponse response, @Nullable Object handler)
            throws IOException {
        populateLocalizedResponse(response, "", HttpStatus.SC_FORBIDDEN, NOT_AUTHORIZED.with());

        return new ModelAndView();
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    protected ModelAndView handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request, HttpServletResponse response, @Nullable Object handler)
            throws IOException {

        LocalizedResponseStatusException exception = DTOValidator.calculateAppropriateException(ex.getConstraintViolations().iterator().next());
        populateLocalizedResponse(response, exception.getUid(), exception.getStatus().value(), exception.getLocalizedReason());

        return new ModelAndView();
    }


    @ExceptionHandler(value = MultiErrorException.class)
    protected ModelAndView handleMultiErrorException(
            MultiErrorException ex, HttpServletRequest request, HttpServletResponse response, @Nullable Object handler)
            throws IOException {
        populateResponse(response, ex.getStatus().value(), ex.getMessage());

        return new ModelAndView();
    }

    @ExceptionHandler(value = RuntimeException.class)
    protected ModelAndView handleRuntimeException(
            RuntimeException ex, HttpServletRequest request, HttpServletResponse response, @Nullable Object handler)
            throws IOException {
        populateResponse(response, HttpStatus.SC_INTERNAL_SERVER_ERROR, ExceptionUtils.getStackTrace(ex));
        return new ModelAndView();
    }

    private static void populateResponse(HttpServletResponse response, Integer httpStatus, String reason) throws IOException {
        response.setStatus(httpStatus);

        response.setContentType(ContentType.APPLICATION_JSON.getMimeType());

        PrintWriter writer = response.getWriter();
        Map<String, Object> exceptionAttributes = new HashMap<>();
        exceptionAttributes.put("reason", reason);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(writer, exceptionAttributes);
        writer.flush();
    }

    public static void populateLocalizedResponse(HttpServletResponse response, String uid, Integer httpStatus, LocalizedReason reason) throws IOException {
        response.setStatus(httpStatus);

        response.setContentType(ContentType.APPLICATION_JSON.getMimeType());

        PrintWriter writer = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> asMap = new HashMap<>(reason.asMap());
        asMap.put("uid", uid);
        mapper.writeValue(writer, asMap);
        writer.flush();
    }
}
