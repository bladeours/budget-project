package com.budget.project.exception;

import com.budget.project.exception.model.ServerExceptionResponse;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler implements AccessDeniedHandler {
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ServerExceptionResponse> handleAppException(AppException ex) {
        return new ResponseEntity<>(
                new ServerExceptionResponse(ex.getCode(), ex.getStatus(), ex.getMessage()),
                ex.getStatus());
    }

    @ExceptionHandler(InternalServerError.class)
    public ResponseEntity<ServerExceptionResponse> handleInternalServerError(
            InternalServerError ex) {
        return new ResponseEntity<>(
                new ServerExceptionResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Internal Server Error"),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ServerExceptionResponse> handleExpiredJwtException(
            ExpiredJwtException ex) {
        return new ResponseEntity<>(
                new ServerExceptionResponse(
                        HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN, "Token Expired"),
                HttpStatus.FORBIDDEN);
    }

    @SneakyThrows
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) {
        throw new AppException("you don't have access", HttpStatus.FORBIDDEN);
    }
}
