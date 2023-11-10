package com.budget.project.service;

import com.budget.project.exception.AppException;
import com.budget.project.filter.model.Filter;
import com.budget.project.filter.service.FilterService;
import com.budget.project.model.db.Account;
import com.budget.project.model.db.Category;
import com.budget.project.model.db.Transaction;
import com.budget.project.model.db.TransactionType;
import com.budget.project.model.dto.request.CustomPage;
import com.budget.project.model.dto.request.input.TransactionInput;
import com.budget.project.model.dto.request.input.TransactionUpdateInput;
import com.budget.project.service.repository.TransactionRepository;
import com.budget.project.utils.DateUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

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
                return handleExpenseTransaction(transactionInput);
            }
            case TRANSFER -> {
                return handleTransferTransaction(transactionInput);
            }
            case INCOME -> {
                return handleIncomeTransaction(transactionInput);
            }
            default -> {
                log.warn("unsupported operation");
                throw new AppException(HttpStatus.BAD_REQUEST);
            }
        }
    }

    private Transaction handleIncomeTransaction(TransactionInput transactionInput) {
        Account accountTo = accountService.getAccount(transactionInput.accountToHash());
        Category category = categoryService.getCategory(transactionInput.categoryHash());
        if (!category.getIncome()) {
            log.warn("category is not for \"income\"");
            throw new AppException("incorrect category", HttpStatus.BAD_REQUEST);
        }
        Transaction transaction = Transaction.of(transactionInput, null, accountTo, category);
        transaction = transactionRepository.save(transaction);
        accountTo.getTransactions().add(transaction);
        addToBalance(accountTo, transaction.getAmount());
        return transactionRepository.save(transaction);
    }

    private Transaction handleTransferTransaction(TransactionInput transactionInput) {
        Account accountFrom = accountService.getAccount(transactionInput.accountFromHash());
        Account accountTo = accountService.getAccount(transactionInput.accountToHash());
        Transaction transaction = Transaction.of(transactionInput, accountFrom, accountTo, null);
        transaction = transactionRepository.save(transaction);
        accountTo.getTransactions().add(transaction);
        accountFrom.getTransactions().add(transaction);
        subtractFromBalance(accountFrom, transaction.getAmount());
        addToBalance(accountTo, transaction.getAmount());
        return transactionRepository.save(transaction);
    }

    private Transaction handleExpenseTransaction(TransactionInput transactionInput) {
        Account accountFrom = accountService.getAccount(transactionInput.accountFromHash());
        Category category = categoryService.getCategory(transactionInput.categoryHash());
        if (category.getIncome()) {
            log.warn("category is not for \"expense\"");
            throw new AppException("incorrect category", HttpStatus.BAD_REQUEST);
        }
        Transaction transaction = Transaction.of(transactionInput, accountFrom, null, category);
        accountFrom.getTransactions().add(transaction);
        subtractFromBalance(accountFrom, transaction.getAmount());
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

    public Page<Transaction> getTransactionsPage(CustomPage page, Filter filter) {
        if (Objects.isNull(filter)) {
            return transactionRepository.findTransactionsForUser(
                    userService.getLoggedUser(), PageRequest.of(page.number(), page.size()));
        }
        return transactionRepository.findAll(
                filterService.getSpecification(filter, Transaction.class),
                PageRequest.of(page.number(), page.size()));
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

    private void subtractFromBalance(Account account, Double amount) {
        account.setBalance(account.getBalance() - amount);
    }

    private void addToBalance(Account account, Double amount) {
        account.setBalance(account.getBalance() + amount);
    }

    public Transaction updateTransaction(
            String hash, TransactionUpdateInput transactionUpdateInput) {
        Transaction transaction = this.getTransaction(hash);
        validate(
                transaction.getTransactionType(),
                transactionUpdateInput.accountToHash(),
                transactionUpdateInput.accountFromHash(),
                transactionUpdateInput.categoryHash());
        switch (transaction.getTransactionType()) {
            case EXPENSE -> {
                Account newAccountFrom =
                        accountService.getAccount(transactionUpdateInput.accountFromHash());
                Category category =
                        categoryService.getCategory(transactionUpdateInput.categoryHash());
                if (category.getIncome()) {
                    log.warn("category is not for \"expense\"");
                    throw new AppException("incorrect category", HttpStatus.BAD_REQUEST);
                }

                if (!newAccountFrom.equals(transaction.getAccountFrom())) {
                    transaction.getAccountFrom().removeTransaction(transaction);
                    addToBalance(transaction.getAccountFrom(), transaction.getAmount());
                    transaction.setAccountFrom(newAccountFrom);
                    newAccountFrom.getTransactions().add(transaction);
                    subtractFromBalance(newAccountFrom, transaction.getAmount());
                }

                if (!transactionUpdateInput.amount().equals(transaction.getAmount())) {
                    subtractFromBalance(
                            transaction.getAccountFrom(),
                            transactionUpdateInput.amount() - transaction.getAmount());
                }

                if (!category.equals(transaction.getCategory())) {
                    transaction.setCategory(category);
                }
            }
        }

        return transactionRepository.save(transaction.toBuilder()
                .amount(transactionUpdateInput.amount())
                .date(DateUtils.parse(transactionUpdateInput.date()))
                .name(transactionUpdateInput.name())
                .currency(transactionUpdateInput.currency())
                .need(transactionUpdateInput.need())
                .note(transactionUpdateInput.note())
                .build());
    }
}
