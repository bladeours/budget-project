package com.budget.project.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {
    private final int code;
    private final HttpStatus status;

    public AppException(String message, HttpStatus status) {
        super(message);
        this.code = status.value();
        this.status = status;
    }
}
