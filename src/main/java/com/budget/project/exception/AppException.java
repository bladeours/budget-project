package com.budget.project.exception;

import static org.springframework.http.HttpStatus.FORBIDDEN;

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

    public AppException(HttpStatus status) {
        super(getExceptionMessage(status));
        this.code = status.value();
        this.status = status;
    }

    private static String getExceptionMessage(HttpStatus status) {
        switch (status) {
            case FORBIDDEN -> {
                return "you don't have permissions to do it";
            }
            case BAD_REQUEST -> {
                return "Bad Request";
            }
            default -> {
                return "exception";
            }
        }
    }
}
