package com.budget.project.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.budget.project.auth.service.AuthService;
import com.budget.project.model.db.Account;
import com.budget.project.model.db.AccountType;
import com.budget.project.model.db.Currency;
import com.budget.project.model.dto.request.AccountInput;
import com.budget.project.model.dto.request.AuthenticationRequest;
import com.budget.project.service.AccountService;
import com.budget.project.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@AutoConfigureGraphQlTester
@SpringBootTest
public class AccountControllerTest {
    @Autowired private GraphQlTester graphQlTester;
    @Autowired private AccountService accountService;
    @Autowired private AuthService authService;
    @Autowired private AuthenticationManager authenticationManager;
    private ObjectMapper objectMapper = new ObjectMapper();
    Authentication auth_user_1;
    Authentication auth_user_2;

    @BeforeEach
    void setUp() {
        var authenticationRequest = new AuthenticationRequest("jd", "123");
        authService.register(authenticationRequest);
        auth_user_1 =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                authenticationRequest.email(), authenticationRequest.password()));
        authenticationRequest = new AuthenticationRequest("jd_2", "123");
        authService.register(authenticationRequest);
        auth_user_2 =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                authenticationRequest.email(), authenticationRequest.password()));
    }

    @Test
    void shouldCreateAccount_whenGetProperInput() {
        loginUser(1);
        AccountInput accountInput =
                AccountInput.builder()
                        .accountType(AccountType.REGULAR)
                        .balance(21.36)
                        .currency(Currency.PLN)
                        .color("#ffffff")
                        .name("test_1")
                        .description("test_1_desc")
                        .build();
        // language=GraphQL
        String mutation =
                """
                        mutation($accountInput: AccountInput!) {
                            addAccount(accountInput: $accountInput){
                                accountType
                                hash
                                name
                                balance
                            }
                        }
                        """;

        Account actualAccount =
                graphQlTester
                        .document(mutation)
                        .variable("accountInput", Utils.toMap(accountInput))
                        .execute()
                        .path("data.addAccount")
                        .entity(Account.class)
                        .get();

        Account expectedAccount = accountService.getAccount(actualAccount.getHash());

        assertAll(
                () -> assertThat(actualAccount.getHash()).isEqualTo(expectedAccount.getHash()),
                () ->
                        assertThat(actualAccount.getBalance())
                                .isEqualTo(expectedAccount.getBalance()),
                () -> assertThat(actualAccount.getName()).isEqualTo(expectedAccount.getName()));
    }

    private void loginUser(int i) {
        if (i == 1) {
            SecurityContextHolder.getContext().setAuthentication(auth_user_1);
        }
        if (i == 2) {
            SecurityContextHolder.getContext().setAuthentication(auth_user_2);
        }
    }
}
