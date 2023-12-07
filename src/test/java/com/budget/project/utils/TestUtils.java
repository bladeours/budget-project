package com.budget.project.utils;

import com.budget.project.auth.model.dto.AuthInput;
import com.budget.project.auth.service.AuthService;
import com.budget.project.model.db.AccountType;
import com.budget.project.model.db.Currency;
import com.budget.project.model.db.TransactionType;
import com.budget.project.model.dto.request.input.AccountInput;
import com.budget.project.model.dto.request.input.CategoryInput;
import com.budget.project.model.dto.request.input.TransactionInput;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.graphql.ResponseError;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class TestUtils {

    public static final String USER_1 = "jd";
    public static final String USER_2 = "jd_2";

    public static Map<String, Object> toMap(Object object) {
        return new ObjectMapper().convertValue(object, new TypeReference<>() {});
    }

    public static Predicate<ResponseError> errorTypeEquals(ErrorType errorType) {
        return responseError -> {
            if (responseError != null) {
                return responseError.getErrorType().equals(errorType);
            }
            return false;
        };
    }

    public static void login(String email, AuthService authService) {
        authService.authenticate(new AuthInput(email, "123"));
    }

    public static AccountInput getAccountInput(String name) {
        return AccountInput.builder()
                .accountType(AccountType.REGULAR)
                .balance(21.36)
                .currency(Currency.PLN)
                .color("#ffffff")
                .archived(false)
                .name(name)
                .description("test_1_desc")
                .build();
    }

    public static TransactionInput getTransactionInputTransfer(
            String accountTo, String accountFrom) {
        return TransactionInput.builder()
                .transactionType(TransactionType.TRANSFER)
                .accountFromHash(accountFrom)
                .amount(10.0)
                .date(OffsetDateTime.now().toString())
                .need(false)
                .accountToHash(accountTo)
                .currency(Currency.PLN)
                .name("transaction_2")
                .build();
    }

    public static TransactionInput getTransactionInputIncome(String category, String accountFrom) {
        return TransactionInput.builder()
                .transactionType(TransactionType.INCOME)
                .accountToHash(accountFrom)
                .amount(10.0)
                .date(OffsetDateTime.now().toString())
                .need(false)
                .categoryHash(category)
                .currency(Currency.PLN)
                .name("transaction_2")
                .build();
    }

    public static TransactionInput getTransactionInputIncome(
            String category, String accountFrom, String subCategory) {
        return TransactionInput.builder()
                .transactionType(TransactionType.INCOME)
                .accountToHash(accountFrom)
                .amount(10.0)
                .subCategoryHash(subCategory)
                .date(OffsetDateTime.now().toString())
                .need(false)
                .categoryHash(category)
                .currency(Currency.PLN)
                .name("transaction_2")
                .build();
    }

    public static TransactionInput getTransactionInputExpense(
            String categoryHash, String accountFrom) {
        return TransactionInput.builder()
                .transactionType(TransactionType.EXPENSE)
                .accountFromHash(accountFrom)
                .amount(10.0)
                .date(OffsetDateTime.now().toString())
                .need(false)
                .categoryHash(categoryHash)
                .currency(Currency.PLN)
                .name("transaction_2")
                .build();
    }

    public static CategoryInput getCategoryInput(Boolean income) {
        return CategoryInput.builder()
                .income(income)
                .subCategories(List.of())
                .color("33")
                .name("name")
                .build();
    }

    public static void registerUsers(
            AuthService authService, AuthenticationManager authenticationManager) {
        var authenticationRequest = new AuthInput(USER_1, "123");
        authService.register(authenticationRequest);
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authenticationRequest.email(), authenticationRequest.password()));
        authenticationRequest = new AuthInput(USER_2, "123");
        authService.register(authenticationRequest);
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authenticationRequest.email(), authenticationRequest.password()));
    }
}
