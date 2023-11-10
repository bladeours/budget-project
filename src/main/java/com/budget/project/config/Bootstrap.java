package com.budget.project.config;

import com.budget.project.auth.service.AuthService;
import com.budget.project.model.db.*;
import com.budget.project.model.dto.request.AuthenticationRequest;
import com.budget.project.model.dto.request.input.AccountInput;
import com.budget.project.model.dto.request.input.CategoryInput;
import com.budget.project.model.dto.request.input.TransactionInput;
import com.budget.project.service.AccountService;
import com.budget.project.service.CategoryService;
import com.budget.project.service.TransactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("local")
public class Bootstrap implements ApplicationRunner {

    private final AuthService authService;
    private final AccountService accountService;
    private final AuthenticationManager authenticationManager;
    private final CategoryService categoryService;
    private final TransactionService transactionService;

    @Override
    public void run(ApplicationArguments args) {
        var authenticationRequest = new AuthenticationRequest("jd", "123");
        System.out.println(
                "JWTTOKEN: " + authService.register(authenticationRequest).jwt());
        var auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authenticationRequest.email(), authenticationRequest.password()));
        SecurityContextHolder.getContext().setAuthentication(auth);
        Account account1 = accountService.createAccount(AccountInput.builder()
                .accountType(AccountType.REGULAR)
                .balance(21.36)
                .currency(Currency.PLN)
                .color("#ffffff")
                .name("test_1")
                .description("test_1_desc")
                .build());
        accountService.createAccount(AccountInput.builder()
                .accountType(AccountType.SAVINGS)
                .balance(1.2)
                .currency(Currency.PLN)
                .color("33")
                .name("test_1_sub_1")
                .description("test_1_desc_sub_1")
                .parentHash(account1.getHash())
                .build());
        accountService.createAccount(AccountInput.builder()
                .accountType(AccountType.SAVINGS)
                .balance(14.21)
                .currency(Currency.PLN)
                .color("#ffffff")
                .name("test_2")
                .description("test_2_desc")
                .build());
        accountService.createAccount(AccountInput.builder()
                .accountType(AccountType.SAVINGS)
                .balance(14.21)
                .currency(Currency.PLN)
                .color("#ffffff")
                .name("test_3")
                .description("test_2_desc")
                .build());
        Category category1 = categoryService.createCategory(CategoryInput.builder()
                .income(false)
                .parentHash(null)
                .color("#ffffff")
                .name("category_1")
                .archived(false)
                .build());

        Transaction transaction1 = transactionService.createTransaction(TransactionInput.builder()
                .transactionType(TransactionType.EXPENSE)
                .accountFromHash(account1.getHash())
                .amount(10.0)
                .date("2023-11-01T15:20:10")
                .need(false)
                .categoryHash(category1.getHash())
                .currency(Currency.PLN)
                .name("transaction_1")
                .build());
        Transaction transaction2 = transactionService.createTransaction(TransactionInput.builder()
                .transactionType(TransactionType.EXPENSE)
                .accountFromHash(account1.getHash())
                .amount(10.0)
                .date("2022-11-01T15:20:10")
                .need(false)
                .categoryHash(category1.getHash())
                .currency(Currency.PLN)
                .name("transaction_2")
                .build());
        Transaction transaction3 = transactionService.createTransaction(TransactionInput.builder()
                .transactionType(TransactionType.EXPENSE)
                .accountFromHash(account1.getHash())
                .amount(10.0)
                .date("2023-10-01T15:20:10")
                .need(false)
                .categoryHash(category1.getHash())
                .currency(Currency.PLN)
                .name("transaction_3")
                .build());
    }
}
