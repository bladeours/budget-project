package com.budget.project.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@Getter
public class AppException extends RuntimeException {
    private final int code;
    private final HttpStatus status;

    public AppException(String message, HttpStatus status) {
        super(message);
        this.code = status.value();
        this.status = status;
    }

    public AppException(HttpStatus status) {
        super(getExceptionMessage(status));
        this.code = status.value();
        this.status = status;
    }

    private static String getExceptionMessage(HttpStatus status) {
        switch (status) {
            case FORBIDDEN -> {
                return  "you don't have permissions to do it";
            }
            default -> {
                return "exception";
            }
        }
    }
}
