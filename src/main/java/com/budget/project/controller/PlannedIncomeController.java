package com.budget.project.controller;

import java.time.LocalDateTime;
import java.time.YearMonth;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.budget.project.model.db.PlannedIncome;
import com.budget.project.model.dto.PlannedIncomeDto;
import com.budget.project.model.dto.request.input.PlannedIncomeInput;
import com.budget.project.service.PlannedIncomeService;
import com.budget.project.utils.DateUtils;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PlannedIncomeController {
  private final PlannedIncomeService plannedIncomeService;
  
  @QueryMapping
  PlannedIncomeDto getPlannedIncome(@Argument String date) {
    LocalDateTime datetime = DateUtils.parse(date);
    return plannedIncomeService.getPlannedIncome(YearMonth.of(datetime.getYear(), datetime.getMonth()));
  }

  @MutationMapping
  PlannedIncome addPlannedIncome(@Argument PlannedIncomeInput plannedIncomeInput){
    return plannedIncomeService.createPlannedIncome(plannedIncomeInput);
  }

  @MutationMapping
  PlannedIncome updatePlannedIncome(@Argument String hash, @Argument Double amount) {
    return plannedIncomeService.updatePlannedIncome(hash, amount);
  }

  @MutationMapping
  Boolean deletePlannedIncome(@Argument String hash) {
    plannedIncomeService.deletePlannedIncome(hash);
    return true;
  }
  
}
