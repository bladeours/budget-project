package com.budget.project.controller;

import com.budget.project.filter.model.AccountFilter;
import com.budget.project.model.db.Account;
import com.budget.project.model.dto.request.AccountInput;
import com.budget.project.model.dto.request.Page;
import com.budget.project.service.AccountService;
import graphql.schema.DataFetchingEnvironment;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.jpa.domain.Specification;
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
    public List<Account> getAccounts(
            @Argument Page page, DataFetchingEnvironment env, @Argument AccountFilter filter) {
        getSpecification(filter);
        return null;
        //        return accountService.getAccounts(page, getSpecification(filter));
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

    @SneakyThrows
    private Specification<Account> getSpecification(AccountFilter filter) {
        //        System.out.println(filter.getClass().getDeclaredFields());
        Arrays.stream(filter.getClass().getDeclaredFields())
                .peek(System.out::println)
                .peek(f -> f.setAccessible(true))
                .peek(System.out::println)
                .map(f -> getFieldValue(f, filter))
                .peek(f -> System.out.println(f));

        return null;
    }

    @SneakyThrows
    private Object getFieldValue(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
