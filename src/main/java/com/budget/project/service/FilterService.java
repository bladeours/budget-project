package com.budget.project.service;

import com.intuit.graphql.filter.client.ExpressionFormat;
import com.intuit.graphql.filter.client.FilterExpression;
import com.intuit.graphql.filter.client.InvalidFilterException;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class FilterService {

    public <T> Specification<T> getSpecification(DataFetchingEnvironment env) {
        try {
            FilterExpression filterExpression =
                    FilterExpression.newFilterExpressionBuilder().args(env.getArguments()).build();

            return filterExpression.getExpression(ExpressionFormat.JPA);

        } catch (InvalidFilterException e) {
            return null;
        }
    }
}
