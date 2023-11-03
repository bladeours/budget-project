package com.budget.project.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.graphql.ResponseError;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.function.Predicate;

public class TestUtils {
    public static Map<String, Object> toMap(Object object) {
        return new ObjectMapper().convertValue(object, new TypeReference<>() {});
    }
    public static Predicate<ResponseError> errorTypeEquals(ErrorType errorType) {
        return responseError -> {
            if (responseError != null) {
                return responseError.getErrorType().equals(errorType);
            }
            return false;
        };
    }
}
