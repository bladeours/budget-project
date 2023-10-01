package com.budget.project.exception.model;

import org.springframework.http.HttpStatus;

public record ServerExceptionResponse(int code, HttpStatus status, String message) {}
