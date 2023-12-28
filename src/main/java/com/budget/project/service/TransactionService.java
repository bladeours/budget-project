package com.budget.project.service;

import com.budget.project.exception.AppException;
import com.budget.project.filter.model.Filter;
import com.budget.project.filter.service.FilterService;
import com.budget.project.model.db.*;
import com.budget.project.model.dto.CustomPage;
import com.budget.project.model.dto.request.input.TransactionInput;
import com.budget.project.service.projection.DayExpense;
import com.budget.project.service.projection.MonthExpense;
import com.budget.project.service.projection.TransactionCategoryNameSum;
import com.budget.project.service.projection.TransactionCategorySum;
import com.budget.project.service.repository.TransactionRepository;
import com.budget.project.utils.DateUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final FilterService filterService;

    public TransactionService(
            TransactionRepository transactionRepository,
            UserService userService,
            @Lazy AccountService accountService,
            @Lazy CategoryService categoryService,
            FilterService filterService) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
        this.accountService = accountService;
        this.categoryService = categoryService;
        this.filterService = filterService;
    }

    @SneakyThrows
    public Transaction createTransaction(TransactionInput transactionInput) {
        validate(transactionInput);
        switch (transactionInput.transactionType()) {
            case EXPENSE -> {
                return handleCreateExpenseTransaction(transactionInput);
            }
            case TRANSFER -> {
                return handleCreateTransferTransaction(transactionInput);
            }
            case INCOME -> {
                return handleCraeteIncomeTransaction(transactionInput);
            }
            default -> {
                log.warn("unsupported operation");
                throw new AppException(HttpStatus.BAD_REQUEST);
            }
        }
    }

    public Page<Transaction> getTransactionsPage(CustomPage page, Filter filter) {
        PageRequest pageRequest =
                PageRequest.of(page.number(), page.size(), Sort.by("date").descending());
        if (Objects.isNull(filter) || Objects.isNull(filter.logicOperator())) {
            return transactionRepository.findTransactionsForUser(
                    userService.getLoggedUser(), pageRequest);
        }
        return transactionRepository.findAll(
                filterService.getSpecification(filter, Transaction.class), pageRequest);
    }

    public Transaction getTransaction(String hash) {
        return transactionRepository
                .findByHashForUser(hash, userService.getLoggedUser())
                .orElseThrow(() -> {
                    log.warn("can't find transaction with hash: {}", hash);
                    return new AppException(
                            "can't find transaction with hash: " + hash, HttpStatus.NOT_FOUND);
                });
    }

    public void deleteTransaction(Transaction transaction) {
        // TODO rollback changes
        Account accountTo = transaction.getAccountTo();
        if (Objects.nonNull(accountTo)) {
            accountTo.getTransactions().remove(transaction);
        }
        Account accountFrom = transaction.getAccountFrom();
        if (Objects.nonNull(accountFrom)) {
            accountFrom.getTransactions().remove(transaction);
        }
        transactionRepository.delete(transaction);
    }

    public void deleteTransaction(String hash) {
        Transaction transaction = this.getTransaction(hash);
        deleteTransaction(transaction);
    }

    public Transaction updateTransaction(String hash, TransactionInput transactionInput) {
        Transaction transaction = rollbackChanges(this.getTransaction(hash));
        validate(
                transactionInput.transactionType(),
                transactionInput.accountToHash(),
                transactionInput.accountFromHash(),
                transactionInput.categoryHash());

        switch (transactionInput.transactionType()) {
            case EXPENSE -> {
                Account newAccountFrom =
                        accountService.getAccount(transactionInput.accountFromHash());
                Category category = categoryService.getCategory(transactionInput.categoryHash());
                if (category.getIncome()) {
                    log.warn("category is not for \"expense\"");
                    throw new AppException("incorrect category", HttpStatus.BAD_REQUEST);
                }
                SubCategory subCategory =
                        getSubCategory(transactionInput.subCategoryHash(), category);
                transaction.setAccountFrom(newAccountFrom);
                newAccountFrom.getTransactions().add(transaction);
                if (DateUtils.parse(transactionInput.date()).isAfter(LocalDateTime.now())) {
                    transaction.setFuture(true);
                } else {
                    subtractFromBalance(transaction.getAccountFrom(), transactionInput.amount());
                }
                transaction.setCategory(category);
                transaction.setSubCategory(subCategory);
            }
            case INCOME -> {
                Account newAccountTo = accountService.getAccount(transactionInput.accountToHash());
                Category category = categoryService.getCategory(transactionInput.categoryHash());
                if (!category.getIncome()) {
                    log.warn("category is not for \"income\"");
                    throw new AppException("incorrect category", HttpStatus.BAD_REQUEST);
                }
                SubCategory subCategory =
                        getSubCategory(transactionInput.subCategoryHash(), category);
                transaction.setAccountTo(newAccountTo);
                newAccountTo.getTransactions().add(transaction);
                if (DateUtils.parse(transactionInput.date()).isAfter(LocalDateTime.now())) {
                    transaction.setFuture(true);
                } else {
                    addToBalance(transaction.getAccountTo(), transactionInput.amount());
                }
                transaction.setCategory(category);
                transaction.setSubCategory(subCategory);
            }
            case TRANSFER -> {
                Account newAccountFrom =
                        accountService.getAccount(transactionInput.accountFromHash());
                Account newAccountTo = accountService.getAccount(transactionInput.accountToHash());
                transaction.setAccountFrom(newAccountFrom);
                newAccountFrom.getTransactions().add(transaction);
                transaction.setAccountTo(newAccountTo);
                newAccountTo.getTransactions().add(transaction);
                if (DateUtils.parse(transactionInput.date()).isAfter(LocalDateTime.now())) {
                    transaction.setFuture(true);
                } else {
                    subtractFromBalance(transaction.getAccountFrom(), transactionInput.amount());
                    addToBalance(transaction.getAccountTo(), transactionInput.amount());
                }
            }
        }

        return transactionRepository.save(transaction.toBuilder()
                .transactionType(transactionInput.transactionType())
                .amount(transactionInput.amount())
                .date(DateUtils.parse(transactionInput.date()))
                .name(transactionInput.name())
                .currency(transactionInput.currency())
                .need(transactionInput.need())
                .note(transactionInput.note())
                .build());
    }

    public List<Transaction> getFutureTransactions() {
        return transactionRepository.findAllByFutureTrue();
    }

    private Transaction handleCraeteIncomeTransaction(TransactionInput transactionInput) {
        Account accountTo = accountService.getAccount(transactionInput.accountToHash());
        Category category = categoryService.getCategory(transactionInput.categoryHash());
        if (!category.getIncome()) {
            log.warn("category is not for \"income\"");
            throw new AppException("incorrect category", HttpStatus.BAD_REQUEST);
        }
        SubCategory subCategory = getSubCategory(transactionInput.subCategoryHash(), category);
        Transaction transaction =
                Transaction.of(transactionInput, null, accountTo, category, subCategory);
        transaction = transactionRepository.save(transaction);
        accountTo.getTransactions().add(transaction);
        category.getTransactions().add(transaction);
        if (DateUtils.parse(transactionInput.date()).isAfter(LocalDateTime.now())) {
            transaction.setFuture(true);
        } else {
            addToBalance(accountTo, transaction.getAmount());
        }
        return transactionRepository.save(transaction);
    }

    private Transaction handleCreateTransferTransaction(TransactionInput transactionInput) {
        Account accountFrom = accountService.getAccount(transactionInput.accountFromHash());
        Account accountTo = accountService.getAccount(transactionInput.accountToHash());
        Transaction transaction =
                Transaction.of(transactionInput, accountFrom, accountTo, null, null);
        transaction = transactionRepository.save(transaction);
        accountTo.getTransactions().add(transaction);
        accountFrom.getTransactions().add(transaction);
        if (DateUtils.parse(transactionInput.date()).isAfter(LocalDateTime.now())) {
            transaction.setFuture(true);
        } else {
            subtractFromBalance(accountFrom, transaction.getAmount());
            addToBalance(accountTo, transaction.getAmount());
        }
        return transactionRepository.save(transaction);
    }

    private SubCategory getSubCategory(String hash, Category parent) {
        SubCategory subCategory = null;
        if (Objects.nonNull(hash)) {
            subCategory = categoryService.getSubCategory(hash);
            if (Objects.isNull(subCategory.getParent())) {
                log.warn("this category is not subCategory");
                throw new AppException("incorrect subCategory", HttpStatus.BAD_REQUEST);
            }
            if (!subCategory.getParent().equals(parent)) {
                log.warn("this subCategory is for different category");
                throw new AppException("incorrect subCategory", HttpStatus.BAD_REQUEST);
            }
        }
        return subCategory;
    }

    private Transaction handleCreateExpenseTransaction(TransactionInput transactionInput) {
        Account accountFrom = accountService.getAccount(transactionInput.accountFromHash());
        Category category = categoryService.getCategory(transactionInput.categoryHash());

        if (category.getIncome()) {
            log.warn("category is not for \"expense\"");
            throw new AppException("incorrect category", HttpStatus.BAD_REQUEST);
        }
        SubCategory subCategory = getSubCategory(transactionInput.subCategoryHash(), category);

        Transaction transaction =
                Transaction.of(transactionInput, accountFrom, null, category, subCategory);
        accountFrom.getTransactions().add(transaction);
        category.getTransactions().add(transaction);
        if (DateUtils.parse(transactionInput.date()).isAfter(LocalDateTime.now())) {
            transaction.setFuture(true);
        } else {
            subtractFromBalance(accountFrom, transaction.getAmount());
        }
        return transactionRepository.save(transaction);
    }

    @SneakyThrows
    private void validate(TransactionInput transactionInput) {
        validate(
                transactionInput.transactionType(),
                transactionInput.accountToHash(),
                transactionInput.accountFromHash(),
                transactionInput.categoryHash());
    }

    @SneakyThrows
    private void validate(
            TransactionType transactionType,
            String accountToHash,
            String accountFromHash,
            String categoryHash) {
        switch (transactionType) {
            case INCOME -> {
                if (Objects.isNull(accountToHash)) {
                    log.debug("account to can't be null when transactionType is Income");
                    throw new AppException("\"to\" can't be null", HttpStatus.BAD_REQUEST);
                }
                if (Objects.isNull(categoryHash)) {
                    log.debug("category to can't be null when transactionType is Income");
                    throw new AppException("category can't be null", HttpStatus.BAD_REQUEST);
                }
            }
            case EXPENSE -> {
                if (Objects.isNull(accountFromHash)) {
                    log.debug("account from can't be null when transactionType is Expense");
                    throw new AppException("\"from\" can't be null", HttpStatus.BAD_REQUEST);
                }
                if (Objects.isNull(categoryHash)) {
                    log.debug("category to can't be null when transactionType is Expense");
                    throw new AppException("category can't be null", HttpStatus.BAD_REQUEST);
                }
            }
            case TRANSFER -> {
                if (Objects.isNull(accountToHash)) {
                    log.debug("account to can't be null when transactionType is Transfer");
                    throw new AppException("account to can't be null", HttpStatus.BAD_REQUEST);
                }
                if (Objects.isNull(accountFromHash)) {
                    log.debug("account from to can't be null when transactionType is Transfer");
                    throw new AppException("\"from\" can't be null", HttpStatus.BAD_REQUEST);
                }
                if (Objects.equals(accountToHash, categoryHash)) {
                    log.debug("you can't transfer to the same account");
                    throw new AppException(
                            "\"from\" and \"to\" can't be the same", HttpStatus.BAD_REQUEST);
                }
            }
        }
    }

    private Transaction rollbackChanges(Transaction transaction) {
        switch (transaction.getTransactionType()) {
            case EXPENSE -> {
                Account account = transaction.getAccountFrom();
                transaction.setAccountFrom(null);
                transaction.setCategory(null);
                if (!transaction.getFuture()) {
                    addToBalance(account, transaction.getAmount());
                }
                account.removeTransaction(transaction);
            }
            case INCOME -> {
                Account account = transaction.getAccountTo();
                transaction.setAccountTo(null);
                transaction.setCategory(null);
                if (!transaction.getFuture()) {
                    subtractFromBalance(account, transaction.getAmount());
                }
                account.removeTransaction(transaction);
            }
            case TRANSFER -> {
                Account accountTo = transaction.getAccountTo();
                transaction.setAccountTo(null);
                accountTo.removeTransaction(transaction);

                Account accountFrom = transaction.getAccountFrom();
                transaction.setAccountFrom(null);
                accountFrom.removeTransaction(transaction);
                if (!transaction.getFuture()) {
                    subtractFromBalance(accountTo, transaction.getAmount());
                    addToBalance(accountFrom, transaction.getAmount());
                }
            }
        }
        return transaction;
    }

    public List<TransactionCategoryNameSum> sumTransactionAmountForCategoriesName(
            Boolean income, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.sumTransactionAmountForCategoriesNameAndUser(
                income, userService.getLoggedUser(), startDate, endDate);
    }

    public List<TransactionCategorySum> sumTransactionAmountForCategories(
            LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.sumTransactionAmountForCategoriesAndUser(
                userService.getLoggedUser(), startDate, endDate);
    }

    private void subtractFromBalance(Account account, Double amount) {
        account.setBalance(account.getBalance() - amount);
    }

    private void addToBalance(Account account, Double amount) {
        account.setBalance(account.getBalance() + amount);
    }

    public Optional<Double> getExpense(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.getExpense(startDate, endDate, userService.getLoggedUser());
    }

    public Optional<Double> getIncome(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.getIncome(startDate, endDate, userService.getLoggedUser());
    }

    public List<DayExpense> getExpensesPerDayOfTheWeek(
            LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.getExpensesPerDayOfTheWeek(
                startDate, endDate, userService.getLoggedUser());
    }

    public List<MonthExpense> getExpensesMonth(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.getExpensesMonth(
                startDate, endDate, userService.getLoggedUser());
    }
}
