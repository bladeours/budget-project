package com.budget.project.controller;

import com.budget.project.auth.service.AuthService;
import com.budget.project.exception.AppException;
import com.budget.project.model.db.Account;
import com.budget.project.model.db.AccountType;
import com.budget.project.model.db.Currency;
import com.budget.project.model.db.Transaction;
import com.budget.project.model.dto.request.input.AccountInput;
import com.budget.project.service.AccountService;
import com.budget.project.service.TransactionService;
import com.budget.project.utils.TestUtils;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.budget.project.utils.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.graphql.execution.ErrorType.BAD_REQUEST;
import static org.springframework.graphql.execution.ErrorType.NOT_FOUND;

@AutoConfigureGraphQlTester
@SpringBootTest
@ActiveProfiles("test")
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
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        registerUsers(authService, authenticationManager);
    }

    @Test
    void shouldCreateAccount_whenGetProperInput() {
        login("jd", authService);
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
                .variable("accountInput", TestUtils.toMap(getAccountInput("")))
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
    void shouldReturnError_whenMoreThanOneLevelSubAccount() {
        login("jd", authService);
        Account accountParent = accountService.createAccount(getAccountInput("parent"));
        Account accountChild = accountService.createAccount(getAccountInput("child").toBuilder()
                .parentHash(accountParent.getHash())
                .build());
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

        graphQlTester
                .document(mutation)
                .variable(
                        "accountInput",
                        TestUtils.toMap(getAccountInput("name").toBuilder()
                                .parentHash(accountChild.getHash())
                                .build()))
                .execute()
                .errors()
                .expect(errorTypeEquals(BAD_REQUEST));
    }

    @Test
    void shouldGetAccount_whenGetProperRequest() {
        login("jd", authService);

        Account expectedAccount = accountService.createAccount(getAccountInput(""));
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
        login("jd", authService);

        Account expectedAccount = accountService.createAccount(getAccountInput(""));
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
        login("jd_2", authService);
        graphQlTester
                .document(query)
                .variable("hash", expectedAccount.getHash())
                .execute()
                .errors()
                .expect(errorTypeEquals(NOT_FOUND));
    }

    @Test
    void shouldGetAccountsPage_whenGetProperRequest() {
        login("jd_2", authService);
        accountService.createAccount(getAccountInput("name_1"));
        login("jd", authService);
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
                .matchesJson(String.format(
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
                        expectedAccount_2.getHash(), expectedAccount_3.getHash()));
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
                .matchesJson(String.format(
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

                  """,
                        expectedAccount_4.getHash()));
    }

    @Test
    void shouldRemoveAccount_whenGetProperInput() {
        login("jd", authService);
        Account accountTo = accountService.createAccount(getAccountInput("test"));
        Account accountFrom = accountService.createAccount(getAccountInput("test"));
        Transaction transaction = transactionService.createTransaction(
                getTransactionInputTransfer(accountTo.getHash(), accountFrom.getHash()));
        // language=GraphQL
        String mutation =
                """
				mutation($hash: String!) {
				    deleteAccount(hash: $hash, removeSub: false)
				}
				""";

        graphQlTester.document(mutation).variable("hash", accountFrom.getHash()).execute();

        assertAll(
                () -> assertThrows(
                        AppException.class, () -> accountService.getAccount(accountFrom.getHash())),
                () -> assertThrows(
                        AppException.class,
                        () -> transactionService.getTransaction(transaction.getHash())));
    }

    @Test
    void shouldRemoveAccountAndSubAccounts_whenGetProperInput() {
        login("jd", authService);
        Account accountParent = accountService.createAccount(getAccountInput("parent"));
        Account accountChild = accountService.createAccount(getAccountInput("child").toBuilder()
                .parentHash(accountParent.getHash())
                .build());

        // language=GraphQL
        String mutation =
                """
				mutation($hash: String!) {
				    deleteAccount(hash: $hash, removeSub: true)
				}
				""";

        graphQlTester
                .document(mutation)
                .variable("hash", accountParent.getHash())
                .execute();

        assertAll(
                () -> assertThrows(
                        AppException.class,
                        () -> accountService.getAccount(accountParent.getHash())),
                () -> assertThrows(
                        AppException.class,
                        () -> accountService.getAccount(accountChild.getHash())));
    }

    @Test
    void shouldUpdateAccount_whenGetProperInput() {
        login("jd", authService);
        Account account = accountService.createAccount(getAccountInput("name_1"));

        AccountInput accountInput = AccountInput.builder()
                .archived(account.getArchived())
                .accountType(account.getAccountType())
                .color(account.getColor())
                .currency(account.getCurrency())
                .description(account.getDescription())
                .name("new_name")
                .balance(31.13)
                .build();
        // language=Graphql
        String mutation =
                """
				mutation($accountInput: AccountInput!, $hash: String!) {
				    updateAccount(hash: $hash, accountInput: $accountInput){
				        name
				        balance
				    }
				}
				""";

        graphQlTester
                .document(mutation)
                .variable("accountInput", TestUtils.toMap(accountInput))
                .variable("hash", account.getHash())
                .execute();

        Account actualAccount = accountService.getAccount(account.getHash());

        assertAll(
                () -> assertThat(actualAccount.getName()).isEqualTo(accountInput.name()),
                () -> assertThat(actualAccount.getBalance()).isEqualTo(accountInput.balance()));
    }

    @Test
    void shouldReturnError_whenUserCanNotUpdateAccount() {
        login("jd", authService);
        Account account = accountService.createAccount(getAccountInput("name_1"));

        AccountInput accountInput = AccountInput.builder()
                .archived(account.getArchived())
                .accountType(account.getAccountType())
                .color(account.getColor())
                .currency(account.getCurrency())
                .description(account.getDescription())
                .name("new_name")
                .balance(31.13)
                .build();
        login("jd_2", authService);
        // language=Graphql
        String mutation =
                """
				mutation($accountInput: AccountInput!, $hash: String!) {
				    updateAccount(hash: $hash, accountInput: $accountInput){
				        name
				        balance
				    }
				}
				""";

        graphQlTester
                .document(mutation)
                .variable("accountInput", TestUtils.toMap(accountInput))
                .variable("hash", account.getHash())
                .execute()
                .errors()
                .expect(errorTypeEquals(NOT_FOUND));
    }

    @Test
    @Transactional
    void shouldUpdateAccountParent_whenGetProperInput() {
        login("jd", authService);
        Account oldParent = accountService.createAccount(getAccountInput("old parent"));
        Account account = accountService.createAccount(AccountInput.builder()
                .parentHash(oldParent.getHash())
                .balance(2D)
                .accountType(AccountType.SAVINGS)
                .description("")
                .currency(Currency.PLN)
                .archived(false)
                .color("33")
                .name("name")
                .build());
        Account newParent = accountService.createAccount(getAccountInput("new parent"));

        AccountInput accountInput = AccountInput.builder()
                .archived(account.getArchived())
                .accountType(account.getAccountType())
                .color(account.getColor())
                .currency(account.getCurrency())
                .description(account.getDescription())
                .name(account.getName())
                .parentHash(newParent.getHash())
                .balance(account.getBalance())
                .build();

        // language=Graphql
        String mutation =
                """
				mutation($accountInput: AccountInput!, $hash: String!) {
				    updateAccount(hash: $hash, accountInput: $accountInput){
				        name
				        balance
				    }
				}
				""";

        graphQlTester
                .document(mutation)
                .variable("hash", account.getHash())
                .variable("accountInput", TestUtils.toMap(accountInput))
                .execute();

        Account actualAccount = accountService.getAccount(account.getHash());

        List<Account> newParentSubAccounts =
                accountService.getAccount(newParent.getHash()).getSubAccounts();
        List<Account> oldParentSubAccounts =
                accountService.getAccount(oldParent.getHash()).getSubAccounts();
        assertAll(
                () -> assertThat(actualAccount.getParent()).isEqualTo(newParent),
                () -> assertThat(oldParentSubAccounts).doesNotContain(actualAccount),
                () -> assertThat(newParentSubAccounts).contains(actualAccount));
    }
}
