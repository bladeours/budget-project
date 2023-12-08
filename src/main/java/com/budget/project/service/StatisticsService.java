package com.budget.project.service;

import com.budget.project.filter.model.*;
import com.budget.project.model.db.Category;
import com.budget.project.model.db.Transaction;
import com.budget.project.model.dto.CategoryAmount;

import com.budget.project.service.projection.TransactionCategorySum;
import com.budget.project.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class StatisticsService {
    private final CategoryService categoryService;
    private final TransactionService transactionService;

    public List<CategoryAmount> getAmountByCategory(
            String startDate, String endDate, Boolean income) {
        List<TransactionCategorySum> transactions = transactionService.sumTransactionAmountForCategories(income, DateUtils.parse(startDate), DateUtils.parse(endDate));
        return transactions.stream().map(t -> new CategoryAmount(t.getCategoryName(), t.getSumForCategory())).toList();
    }
}
