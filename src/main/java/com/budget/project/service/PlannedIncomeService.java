package com.budget.project.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.budget.project.exception.AppException;
import com.budget.project.model.db.Budget;
import com.budget.project.model.db.PlannedIncome;
import com.budget.project.model.dto.BudgetDto;
import com.budget.project.model.dto.PlannedIncomeDto;
import com.budget.project.model.dto.request.input.PlannedIncomeInput;
import com.budget.project.service.repository.PlannedIncomeRepository;
import com.budget.project.utils.DateUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PlannedIncomeService {
   private final PlannedIncomeRepository plannedIncomeRepository;
   private final BudgetService budgetService;
   private final UserService userService;

   public PlannedIncome createPlannedIncome(YearMonth date, Double amount){
    return plannedIncomeRepository.save(
        PlannedIncome.builder()
        .date(date)
        .amount(amount)
        .hash(UUID.randomUUID().toString())
        .user(userService.getLoggedUser())
        .build()
    );
   }

   public PlannedIncomeDto getPlannedIncome(YearMonth date){
        Optional<PlannedIncome> plannedIncomeOptional = 
        plannedIncomeRepository.findByDateAndUser(date, userService.getLoggedUser());
        if(plannedIncomeOptional.isPresent()){
            return getPlannedIncomeDto(plannedIncomeOptional.get());
        }
        return getPlannedIncomeDto(createPlannedIncome(date, 0.0));
    }

    private PlannedIncomeDto getPlannedIncomeDto(PlannedIncome plannedIncome){
        List<BudgetDto> budgetDtoList = budgetService.getBudgetDtoList(plannedIncome.getDate());
        Double sum = 
        budgetDtoList.stream().collect(Collectors.summingDouble(b -> b.budget().getPlannedBudget()));
        Double left = plannedIncome.getAmount() - sum;
        Double percent = plannedIncome.getAmount().equals(0.0) ? 0.0 : (sum/plannedIncome.getAmount()) * 100;
        return new PlannedIncomeDto(plannedIncome, left, percent);
    }

    public PlannedIncome createPlannedIncome(PlannedIncomeInput plannedIncomeInput) {
        LocalDateTime date = DateUtils.parse(plannedIncomeInput.date());
        if (date.getDayOfMonth()
                < userService.getLoggedUser().getSettings().getFirstDayOfTheMonth()) {
            date = date.minusMonths(1);
        }

        PlannedIncome plannedIncome = PlannedIncome.builder()
        .amount(plannedIncomeInput.amount())
        .hash(UUID.randomUUID().toString())
        .date(YearMonth.of(date.getYear(), date.getMonth()))
        .user(userService.getLoggedUser())
        .build();

        return plannedIncomeRepository.save(plannedIncome);
    }

    public PlannedIncome updatePlannedIncome(String hash, Double amount) {
        PlannedIncome plannedIncome = this.getPlannedIncome(hash);
        plannedIncomeRepository.delete(plannedIncome);
        return createPlannedIncome(PlannedIncomeInput.builder()
        .date(OffsetDateTime.now().withYear(plannedIncome.getDate().getYear())
        .withMonth(plannedIncome.getDate().getMonthValue()).toString())
        .amount(amount)
        .build()
        );
    }

    public PlannedIncome getPlannedIncome(String hash) {
        return plannedIncomeRepository
                .findByHashAndUser(hash, userService.getLoggedUser())
                .orElseThrow(() -> {
                    log.warn("can't find planned income with hash: {}", hash);
                    return new AppException(
                            "can't find planned income with hash: " + hash, HttpStatus.NOT_FOUND);
                });
    }

    public void deletePlannedIncome(String hash) {
        plannedIncomeRepository.delete(this.getPlannedIncome(hash));
    }
}
