package com.budget.project.service;

import com.budget.project.exception.AppException;
import com.budget.project.filter.model.Filter;
import com.budget.project.filter.service.FilterService;
import com.budget.project.model.db.Account;
import com.budget.project.model.db.Transaction;
import com.budget.project.model.db.User;
import com.budget.project.model.dto.request.CustomPage;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
        Account account;
        if (Objects.nonNull(accountInput.parentHash())) {
            Account parent = this.getAccount(accountInput.parentHash());
            if (Objects.nonNull(parent.getParent())) {
                log.warn("only one level of subAccounts is possible");
                throw new AppException(
                        "only one level of subAccounts is possible", HttpStatus.BAD_REQUEST);
            }
            if (!loggedUserHasAccess(parent)) {
                log.debug(
                        "{} doesn't have access to parent: {}",
                        userService.getLoggedUser().getEmail(),
                        parent.getHash());
                throw new AppException(HttpStatus.FORBIDDEN);
            }
            account = Account.of(accountInput, userService.getLoggedUser(), parent);
            account = accountRepository.save(account);
            if(Objects.isNull(parent.getSubAccounts())){
                parent.setSubAccounts( List.of(account));
            }else {
                parent.getSubAccounts().add(account);
            }
        } else {
            account = Account.of(accountInput, userService.getLoggedUser());
            account = accountRepository.save(account);
        }
        User user = userService.getLoggedUser();
        user.getAccounts().add(account);

        return account;
    }

    public Page<Account> getAccountsPage(CustomPage page, Filter filter) {
        if (Objects.isNull(filter)) {
            return accountRepository.findAllByUsersContaining(
                    PageRequest.of(page.number(), page.size()), userService.getLoggedUser());
        }
        return accountRepository.findAll(
                filterService.getSpecification(filter, Account.class),
                PageRequest.of(page.number(), page.size()));
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
    public void deleteAccount(String hash, Boolean removeSub) {
        Account account = this.getAccount(hash);
        if (!loggedUserHasAccess(account)) {
            log.debug(
                    "{} doesn't have access to account: {}",
                    userService.getLoggedUser().getEmail(),
                    account.getHash());
            throw new AppException(HttpStatus.FORBIDDEN);
        }

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

    private Boolean loggedUserHasAccess(Account account) {
        return account.getUsers().contains(userService.getLoggedUser());
    }

    public Account updateAccount(String hash, AccountInput accountInput) {
        Account account = this.getAccount(hash);
        if (parentChanged(accountInput.parentHash(), account)) {
            if (Objects.nonNull(account.getParent())) {
                account.getParent().getSubAccounts().remove(account);
            }
            Account newParent = this.getAccount(accountInput.parentHash());
            account.setParent(newParent);
            newParent.getSubAccounts().add(account);
        }

        account = account.toBuilder()
                .accountType(accountInput.accountType())
                .archived(accountInput.archived())
                .description(accountInput.description())
                .balance(accountInput.balance())
                .color(accountInput.color())
                .name(accountInput.name())
                .currency(accountInput.currency())
                .build();
        accountRepository.save(account);
        return account;
    }

    private boolean parentChanged(String parentHash, Account account) {
        return(Objects.isNull(parentHash) && Objects.nonNull(account.getParent())) || (Objects.nonNull(account.getParent())
                        && !parentHash.equals(account.getParent().getHash()));
    }
}
