package com.budget.project.controller;

import static com.budget.project.utils.TestUtils.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.graphql.execution.ErrorType.BAD_REQUEST;
import static org.springframework.graphql.execution.ErrorType.NOT_FOUND;

import com.budget.project.auth.service.AuthService;
import com.budget.project.exception.AppException;
import com.budget.project.model.db.Account;
import com.budget.project.model.db.Category;
import com.budget.project.model.db.Transaction;
import com.budget.project.model.db.TransactionType;
import com.budget.project.model.dto.request.input.TransactionInput;
import com.budget.project.service.AccountService;
import com.budget.project.service.CategoryService;
import com.budget.project.service.TransactionService;

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

@AutoConfigureGraphQlTester
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class TransactionControllerTest {
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

    @Autowired
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        registerUsers(authService, authenticationManager);
    }

    @Test
    @Transactional
    void shouldCreateExpenseTransaction_whenGetProperInput() {
        login(USER_1, authService);
        Account account = accountService.createAccount(getAccountInput(""));
        Double accountBalance = account.getBalance();
        Category category = categoryService.createCategory(getCategoryInput(false));

        // language=GraphQL
        String mutation =
                """
				mutation($input: TransactionInput!) {
				    addTransaction(transactionInput: $input){
				        name
				        hash
				        amount
				    }
				}
				""";

        Transaction transaction = graphQlTester
                .document(mutation)
                .variable(
                        "input",
                        toMap(getTransactionInputExpense(category.getHash(), account.getHash())))
                .execute()
                .path("data.addTransaction")
                .entity(Transaction.class)
                .get();
        Account finalAccount = accountService.getAccount(account.getHash());
        assertAll(
                () -> assertThat(transactionService.getTransaction(transaction.getHash()))
                        .isNotNull(),
                () -> assertThat(finalAccount.getBalance())
                        .isEqualTo(accountBalance - transaction.getAmount()),
                () -> assertThat(finalAccount.getTransactions()).contains(transaction),
                () -> assertThat(category.getTransactions()).contains(transaction));
    }

    @Test
    @Transactional
    void shouldCreateTransferTransaction_whenGetProperInput() {
        login(USER_1, authService);
        Account accountTo = accountService.createAccount(getAccountInput(""));
        Double accountToBalance = accountTo.getBalance();
        Account accountFrom = accountService.createAccount(getAccountInput("2"));
        Double accountFromBalance = accountFrom.getBalance();
        // language=GraphQL
        String mutation =
                """
				mutation($input: TransactionInput!) {
				    addTransaction(transactionInput: $input){
				        name
				        hash
				        amount
				    }
				}
				""";

        Transaction transaction = graphQlTester
                .document(mutation)
                .variable(
                        "input",
                        toMap(getTransactionInputTransfer(
                                accountTo.getHash(), accountFrom.getHash())))
                .execute()
                .path("data.addTransaction")
                .entity(Transaction.class)
                .get();
        assertAll(
                () -> assertThat(transactionService.getTransaction(transaction.getHash()))
                        .isNotNull(),
                () -> assertThat(
                                accountService.getAccount(accountFrom.getHash()).getBalance())
                        .isEqualTo(accountFromBalance - transaction.getAmount()),
                () -> assertThat(accountService.getAccount(accountTo.getHash()).getBalance())
                        .isEqualTo(accountToBalance + transaction.getAmount()),
                () -> assertThat(
                                accountService.getAccount(accountFrom.getHash()).getTransactions())
                        .contains(transaction),
                () -> assertThat(accountService.getAccount(accountTo.getHash()).getTransactions())
                        .contains(transaction));
    }

    @Test
    @Transactional
    void shouldCreateIncomeTransaction_whenGetProperInput() {
        login(USER_1, authService);
        Account accountTo = accountService.createAccount(getAccountInput(""));
        Double accountToBalance = accountTo.getBalance();
        Category category = categoryService.createCategory(getCategoryInput(true));
        // language=GraphQL
        String mutation =
                """
				mutation($input: TransactionInput!) {
				    addTransaction(transactionInput: $input){
				        name
				        hash
				        amount
				    }
				}
				""";

        Transaction transaction = graphQlTester
                .document(mutation)
                .variable(
                        "input",
                        toMap(getTransactionInputIncome(category.getHash(), accountTo.getHash())))
                .execute()
                .path("data.addTransaction")
                .entity(Transaction.class)
                .get();
        Account finalAccount = accountService.getAccount(accountTo.getHash());
        assertAll(
                () -> assertThat(transactionService.getTransaction(transaction.getHash()))
                        .isNotNull(),
                () -> assertThat(finalAccount.getBalance())
                        .isEqualTo(accountToBalance + transaction.getAmount()),
                () -> assertThat(finalAccount.getTransactions()).contains(transaction),
                () -> assertThat(category.getTransactions()).contains(transaction));
    }

    @Test
    void shouldReturnError_whenCreateIncomeTransactionButWrongCategory() {
        login(USER_1, authService);
        Account accountTo = accountService.createAccount(getAccountInput(""));
        Category category = categoryService.createCategory(getCategoryInput(false));
        // language=GraphQL
        String mutation =
                """
				mutation($input: TransactionInput!) {
				    addTransaction(transactionInput: $input){
				        name
				        hash
				        amount
				    }
				}
				""";

        graphQlTester
                .document(mutation)
                .variable(
                        "input",
                        toMap(getTransactionInputIncome(category.getHash(), accountTo.getHash())))
                .execute()
                .errors()
                .expect(errorTypeEquals(BAD_REQUEST));
    }

    @Test
    void shouldReturnError_whenCreateExpenseTransactionButWrongCategory() {
        login(USER_1, authService);
        Account accountTo = accountService.createAccount(getAccountInput(""));
        Category category = categoryService.createCategory(getCategoryInput(true));
        // language=GraphQL
        String mutation =
                """
				mutation($input: TransactionInput!) {
				    addTransaction(transactionInput: $input){
				        name
				        hash
				        amount
				    }
				}
				""";

        graphQlTester
                .document(mutation)
                .variable(
                        "input",
                        toMap(getTransactionInputExpense(category.getHash(), accountTo.getHash())))
                .execute()
                .errors()
                .expect(errorTypeEquals(BAD_REQUEST));
    }

    @Test
    void shouldReturnTransactionPage_whenGetProperRequest() {
        login(USER_2, authService);
        Account account_jd_2 = accountService.createAccount(getAccountInput(""));
        Category category_jd_2 = categoryService.createCategory(getCategoryInput(false));
        transactionService.createTransaction(
                getTransactionInputExpense(category_jd_2.getHash(), account_jd_2.getHash()));
        login(USER_1, authService);
        Account account_jd = accountService.createAccount(getAccountInput(""));
        Category category_jd = categoryService.createCategory(getCategoryInput(false));
        Transaction transaction_jd1 = transactionService.createTransaction(
                getTransactionInputExpense(category_jd.getHash(), account_jd.getHash()));
        Transaction transaction_jd2 = transactionService.createTransaction(
                getTransactionInputExpense(category_jd.getHash(), account_jd.getHash()));
        Transaction transaction_jd3 = transactionService.createTransaction(
                getTransactionInputExpense(category_jd.getHash(), account_jd.getHash()));

        // language=Graphql
        String query =
                """
                query {
                getTransactionsPage(page: {number: 0, size: 2}){
                    content {
                        hash
                    }
                }
                }
                """;
        graphQlTester
                .document(query)
                .execute()
                .path("data.getTransactionsPage")
                .matchesJson(String.format(
                        """
                      {
                        "content": [
                          {
                            "hash": %s
                          },
                          {
                            "hash": %s
                          }
                        ]
                      }

                  """,
                        transaction_jd3.getHash(), transaction_jd2.getHash()));

        query =
                """
                query {
                getTransactionsPage(page: {number: 1, size: 2}){
                    content {
                        hash
                    }
                }
                }
                """;
        graphQlTester
                .document(query)
                .execute()
                .path("data.getTransactionsPage")
                .matchesJson(String.format(
                        """
                      {
                        "content": [
                          {
                            "hash": %s
                          }
                        ]
                      }

                  """,
                        transaction_jd1.getHash()));
    }

    @Test
    void shouldGetTransaction_whenGetProperRequest() {
        login(USER_1, authService);
        Account account = accountService.createAccount(getAccountInput(""));
        Category category = categoryService.createCategory(getCategoryInput(false));
        Transaction expectedTransaction = transactionService.createTransaction(
                getTransactionInputExpense(category.getHash(), account.getHash()));
        // language=GraphQL
        String query =
                """
				query($hash: String!) {
				    getTransaction(hash: $hash){
				        hash
				        name
				        amount
				    }
				}
				""";

        Transaction actualTransaction = graphQlTester
                .document(query)
                .variable("hash", expectedTransaction.getHash())
                .execute()
                .path("data.getTransaction")
                .entity(Transaction.class)
                .get();

        assertAll(
                () -> assertThat(actualTransaction.getHash())
                        .isEqualTo(expectedTransaction.getHash()),
                () -> assertThat(actualTransaction.getAmount())
                        .isEqualTo(expectedTransaction.getAmount()),
                () -> assertThat(actualTransaction.getName())
                        .isEqualTo(expectedTransaction.getName()));
    }

    @Test
    void shouldNotGetAccount_whenAccountIsForDifferentUser() {
        login(USER_1, authService);
        Account account = accountService.createAccount(getAccountInput(""));
        Category category = categoryService.createCategory(getCategoryInput(false));
        Transaction expectedTransaction = transactionService.createTransaction(
                getTransactionInputExpense(category.getHash(), account.getHash()));
        login(USER_2, authService);
        // language=GraphQL
        String query =
                """
				query($hash: String!) {
				    getTransaction(hash: $hash){
				        hash
				        name
				        amount
				    }
				}
				""";

        graphQlTester
                .document(query)
                .variable("hash", expectedTransaction.getHash())
                .execute()
                .errors()
                .expect(errorTypeEquals(NOT_FOUND));
    }

    @Test
    @Transactional
    void shouldRemoveTransaction_whenGetProperInput() {
        login(USER_1, authService);
        Account accountFrom = accountService.createAccount(getAccountInput("essa"));
        Account accountTo = accountService.createAccount(getAccountInput("essa2"));
        Transaction expectedTransaction = transactionService.createTransaction(
                getTransactionInputTransfer(accountTo.getHash(), accountFrom.getHash()));

        // language=GraphQL
        String mutation =
                """
				mutation($hash: String!) {
				    deleteTransaction(hash: $hash)
				}
				""";
        graphQlTester
                .document(mutation)
                .variable("hash", expectedTransaction.getHash())
                .execute();
        assertAll(
                () -> assertThrows(
                        AppException.class,
                        () -> transactionService.getTransaction(expectedTransaction.getHash())),
                () -> assertThat(
                                accountService.getAccount(accountFrom.getHash()).getTransactions())
                        .isEmpty(),
                () -> assertThat(accountService.getAccount(accountTo.getHash()).getTransactions())
                        .isEmpty());
    }

    @Test
    void shouldUpdateExpenseTransaction_whenTypeNotChange() {
        login(USER_1, authService);
        Account oldAccountFrom = accountService.createAccount(getAccountInput("essa"));
        Account newAccountFrom = accountService.createAccount(getAccountInput("essa2"));
        Category category = categoryService.createCategory(getCategoryInput(false));
        Transaction transactionBefore = transactionService.createTransaction(
                getTransactionInputExpense(category.getHash(), oldAccountFrom.getHash()));

        TransactionInput transactionUpdateInput = TransactionInput.builder()
                .date(transactionBefore.getDate().toString() + "Z")
                .name("new_name")
                .need(true)
                .currency(transactionBefore.getCurrency())
                .accountFromHash(newAccountFrom.getHash())
                .categoryHash(category.getHash())
                .amount(transactionBefore.getAmount() + 10.0)
                .transactionType(TransactionType.EXPENSE)
                .build();
        // language=GraphQL
        String mutation =
                """
        mutation($input: TransactionInput!, $hash: String!) {
            updateTransaction(hash: $hash, transactionInput: $input){
            hash
            }
        }
        """;
        graphQlTester
                .document(mutation)
                .variable("hash", transactionBefore.getHash())
                .variable("input", toMap(transactionUpdateInput))
                .execute()
                .path("data.updateTransaction");

        assertAll(
                () -> assertThat(transactionService
                                .getTransaction(transactionBefore.getHash())
                                .getAmount())
                        .isEqualTo(transactionUpdateInput.amount()),
                () -> assertThat(accountService
                                .getAccount(oldAccountFrom.getHash())
                                .getBalance())
                        .isEqualTo(oldAccountFrom.getBalance()),
                () -> assertEquals(
                        accountService.getAccount(newAccountFrom.getHash()).getBalance(),
                        newAccountFrom.getBalance() - transactionUpdateInput.amount(),
                        0.0001));
    }

    @Test
    void shouldUpdateExpenseTransaction_whenTypeChange() {
        login(USER_1, authService);
        Account oldAccountFrom = accountService.createAccount(getAccountInput("essa"));
        Account newAccountTo = accountService.createAccount(getAccountInput("essa2"));
        Category category = categoryService.createCategory(getCategoryInput(false));
        Category categoryIncome = categoryService.createCategory(getCategoryInput(true));
        Transaction transactionBefore = transactionService.createTransaction(
                getTransactionInputExpense(category.getHash(), oldAccountFrom.getHash()));

        TransactionInput transactionUpdateInput = TransactionInput.builder()
                .date(transactionBefore.getDate().toString() + "Z")
                .name("new_name")
                .need(true)
                .currency(transactionBefore.getCurrency())
                .accountToHash(newAccountTo.getHash())
                .categoryHash(categoryIncome.getHash())
                .amount(transactionBefore.getAmount() + 10.0)
                .transactionType(TransactionType.INCOME)
                .build();
        // language=GraphQL
        String mutation =
                """
        mutation($input: TransactionInput!, $hash: String!) {
            updateTransaction(hash: $hash, transactionInput: $input){
            hash
            }
        }
        """;
        graphQlTester
                .document(mutation)
                .variable("hash", transactionBefore.getHash())
                .variable("input", toMap(transactionUpdateInput))
                .execute()
                .path("data.updateTransaction");

        assertAll(
                () -> assertThat(transactionService
                                .getTransaction(transactionBefore.getHash())
                                .getAmount())
                        .isEqualTo(transactionUpdateInput.amount()),
                () -> assertThat(accountService
                                .getAccount(oldAccountFrom.getHash())
                                .getBalance())
                        .isEqualTo(oldAccountFrom.getBalance()),
                () -> assertEquals(
                        accountService.getAccount(newAccountTo.getHash()).getBalance(),
                        newAccountTo.getBalance() + transactionUpdateInput.amount(),
                        0.0001));
    }
}
