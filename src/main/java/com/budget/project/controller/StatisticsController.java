package com.budget.project.controller;

import com.budget.project.model.dto.CategoryAmount;
import com.budget.project.service.StatisticsService;

import lombok.RequiredArgsConstructor;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @QueryMapping
    public List<CategoryAmount> getAmountByCategory(
            @Argument String startDate, @Argument String endDate, @Argument Boolean income) {
        return statisticsService.getAmountByCategory(startDate, endDate, income);
    }
}
