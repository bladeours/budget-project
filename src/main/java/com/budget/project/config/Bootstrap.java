package com.budget.project.config;

import com.budget.project.auth.model.dto.AuthInput;
import com.budget.project.auth.service.AuthService;
import com.budget.project.model.db.*;
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
        var authenticationRequest = new AuthInput("jd", "123");
        System.out.println(
                "JWTTOKEN: " + authService.register(authenticationRequest).jwt());
        var auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authenticationRequest.email(), authenticationRequest.password()));
        SecurityContextHolder.getContext().setAuthentication(auth);
        Account account1 = accountService.createAccount(AccountInput.builder()
                .accountType(AccountType.REGULAR)
                .balance(21.36)
                .currency(Currency.PLN)
                .color("#FFCE30")
                .name("Santander")
                .description("Main account")
                .build());
        var account2 = accountService.createAccount(AccountInput.builder()
                .accountType(AccountType.SAVINGS)
                .balance(1.2)
                .currency(Currency.PLN)
                .color("#FFCE30")
                .name("Santander - saving")
                .description("savings")
                .parentHash(account1.getHash())
                .build());

        var account3 = accountService.createAccount(AccountInput.builder()
                .accountType(AccountType.SAVINGS)
                .balance(1.2)
                .currency(Currency.PLN)
                .color("#746AB0")
                .name("deposit")
                .archived(true)
                .description("just an deposit account")
                .parentHash(account1.getHash())
                .build());
        accountService.createAccount(AccountInput.builder()
                .accountType(AccountType.REGULAR)
                .balance(14.21)
                .currency(Currency.PLN)
                .color("#E389B9")
                .name("mBank")
                .description("secondary account")
                .build());
        accountService.createAccount(AccountInput.builder()
                .accountType(AccountType.REGULAR)
                .balance(14.21)
                .currency(Currency.PLN)
                .color("#E389B9")
                .name("AliorBank")
                .description("third account")
                .build());
        Category category1 = categoryService.createCategory(CategoryInput.builder()
                .income(true)
                .parentHash(null)
                .color("#746AB0")
                .name("Salary")
                .archived(false)
                .build());

        Category category1_1 = categoryService.createCategory(CategoryInput.builder()
                .income(true)
                .parentHash(category1.getHash())
                .color("#ffffff")
                .name("Glovo")
                .archived(false)
                .build());

        Category category1_2 = categoryService.createCategory(CategoryInput.builder()
                .income(true)
                .parentHash(category1.getHash())
                .color("#ffffff")
                .name("Atos")
                .archived(false)
                .build());

        Category category2 = categoryService.createCategory(CategoryInput.builder()
                .income(true)
                .parentHash(null)
                .color("#ffffff")
                .name("Other")
                .archived(false)
                .build());

        Category category3 = categoryService.createCategory(CategoryInput.builder()
                .income(false)
                .parentHash(null)
                .color("#ffffff")
                .name("Food")
                .archived(false)
                .build());

        Category category4 = categoryService.createCategory(CategoryInput.builder()
                .income(false)
                .parentHash(category3.getHash())
                .color("#ffffff")
                .name("Restaurant")
                .archived(false)
                .build());

        Category category5 = categoryService.createCategory(CategoryInput.builder()
                .income(false)
                .parentHash(category3.getHash())
                .color("#ffffff")
                .name("Grocery")
                .archived(false)
                .build());

        Category category6 = categoryService.createCategory(CategoryInput.builder()
                .income(false)
                .color("#ffffff")
                .name("Entertainment")
                .archived(false)
                .build());


        Transaction transaction1 = transactionService.createTransaction(TransactionInput.builder()
                .transactionType(TransactionType.EXPENSE)
                .accountFromHash(account1.getHash())
                .amount(7.50)
                .date("2023-11-01T15:20:10.000Z")
                .need(false)
                .categoryHash(category3.getHash())
                .subCategoryHash(category5.getHash())
                .currency(Currency.PLN)
                .name("transaction_1")
                .build());
        Transaction transaction2 = transactionService.createTransaction(TransactionInput.builder()
                .transactionType(TransactionType.EXPENSE)
                .accountFromHash(account1.getHash())
                .amount(10.40)
                .date("2022-11-01T15:20:10Z")
                .need(false)
                .categoryHash(category5.getHash())
                .currency(Currency.PLN)
                .name("transaction_2")
                .build());
        Transaction transaction3 = transactionService.createTransaction(TransactionInput.builder()
                .transactionType(TransactionType.EXPENSE)
                .accountFromHash(account1.getHash())
                .amount(21.12)
                .date("2023-10-01T15:20:10Z")
                .need(false)
                .categoryHash(category5.getHash())
                .currency(Currency.PLN)
                .name("transaction_3")
                .build());

        Transaction transaction4 = transactionService.createTransaction(TransactionInput.builder()
                .transactionType(TransactionType.TRANSFER)
                .accountFromHash(account1.getHash())
                .accountToHash(account2.getHash())
                .amount(21.12)
                .date("2023-10-01T15:20:10Z")
                .need(false)
                .currency(Currency.PLN)
                .name("transaction_3")
                .build());

        Transaction transaction5 = transactionService.createTransaction(TransactionInput.builder()
                .transactionType(TransactionType.INCOME)
                .accountToHash(account1.getHash())
                .amount(75.42)
                .date("2023-10-01T15:20:10Z")
                .need(false)
                .categoryHash(category1.getHash())
                .currency(Currency.PLN)
                .name("transaction_3")
                .build());

        for (int i=0; i<50; i++){
            transactionService.createTransaction(TransactionInput.builder()
                    .transactionType(TransactionType.INCOME)
                    .accountToHash(account1.getHash())
                    .amount(75.42)
                    .date("2023-10-01T15:20:10Z")
                    .need(false)
                    .categoryHash(category1.getHash())
                    .currency(Currency.PLN)
                    .name("transaction_3")
                    .build());
        }
    }
}
