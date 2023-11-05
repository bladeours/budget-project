package com.budget.project.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Component
@Slf4j
public class CustomExceptionResolver  extends DataFetcherExceptionResolverAdapter {
    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof AppException) {
            ErrorType errorType = ErrorType.INTERNAL_ERROR;
            switch (((AppException) ex).getStatus()) {
                case FORBIDDEN -> errorType = ErrorType.FORBIDDEN;
                case UNAUTHORIZED -> errorType = ErrorType.UNAUTHORIZED;
                case BAD_REQUEST -> errorType = ErrorType.BAD_REQUEST;
                case NOT_FOUND -> errorType = ErrorType.NOT_FOUND;
            }
            return GraphqlErrorBuilder.newError()
                    .errorType(errorType)
                    .message(ex.getMessage())
                    .path(env.getExecutionStepInfo().getPath())
                    .location(env.getField().getSourceLocation())
                    .build();
        } else {
            log.error("" + ex);
            return null;
//            return GraphqlErrorBuilder.newError()
//                    .errorType(ErrorType.INTERNAL_ERROR)
//                    .message(Objects.requireNonNullElse(ex.getMessage(), "no message"))
//                    .path(env.getExecutionStepInfo().getPath())
//                    .location(env.getField().getSourceLocation())
//                    .build();
        }
    }
}
