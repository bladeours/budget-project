package com.budget.project.controller;

import com.budget.project.filter.model.Filter;
import com.budget.project.model.db.Account;
import com.budget.project.model.dto.request.CustomPage;
import com.budget.project.model.dto.request.input.AccountInput;
import com.budget.project.service.AccountService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @QueryMapping
    public Page<Account> getAccountsPage(@Argument CustomPage page, @Argument Filter filter) {
        return accountService.getAccountsPage(page, filter);
    }

    @QueryMapping
    public List<Account> getAccounts(@Argument Filter filter) {
        return accountService.getAccounts(filter);
    }

    @QueryMapping
    public Account getAccount(@Argument String hash) {
        return accountService.getAccount(hash);
    }

    @MutationMapping
    public Account addAccount(@Argument AccountInput accountInput) {
        return accountService.createAccount(accountInput);
    }

    @MutationMapping
    public Boolean deleteAccount(@Argument String hash, @Argument Boolean removeSub) {
        accountService.deleteAccount(hash, removeSub);
        return true;
    }

    @MutationMapping
    public Account updateAccount(@Argument String hash, @Argument AccountInput accountInput) {
        return accountService.updateAccount(hash, accountInput);
    }
}
