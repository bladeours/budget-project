package com.budget.project.service;

import com.budget.project.exception.AppException;
import com.budget.project.model.db.Budget;
import com.budget.project.model.db.Category;
import com.budget.project.model.db.SubCategory;
import com.budget.project.model.dto.BudgetDto;
import com.budget.project.model.dto.request.input.BudgetInput;
import com.budget.project.service.projection.TransactionCategorySum;
import com.budget.project.service.repository.BudgetRepository;
import com.budget.project.utils.DateUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final CategoryService categoryService;
    private final UserService userService;
    private final TransactionService transactionService;
    @SneakyThrows
    public Budget createBudget(BudgetInput budgetInput) {
        Category category = categoryService.getCategory(budgetInput.categoryHash());
        if (category.getIncome()) {
            log.warn("category has to be expense");
            throw new AppException("category has to be expense", HttpStatus.BAD_REQUEST);
        }
        SubCategory subCategory = null;
        if (Objects.nonNull(budgetInput.subCategoryHash())) {
            subCategory = category.getSubCategories().stream()
                    .filter(c -> c.getHash().equals(budgetInput.subCategoryHash()))
                    .findFirst()
                    .orElseThrow(() -> {
                        log.warn(
                                "can't find subCategory with hash: {}",
                                budgetInput.subCategoryHash());
                        return new AppException(
                                "can't find subCategory with hash: "
                                        + budgetInput.subCategoryHash(),
                                HttpStatus.NOT_FOUND);
                    });
        }

        LocalDateTime date = DateUtils.parse(budgetInput.date());
        if (date.getDayOfMonth()
                < userService.getLoggedUser().getSettings().getFirstDayOfTheMonth()) {
            date = date.minusMonths(1);
        }

        Budget budget = budgetRepository.save(Budget.builder()
                .user(userService.getLoggedUser())
                .category(category)
                .plannedBudget(budgetInput.plannedBudget())
                .hash(UUID.randomUUID().toString())
                .subCategory(subCategory)
                .date(YearMonth.of(date.getYear(), date.getMonth()))
                .build());
        userService.getLoggedUser().getBudgets().add(budget);
        return budget;
    }

    public List<BudgetDto> getBudgetDtoList(String date) {
        LocalDateTime dateTime = DateUtils.parse(date);
        YearMonth yearMonth = YearMonth.of(dateTime.getYear(), dateTime.getMonth());
        return getBudgetDtoList(yearMonth);
    }

    private List<BudgetDto> getBudgetDtoList(YearMonth yearMonth) {
        Integer firstDayOfTheMonth =
                userService.getLoggedUser().getSettings().getFirstDayOfTheMonth();
        LocalDateTime startDate = yearMonth.atDay(firstDayOfTheMonth).atStartOfDay();
        LocalDateTime endDate = yearMonth.atDay(firstDayOfTheMonth).atStartOfDay().plusMonths(1).minusSeconds(1);
        //TODO test first day of the month
        List<TransactionCategorySum> transactionCategoryNameSums =
                transactionService.sumTransactionAmountForCategories(startDate, endDate);
        transactionCategoryNameSums = normalizeTransactionCategorySum(transactionCategoryNameSums);
        List<Budget> budgets = budgetRepository.findAllByDateAndUser(yearMonth, userService.getLoggedUser());
        List<BudgetDto> budgetDtoList = new ArrayList<>();
        for (TransactionCategorySum transactionCategoryNameSum : transactionCategoryNameSums) {
            Category category = transactionCategoryNameSum.getCategory();
            Budget budget = Objects.requireNonNullElse(
                    getBudgetFromListForCategory(budgets, category),
                    Budget.builder().plannedBudget(0.0).category(category).build());
            Double percent = getPercent(
                    budget.getPlannedBudget(), transactionCategoryNameSum.getSumForCategory());
            budgetDtoList.add(new BudgetDto(
                    budget,
                    percent,
                    budget.getPlannedBudget() - transactionCategoryNameSum.getSumForCategory()));
        }
        return budgetDtoList;
    }

    private Double getPercent(Double plannedBudget, Double sumForCategory) {
        if (plannedBudget == 0) {
            return 0.0;
        }
        return sumForCategory / plannedBudget;
    }

    private List<TransactionCategorySum> normalizeTransactionCategorySum(List<TransactionCategorySum> transactionCategorySums){
        List<Category> categories = categoryService.getCategories(null);
        categories = categories.stream().filter(c -> !c.getIncome()).toList();
        for(Category category: categories) {
            if(transactionCategorySums.stream().noneMatch(t -> t.getCategory().equals(category))){
                transactionCategorySums.add(new TransactionCategorySum() {
                    @Override
                    public Category getCategory() {
                        return category;
                    }

                    @Override
                    public Double getSumForCategory() {
                        return 0.0;
                    }
                });
            }
        }
        return transactionCategorySums;
    }

    private Budget getBudgetFromListForCategory(List<Budget> budgets, Category category) {
        return budgets.stream()
                .filter(b -> b.getCategory().equals(category))
                .findFirst()
                .orElse(null);
    }

    public Budget getBudget(String hash){
        return budgetRepository
                .findByHashAndUser(hash, userService.getLoggedUser())
                .orElseThrow(() -> {
                    log.warn("can't find budget with hash: {}", hash);
                    return new AppException(
                            "can't find budget with hash: " + hash, HttpStatus.NOT_FOUND);
                });
    }

    public Budget updateBudget(Double plannedBudget, String hash) {
        Budget budget = this.getBudget(hash);
        budget.setPlannedBudget(plannedBudget);
        return budget;
    }

    public Boolean deleteBudget(String hash) {
        this.budgetRepository.delete(this.getBudget(hash));
        return true;
    }
}
