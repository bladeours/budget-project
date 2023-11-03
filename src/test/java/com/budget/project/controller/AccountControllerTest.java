package com.budget.project.controller;

import static com.budget.project.utils.TestUtils.errorTypeEquals;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.budget.project.auth.service.AuthService;
import com.budget.project.exception.AppException;
import com.budget.project.model.db.*;
import com.budget.project.model.dto.request.AccountInput;
import com.budget.project.model.dto.request.AuthenticationRequest;
import com.budget.project.model.dto.request.TransactionInput;
import com.budget.project.service.AccountService;
import com.budget.project.service.CategoryService;
import com.budget.project.service.TransactionService;
import com.budget.project.service.repository.UserRepository;
import com.budget.project.utils.TestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.annotation.DirtiesContext;

@AutoConfigureGraphQlTester
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AccountControllerTest {
    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TransactionService transactionService;

    Authentication auth_user_1;
    Authentication auth_user_2;

    @BeforeEach
    void setUp() {
        if (userRepository.findByEmail("jd").isEmpty()) {
            var authenticationRequest = new AuthenticationRequest("jd", "123");
            authService.register(authenticationRequest);
            auth_user_1 =
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                            authenticationRequest.email(), authenticationRequest.password()));
        }
        if (userRepository.findByEmail("jd_2").isEmpty()) {
            var authenticationRequest = new AuthenticationRequest("jd_2", "123");
            authService.register(authenticationRequest);
            auth_user_2 =
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                            authenticationRequest.email(), authenticationRequest.password()));
        }
    }

    @Test
    void shouldCreateAccount_whenGetProperInput() {
        login("jd");
        AccountInput accountInput = AccountInput.builder()
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

        Account actualAccount = graphQlTester
                .document(mutation)
                .variable("accountInput", TestUtils.toMap(accountInput))
                .execute()
                .path("data.addAccount")
                .entity(Account.class)
                .get();

        Account expectedAccount = accountService.getAccount(actualAccount.getHash());

        assertAll(
                () -> assertThat(actualAccount.getHash()).isEqualTo(expectedAccount.getHash()),
                () -> assertThat(actualAccount.getBalance())
                        .isEqualTo(expectedAccount.getBalance()),
                () -> assertThat(actualAccount.getName()).isEqualTo(expectedAccount.getName()));
    }

    @Test
    void shouldGetAccount_whenGetProperRequest() {
        login("jd");
        AccountInput accountInput = AccountInput.builder()
                .accountType(AccountType.REGULAR)
                .balance(21.36)
                .currency(Currency.PLN)
                .color("#ffffff")
                .name("test_1")
                .description("test_1_desc")
                .build();

        Account expectedAccount = accountService.createAccount(accountInput);
        // language=GraphQL
        String query =
                """
				query($hash: String!) {
				    getAccount(hash: $hash){
				        hash
				        name
				        balance
				    }
				}
				""";

        Account actualAccount = graphQlTester
                .document(query)
                .variable("hash", expectedAccount.getHash())
                .execute()
                .path("data.getAccount")
                .entity(Account.class)
                .get();

        assertAll(
                () -> assertThat(actualAccount.getHash()).isEqualTo(expectedAccount.getHash()),
                () -> assertThat(actualAccount.getBalance())
                        .isEqualTo(expectedAccount.getBalance()),
                () -> assertThat(actualAccount.getName()).isEqualTo(expectedAccount.getName()));
    }

    @Test
    void shouldNotGetAccount_whenAccountIsForDifferentUser() {
        login("jd");
        AccountInput accountInput = AccountInput.builder()
                .accountType(AccountType.REGULAR)
                .balance(21.36)
                .currency(Currency.PLN)
                .color("#ffffff")
                .name("test_1")
                .description("test_1_desc")
                .build();

        Account expectedAccount = accountService.createAccount(accountInput);
        // language=GraphQL
        String query =
                """
				query($hash: String!) {
				    getAccount(hash: $hash){
				        hash
				        name
				        balance
				    }
				}
				""";
        login("jd_2");
        graphQlTester
                .document(query)
                .variable("hash", expectedAccount.getHash())
                .execute()
                .errors()
                .expect(errorTypeEquals(ErrorType.NOT_FOUND));
    }

    @Test
    void shouldGetAccountsPage_whenGetProperRequest() {
        login("jd_2");
        Account expectedAccount_1 = accountService.createAccount(getAccountInput("name_1"));
        login("jd");
        Account expectedAccount_2 = accountService.createAccount(getAccountInput("name_2"));
        Account expectedAccount_3 = accountService.createAccount(getAccountInput("name_3"));
        Account expectedAccount_4 = accountService.createAccount(getAccountInput("name_4"));
        // language=GraphQL
        String query =
                """
                query {
                    getAccountsPage(page: {number: 0, size: 2}){
                    content {
                        hash
                        name
                        balance
                      }
                    }
                }
                """;
        graphQlTester
                .document(query)
                .execute()
                .path("data.getAccountsPage")
                .matchesJson(
                        String.format(
                                """
                      {
                        "content": [
                          {
                            "hash": %s,
                            "name": "name_2",
                            "balance": 21.36
                          },
                          {
                            "hash": %s,
                            "name": "name_3",
                            "balance": 21.36
                          }
                        ]
                      }

                  """,
                                expectedAccount_2.getHash(), expectedAccount_3.getHash())
                        );
        query =
                """
                query {
                    getAccountsPage(page: {number: 1, size: 2}){
                    content {
                        hash
                        name
                        balance
                      }
                    }
                }
                """;
        graphQlTester
                .document(query)
                .execute()
                .path("data.getAccountsPage")
                .matchesJson(
                        String.format(
                                """
                      {
                        "content": [
                          {
                            "hash": %s,
                            "name": "name_4",
                            "balance": 21.36
                          }
                        ]
                      }

                  """, expectedAccount_4.getHash())
                );

    }

    @Test
    void shouldRemoveAccount_whenGetProperInput() {
        login("jd");
        Account accountTo = accountService.createAccount(getAccountInput("test"));
        Account accountFrom = accountService.createAccount(getAccountInput("test"));
        Transaction transaction = transactionService.createTransaction(getTransactionInput(accountTo.getHash(), accountFrom.getHash()));
        // language=GraphQL
        String mutation =
                """
				mutation($hash: String!) {
				    deleteAccount(hash: $hash, removeSub: false)
				}
				""";

        graphQlTester
                .document(mutation)
                .variable("hash", accountFrom.getHash())
                .execute();

        assertAll(
                () -> assertThrows(AppException.class, () -> accountService.getAccount(accountFrom.getHash())),
                () -> assertThrows(AppException.class, () -> transactionService.getTransaction(transaction.getHash())));
    }

    @Test
    void shouldRemoveAccountAndSubAccounts_whenGetProperInput() {
        login("jd");
        Account accountTo = accountService.createAccount(getAccountInput("test"));
        Account accountFrom = accountService.createAccount(getAccountInput("test"));
        Transaction transaction = transactionService.createTransaction(getTransactionInput(accountTo.getHash(), accountFrom.getHash()));
        // language=GraphQL
        String mutation =
                """
				mutation($hash: String!) {
				    deleteAccount(hash: $hash, removeSub: false)
				}
				""";

        graphQlTester
                .document(mutation)
                .variable("hash", accountFrom.getHash())
                .execute();

        assertAll(
                () -> assertThrows(AppException.class, () -> accountService.getAccount(accountFrom.getHash())),
                () -> assertThrows(AppException.class, () -> transactionService.getTransaction(transaction.getHash())));
    }

    private void login(String email) {
        authService.authenticate(new AuthenticationRequest(email, "123"));
    }
    private AccountInput getAccountInput(String name) {
        return AccountInput.builder()
                .accountType(AccountType.REGULAR)
                .balance(21.36)
                .currency(Currency.PLN)
                .color("#ffffff")
                .name(name)
                .description("test_1_desc")
                .build();
    }

    private TransactionInput getTransactionInput(String accountTo, String accountFrom) {
        return TransactionInput.builder()
                .transactionType(TransactionType.TRANSFER)
                .accountFromHash(accountFrom)
                .amount(10.0)
                .date("2022-11-01T15:20:10")
                .need(false)
                .accountToHash(accountTo)
                .currency(Currency.PLN)
                .name("transaction_2")
                .build();
    }
}
