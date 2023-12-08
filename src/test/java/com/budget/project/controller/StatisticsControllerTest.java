package com.budget.project.controller;

import static com.budget.project.utils.TestUtils.*;
import static com.budget.project.utils.TestUtils.getTransactionInputExpense;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.budget.project.auth.service.AuthService;
import com.budget.project.model.db.Account;
import com.budget.project.model.db.Category;
import com.budget.project.model.db.Transaction;
import com.budget.project.model.dto.CategoryAmount;
import com.budget.project.service.AccountService;
import com.budget.project.service.CategoryService;
import com.budget.project.service.TransactionService;

import org.h2.tools.Server;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

@AutoConfigureGraphQlTester
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class StatisticsControllerTest {
    @Autowired
    private AuthService authService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    void setUp() {
        registerUsers(authService, authenticationManager);
    }

    @Test
    void shouldReturnArrayOfExpenseCategoryAmount_whenGetProperInput() {
        login(USER_1, authService);
        Account account_jd = accountService.createAccount(getAccountInput(""));
        Category category1 = categoryService.createCategory(
                getCategoryInput(false).toBuilder().name("cat1").build());
        Category category2 = categoryService.createCategory(
                getCategoryInput(false).toBuilder().name("cat2").build());
        Transaction transaction1 = transactionService.createTransaction(
                getTransactionInputExpense(category1.getHash(), account_jd.getHash()).toBuilder()
                        .date(OffsetDateTime.now().plusDays(1L).toString())
                        .build());
        Transaction transaction2 = transactionService.createTransaction(
                getTransactionInputExpense(category1.getHash(), account_jd.getHash()));
        Transaction transaction3 = transactionService.createTransaction(
                getTransactionInputExpense(category1.getHash(), account_jd.getHash()));

        Transaction transaction4 = transactionService.createTransaction(
                getTransactionInputExpense(category2.getHash(), account_jd.getHash()));
        Transaction transaction5 = transactionService.createTransaction(
                getTransactionInputExpense(category2.getHash(), account_jd.getHash()));
        Transaction transaction6 = transactionService.createTransaction(
                getTransactionInputExpense(category2.getHash(), account_jd.getHash()).toBuilder()
                        .date(OffsetDateTime.now().minusDays(10L).toString())
                        .build());

        // language=Graphql
        String query =
                """
                        query($startDate: String!, $endDate: String!) {
                        getAmountByCategory(startDate: $startDate, endDate: $endDate, income: false){
                            amount
                            name
                        }
                        }
                        """;
        List<CategoryAmount> categoryAmounts = graphQlTester
                .document(query)
                .variable("startDate", OffsetDateTime.now().minusDays(1L).toString())
                .variable("endDate", OffsetDateTime.now().plusMinutes(1L).toString())
                .execute()
                .path("data.getAmountByCategory")
                .entityList(CategoryAmount.class)
                .get();

        assertAll(
                () -> assertThat(categoryAmounts.stream()
                                .filter(c -> c.name().equals("cat1"))
                                .findFirst()
                                .get()
                                .amount())
                        .isEqualTo(transaction2.getAmount() + transaction3.getAmount()),
                () -> assertThat(categoryAmounts.stream()
                                .filter(c -> c.name().equals("cat2"))
                                .findFirst()
                                .get()
                                .amount())
                        .isEqualTo(transaction4.getAmount() + transaction5.getAmount()));
    }
}
