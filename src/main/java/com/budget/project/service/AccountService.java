package com.budget.project.service;

import com.budget.project.exception.AppException;
import com.budget.project.filter.model.Filter;
import com.budget.project.filter.service.FilterService;
import com.budget.project.model.db.Account;
import com.budget.project.model.db.Transaction;
import com.budget.project.model.db.User;
import com.budget.project.model.dto.CustomPage;
import com.budget.project.model.dto.request.input.AccountInput;
import com.budget.project.service.repository.AccountRepository;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AccountService {
    private final UserService userService;
    private final AccountRepository accountRepository;
    private final FilterService filterService;
    private final TransactionService transactionService;

    @SneakyThrows
    public Account createAccount(AccountInput accountInput) {
        if (this.getAccountByName(accountInput.name()).isPresent()) {
            log.warn("There can not be two accounts with the same name: {}", accountInput.name());
            throw new AppException(
                    "There can not be two accounts with the same name", HttpStatus.BAD_REQUEST);
        }
        Account account = Account.of(accountInput, userService.getLoggedUser());
        account = accountRepository.save(account);
        User user = userService.getLoggedUser();
        user.getAccounts().add(account);

        return account;
    }

    public Page<Account> getAccountsPage(CustomPage page, Filter filter) {
        if (Objects.isNull(filter) || Objects.isNull(filter.logicOperator())) {
            return accountRepository.findAllByUsersContaining(
                    PageRequest.of(page.number(), page.size()), userService.getLoggedUser());
        }
        return accountRepository.findAll(
                filterService.getSpecification(filter, Account.class),
                PageRequest.of(page.number(), page.size()));
    }

    public List<Account> getAccounts(Filter filter) {
        if (Objects.isNull(filter) || Objects.isNull(filter.logicOperator())) {
            return accountRepository.findAllByUsersContaining(userService.getLoggedUser());
        }
        return accountRepository.findAll(filterService.getSpecification(filter, Account.class));
    }

    @SneakyThrows
    public Account getAccount(String hash) {
        return accountRepository
                .findByHashAndUsersContainingIgnoreCase(hash, userService.getLoggedUser())
                .orElseThrow(() -> {
                    log.warn("can't find account with hash: {}", hash);
                    return new AppException(
                            "can't find account with hash: " + hash, HttpStatus.NOT_FOUND);
                });
    }

    @SneakyThrows
    public Optional<Account> getAccountByName(String name) {
        return accountRepository.findByNameAndUsersContainingIgnoreCase(
                name, userService.getLoggedUser());
    }

    @SneakyThrows
    public void deleteAccount(String hash, Boolean removeSub) {
        Account account = this.getAccount(hash);
        for (Account child : account.getSubAccounts()) {
            if (removeSub) {
                deleteAccount(child.getHash(), false);
            } else {
                child.setParent(null);
            }
        }
        for (Transaction transaction : account.getTransactions()) {
            transactionService.deleteTransaction(transaction);
        }
        userService.getLoggedUser().getAccounts().remove(account);
        accountRepository.delete(account);
    }

    public Account updateAccount(String hash, AccountInput accountInput) {
        Account account = this.getAccount(hash);
        account = account.toBuilder()
                .accountType(accountInput.accountType())
                .archived(accountInput.archived())
                .description(accountInput.description())
                .balance(accountInput.balance())
                .color(accountInput.color())
                .name(accountInput.name())
                .currency(accountInput.currency())
                .build();
        return accountRepository.save(account);
    }

    private boolean parentChanged(String parentHash, Account account) {
        return (Objects.isNull(parentHash) && Objects.nonNull(account.getParent()))
                || (Objects.nonNull(account.getParent())
                        && !parentHash.equals(account.getParent().getHash()));
    }

    public List<Account> getTopAccounts() {
        return accountRepository.getTopAccounts(userService.getLoggedUser());
    }
}
