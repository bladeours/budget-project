package com.budget.project.service;

import com.budget.project.exception.AppException;
import com.budget.project.model.db.Account;
import com.budget.project.model.db.Category;
import com.budget.project.model.db.Transaction;
import com.budget.project.model.dto.request.TransactionInput;
import com.budget.project.service.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final AccountService accountService;
    private final CategoryService categoryService;

    //    public List<Transaction> getTransactions(Page page) {
    //        return transactionRepository.findTransactionForUser(
    //                userService.getLoggedUser(), PageRequest.of(page.number(), page.size()));
    //    }

    @SneakyThrows
    public Transaction createTransaction(TransactionInput transactionInput) {
        validate(transactionInput);
        Account accountFrom = null;
        Account accountTo = null;
        Category category = null;
        switch (transactionInput.transactionType()) {
            case EXPENSE -> {
                accountFrom = accountService.getAccount(transactionInput.accountFromHash());
                category = categoryService.getCategory(transactionInput.categoryHash());
                if (category.getIncome()) {
                    log.debug("category is not for \"expense\"");
                    throw new AppException("incorrect category", HttpStatus.BAD_REQUEST);
                }
            }
            case TRANSFER -> {
                accountFrom = accountService.getAccount(transactionInput.accountFromHash());
                accountTo = accountService.getAccount(transactionInput.accountToHash());
            }
            case INCOME -> {
                accountTo = accountService.getAccount(transactionInput.accountToHash());
                category = categoryService.getCategory(transactionInput.categoryHash());
                if (!category.getIncome()) {
                    log.debug("category is not for \"income\"");
                    throw new AppException("incorrect category", HttpStatus.BAD_REQUEST);
                }
            }
        }

        Transaction transaction =
                Transaction.of(transactionInput, accountFrom, accountTo, category);
        transaction = transactionRepository.save(transaction);
        if (Objects.nonNull(accountFrom)) {
            accountFrom.getTransactions().add(transaction);
        }
        if (Objects.nonNull(accountTo)) {
            accountTo.getTransactions().add(transaction);
        }
        return transaction;
    }

    @SneakyThrows
    private void validate(TransactionInput transactionInput) {
        switch (transactionInput.transactionType()) {
            case INCOME -> {
                if (Objects.isNull(transactionInput.accountToHash())) {
                    log.debug("account to can't be null when transactionType is Income");
                    throw new AppException("\"to\" can't be null", HttpStatus.BAD_REQUEST);
                }
                if (Objects.isNull(transactionInput.categoryHash())) {
                    log.debug("category to can't be null when transactionType is Income");
                    throw new AppException("category can't be null", HttpStatus.BAD_REQUEST);
                }
            }
            case EXPENSE -> {
                if (Objects.isNull(transactionInput.accountFromHash())) {
                    log.debug("account from can't be null when transactionType is Expense");
                    throw new AppException("\"from\" can't be null", HttpStatus.BAD_REQUEST);
                }
                if (Objects.isNull(transactionInput.categoryHash())) {
                    log.debug("category to can't be null when transactionType is Expense");
                    throw new AppException("category can't be null", HttpStatus.BAD_REQUEST);
                }
            }
            case TRANSFER -> {
                if (Objects.isNull(transactionInput.accountToHash())) {
                    log.debug("account to can't be null when transactionType is Transfer");
                    throw new AppException("account to can't be null", HttpStatus.BAD_REQUEST);
                }
                if (Objects.isNull(transactionInput.accountFromHash())) {
                    log.debug("account from to can't be null when transactionType is Transfer");
                    throw new AppException("\"from\" can't be null", HttpStatus.BAD_REQUEST);
                }
            }
        }
    }
}
