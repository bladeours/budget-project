package com.budget.project.service;

import com.budget.project.model.db.User;
import com.budget.project.model.dto.CategoryAmount;
import com.budget.project.model.dto.IncomeExpense;
import com.budget.project.service.projection.TransactionCategoryNameSum;
import com.budget.project.utils.DateUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class StatisticsService {
    private final TransactionService transactionService;
    private final UserService userService;

    public List<CategoryAmount> getAmountByCategory(
            String startDate, String endDate, Boolean income) {
        List<TransactionCategoryNameSum> transactions =
                transactionService.sumTransactionAmountForCategoriesName(
                        income, DateUtils.parse(startDate), DateUtils.parse(endDate));
        return transactions.stream()
                .map(t -> new CategoryAmount(
                        t.getCategoryName(), t.getSumForCategory(), t.getCategoryColor()))
                .toList();
    }

    public IncomeExpense getIncomeExpense(String date) {
        User user = userService.getLoggedUser();
        LocalDateTime localDateTime = DateUtils.parse(date);
        LocalDateTime startDate = localDateTime.withHour(0).withSecond(0).withMinute(0);
        LocalDateTime endDate = localDateTime.withHour(23).withMinute(59).withSecond(0);
        int firstDay = user.getSettings().getFirstDayOfTheMonth();
        if(localDateTime.getDayOfMonth() < firstDay) {
            startDate = startDate.withDayOfMonth(firstDay).minusMonths(1);
            endDate = endDate.withDayOfMonth(firstDay - 1);
        } else {
            startDate = startDate.withDayOfMonth(firstDay);
            endDate = endDate.withDayOfMonth(firstDay - 1).plusMonths(1);
        }
        Double income = transactionService.getIncome(startDate, endDate).orElse(0D);
        Double expense = transactionService.getExpense(startDate, endDate).orElse(0D);
        return new IncomeExpense(income, expense);
    }
}
