package com.example.securedoc.utils;

import com.example.securedoc.domain.Response;
import com.example.securedoc.exception.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static java.time.LocalTime.now;
import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class RequestUtils {
    public static final BiConsumer<HttpServletResponse, Response> writeResponse = (httpServletResponse, response) -> {
        try {
            var outputStream = httpServletResponse.getOutputStream();
            new ObjectMapper().writeValue(outputStream, response);
            outputStream.flush();
        } catch (Exception e) {
            throw new ApiException(e.getMessage());
        }
    };

    private static final BiFunction<Exception, HttpStatus, String> errorResponse = (exception, status) -> {
        if (status.isSameCodeAs(FORBIDDEN)) {
            return "Not enough permission";
        }

        if (status.isSameCodeAs(UNAUTHORIZED)) {
            return "Not logged in";
        }

        if (exception instanceof DisabledException || exception instanceof LockedException ||
                exception instanceof BadCredentialsException || exception instanceof CredentialsExpiredException ||
                exception instanceof ApiException
        ) {
            return exception.getMessage();
        }

        if (status.is5xxServerError()) {
            return "Internal Server Error";
        }

        return "Unknown Error occurred. Try again";
    };


    public static Response getResponse(HttpServletRequest request, Map<?, ?> data, String message, HttpStatus status) {
        return new Response(LocalDateTime.now().toString(), status.value(), request.getRequestURI(), HttpStatus.valueOf(status.value()), message, EMPTY, data);
    }

    public static void handleErrorResponse(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        if (exception instanceof AccessDeniedException) {
            var apiResponse = getErrorResponse(request, response, exception, FORBIDDEN);
            writeResponse.accept(response, apiResponse);
        } else {
            var rep = getErrorResponse(request, response, exception, INTERNAL_SERVER_ERROR);
            writeResponse.accept(response, rep);
        }
    }

    private static Response getErrorResponse(HttpServletRequest request, HttpServletResponse response, Exception exception, HttpStatus status) {
        response.setStatus(status.value());
        response.setContentType(APPLICATION_JSON_VALUE);

        return new Response(now().toString(), status.value(), request.getRequestURI(), HttpStatus.valueOf(status.value()),
                errorResponse.apply(exception, status), getRootCauseMessage(exception), emptyMap());
    }
}
