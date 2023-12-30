package com.budget.project.controller;

import static com.budget.project.utils.TestUtils.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.budget.project.auth.service.AuthService;
import com.budget.project.exception.AppException;
import com.budget.project.model.db.Account;
import com.budget.project.model.db.Budget;
import com.budget.project.model.db.Category;
import com.budget.project.model.db.Transaction;
import com.budget.project.model.dto.BudgetDto;
import com.budget.project.model.dto.request.input.BudgetInput;
import com.budget.project.service.AccountService;
import com.budget.project.service.BudgetService;
import com.budget.project.service.CategoryService;
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

import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.List;

@AutoConfigureGraphQlTester
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class BudgetControllerTest {
    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private BudgetService budgetService;

    @BeforeEach
    void setUp() {
        registerUsers(authService, authenticationManager);
    }

    @Test
    void shouldCreateBudget_whenGetProperInput() {
        login(USER_1, authService);
        Category category = categoryService.createCategory(getCategoryInput(false));
        // language=GraphQL
        String mutation =
                """
                        mutation($budgetInput: BudgetInput!) {
                            addBudget(budgetInput: $budgetInput){
                                hash
                            }
                        }
                        """;

        String date = "2023-12-09T15:52:18.526275423+01:00";
        BudgetInput budgetInput = getBudgetInput(category.getHash(), date);
        Budget budget = graphQlTester
                .document(mutation)
                .variable("budgetInput", TestUtils.toMap(budgetInput))
                .execute()
                .path("data.addBudget")
                .entity(Budget.class)
                .get();

        Budget actualBudget = budgetService.getBudget(budget.getHash());

        assertAll(
                () -> assertThat(actualBudget.getPlannedBudget())
                        .isEqualTo(budgetInput.plannedBudget()),
                () -> assertThat(actualBudget.getCategory()).isEqualTo(category),
                () -> assertThat(actualBudget.getDate()).isEqualTo(YearMonth.of(2023, 11)));
    }

    @Test
    void shouldGetBudgetsForMonth_whenGetProperRequest() {
        login(USER_1, authService);
        Category category1 = categoryService.createCategory(getCategoryInput(false));
        Category category2 = categoryService.createCategory(getCategoryInput(false));
        Category category3 = categoryService.createCategory(getCategoryInput(false));
        Account account = accountService.createAccount(getAccountInput("account"));
        Transaction transaction = transactionService.createTransaction(
                getTransactionInputExpense(category1.getHash(), account.getHash()));
        Budget budget1 = budgetService.createBudget(getBudgetInput(category1.getHash()));
        Budget budget2 = budgetService.createBudget(getBudgetInput(category2.getHash()));

        // language=GraphQL
        String query =
                """
                            query ($date: String!) {
                                getBudgets(date: $date){
                                    budget {
                                    hash
                        }
                        left
                        percent
                                }
                            }
                            """;

        graphQlTester
                .document(query)
                .variable("date", OffsetDateTime.now().toString())
                .execute()
                .path("data.getBudgets");

        List<BudgetDto> budgetDtoList =
                budgetService.getBudgetDtoList(OffsetDateTime.now().toString());

        assertAll(
                () -> assertThat(budgetDtoList.size()).isEqualTo(3),
                () -> assertThat(budgetDtoList.stream()
                                .filter(b -> b.budget().getHash().equals(budget1.getHash()))
                                .findFirst()
                                .get()
                                .left())
                        .isEqualTo(budget1.getPlannedBudget() - transaction.getAmount()),
                () -> assertThat(budgetDtoList.stream()
                                .filter(b -> b.budget().getHash().equals(budget1.getHash()))
                                .findFirst()
                                .get()
                                .percent())
                        .isEqualTo(transaction.getAmount() / budget1.getPlannedBudget() * 100));
    }

    @Test
    @Transactional
    void shouldUpdateBudget_whenGetProperRequest() {
        login(USER_1, authService);
        Category category = categoryService.createCategory(getCategoryInput(false));
        Budget budget = budgetService.createBudget(getBudgetInput(category.getHash()));
        // language=GraphQL
        String mutation =
                """
                        mutation ($hash: String!, $plannedBudget: Float!) {
                            updateBudget(plannedBudget: $plannedBudget, hash: $hash){
                                hash
                                plannedBudget
                            }
                        }
                        """;

        Budget actualBudget = graphQlTester
                .document(mutation)
                .variable("hash", budget.getHash())
                .variable("plannedBudget", 100.00)
                .execute()
                .path("data.updateBudget")
                .entity(Budget.class)
                .get();

        assertThat(actualBudget.getPlannedBudget()).isEqualTo(100.00);
    }

    @Test
    void shouldDeleteBudget_whenGetProperRequest() {
        login(USER_1, authService);
        Category category = categoryService.createCategory(getCategoryInput(false));
        Budget budget = budgetService.createBudget(getBudgetInput(category.getHash()));
        // language=GraphQL
        String mutation =
                """
                        mutation ($hash: String!) {
                            deleteBudget(hash: $hash)
                        }
                        """;

        graphQlTester.document(mutation).variable("hash", budget.getHash()).execute();

        assertThrows(AppException.class, () -> budgetService.getBudget(budget.getHash()));
    }
}
