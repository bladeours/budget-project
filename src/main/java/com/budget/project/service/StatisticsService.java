package com.budget.project.service;

import com.budget.project.model.dto.CategoryAmount;
import com.budget.project.service.projection.TransactionCategoryNameSum;
import com.budget.project.utils.DateUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class StatisticsService {
    private final TransactionService transactionService;

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
}
