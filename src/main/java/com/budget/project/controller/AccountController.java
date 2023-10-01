package com.budget.project.controller;

import com.budget.project.model.db.Account;
import com.budget.project.model.dto.request.AccountInput;
import com.budget.project.service.AccountService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    @QueryMapping
    public Set<Account> getAccounts() {
        return accountService.getAccounts();
    }

    @MutationMapping
    public Account addAccount(@Argument AccountInput accountInput) {
        return accountService.createAccount(accountInput);
    }

    @MutationMapping
    @PreAuthorize("'email@email.com'")
    public boolean deleteAccount(@Argument String hash) {
        accountService.deleteAccount(hash);
        return true;
    }
}
