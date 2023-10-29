package com.budget.project.controller;

import com.budget.project.filter.AccountFilter;
import com.budget.project.model.db.Account;
import com.budget.project.model.dto.request.AccountInput;
import com.budget.project.model.dto.request.CustomPage;
import com.budget.project.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    @QueryMapping
    public Page<Account> getAccountsPage(
            @Argument CustomPage page, @Argument("filter") AccountFilter filterObject) {
        return accountService.getAccounts(page, filterObject);
    }

    @MutationMapping
    public Account addAccount(@Argument AccountInput accountInput) {
        return accountService.createAccount(accountInput);
    }

    @MutationMapping
    public boolean deleteAccount(@Argument String hash) {
        accountService.deleteAccount(hash);
        return true;
    }
}
