package com.budget.project.exception.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

public class CustomForbiddenEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String message =
                new ObjectMapper()
                        .writeValueAsString(
                                new ServerExceptionResponse(
                                        HttpStatus.FORBIDDEN.value(),
                                        HttpStatus.FORBIDDEN,
                                        "You don't have access"));
        response.getWriter().print(message);
    }
}
