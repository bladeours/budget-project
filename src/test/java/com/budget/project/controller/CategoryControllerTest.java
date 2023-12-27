package com.budget.project.controller;

import static com.budget.project.utils.TestUtils.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.graphql.execution.ErrorType.*;

import com.budget.project.auth.service.AuthService;
import com.budget.project.exception.AppException;
import com.budget.project.model.db.Account;
import com.budget.project.model.db.Category;
import com.budget.project.model.db.SubCategory;
import com.budget.project.model.db.Transaction;
import com.budget.project.model.dto.request.input.CategoryInput;
import com.budget.project.model.dto.request.input.CategoryUpdateInput;
import com.budget.project.model.dto.request.input.SubCategoryInput;
import com.budget.project.service.PlannedIncomeService;
import com.budget.project.service.AccountService;
import com.budget.project.service.CategoryService;
import com.budget.project.service.TransactionService;
import com.budget.project.service.UserService;

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

@AutoConfigureGraphQlTester
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CategoryControllerTest {
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
    private UserService userService;

    @BeforeEach
    void setUp() {
        registerUsers(authService, authenticationManager);
    }

    @Test
    @Transactional
    void shouldCreateCategory_whenGetProperInput() {
        login(USER_1, authService);
        CategoryInput categoryInput = getCategoryInput(true);
        // language=GraphQL
        String mutation =
                """
				mutation($categoryInput: CategoryInput!) {
				    addCategory(categoryInput: $categoryInput){
				        hash
				        name
				    }
				}
				""";

        String hash = graphQlTester
                .document(mutation)
                .variable("categoryInput", toMap(categoryInput))
                .execute()
                .path("data.addCategory")
                .entity(Category.class)
                .get()
                .getHash();

        Category actualCategory = categoryService.getCategory(hash);
        assertAll(
                () -> assertThat(actualCategory.getName()).isEqualTo(categoryInput.name()),
                () -> assertThat(actualCategory.getUsers().stream()
                                .anyMatch(u -> u.getEmail().equals(USER_1)))
                        .isTrue());
    }

    @Test
    @Transactional
    void shouldGetCategory_whenGetProperInput() {
        login(USER_1, authService);
        CategoryInput categoryInput = getCategoryInput(true);
        Category expectedCategory = categoryService.createCategory(categoryInput);
        // language=GraphQL
        String query =
                """
				query ($hash: String!) {
				    getCategory(hash: $hash){
				        hash
				        name
				    }
				}
				""";

        Category actualCategory = graphQlTester
                .document(query)
                .variable("hash", expectedCategory.getHash())
                .execute()
                .path("data.getCategory")
                .entity(Category.class)
                .get();

        assertAll(
                () -> assertThat(actualCategory.getHash()).isEqualTo(expectedCategory.getHash()),
                () -> assertThat(actualCategory.getName()).isEqualTo(expectedCategory.getName()));
    }

    @Test
    @Transactional
    void shouldNotCategory_whenCategoryIsForDifferentUser() {
        login(USER_2, authService);
        CategoryInput categoryInput = getCategoryInput(true);
        Category expectedCategory = categoryService.createCategory(categoryInput);
        login(USER_1, authService);
        // language=GraphQL
        String query =
                """
				query ($hash: String!) {
				    getCategory(hash: $hash){
				        hash
				        name
				    }
				}
				""";

        graphQlTester
                .document(query)
                .variable("hash", expectedCategory.getHash())
                .execute()
                .errors()
                .expect(errorTypeEquals(NOT_FOUND));
    }

    @Test
    void shouldGetAccountsPage_whenGetProperRequest() {
        login(USER_2, authService);
        categoryService.createCategory(getCategoryInput(true));
        login(USER_1, authService);
        Category expectedCategory_2 = categoryService.createCategory(getCategoryInput(true));
        Category expectedCategory_3 = categoryService.createCategory(getCategoryInput(true));
        Category expectedCategory_4 = categoryService.createCategory(getCategoryInput(true));
        // language=GraphQL
        String query =
                """
                query {
                    getCategoriesPage(page: {number: 0, size: 2}){
                    content {
                        hash
                      }
                    }
                }
                """;
        graphQlTester
                .document(query)
                .execute()
                .path("data.getCategoriesPage")
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
                        expectedCategory_2.getHash(), expectedCategory_3.getHash()));
        query =
                """
                query {
                    getCategoriesPage(page: {number: 1, size: 2}){
                    content {
                        hash
                      }
                    }
                }
                """;
        graphQlTester
                .document(query)
                .execute()
                .path("data.getCategoriesPage")
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
                        expectedCategory_4.getHash()));
    }

    @Test
    void shouldRemoveCategoryAndSubCategories_whenGetProperInput() {
        login(USER_1, authService);
        Category parent = categoryService.createCategory(getCategoryInput(true).toBuilder()
                .subCategories(List.of(new SubCategoryInput("child", null)))
                .build());

        SubCategory child = parent.getSubCategories().stream()
                .filter(c -> c.getName().equals("child"))
                .findFirst()
                .get();

        // language=GraphQL
        String mutation =
                """
				mutation($hash: String!) {
				    deleteCategory(hash: $hash)
				}
				""";

        graphQlTester.document(mutation).variable("hash", parent.getHash()).execute();

        assertAll(
                () -> assertThrows(
                        AppException.class, () -> categoryService.getCategory(parent.getHash())),
                () -> assertThrows(
                        AppException.class, () -> categoryService.getCategory(child.getHash())));
    }

    @Test
    void shouldReturnError_whenDeleteDifferentUserAccount() {
        login(USER_1, authService);
        Category parent = categoryService.createCategory(getCategoryInput(true));
        login(USER_2, authService);
        // language=GraphQL
        String mutation =
                """
				mutation($hash: String!) {
				    deleteCategory(hash: $hash)
				}
				""";

        graphQlTester
                .document(mutation)
                .variable("hash", parent.getHash())
                .execute()
                .errors()
                .expect(errorTypeEquals(NOT_FOUND));
    }

    @Test
    void shouldUpdateCategory_whenGetProperInput() {
        login(USER_1, authService);
        Category category = categoryService.createCategory(getCategoryInput(true));
        CategoryUpdateInput categoryUpdateInput = CategoryUpdateInput.builder()
                .archived(false)
                .color(category.getColor())
                .name("essa")
                .build();
        // language=Graphql
        String mutation =
                """
				mutation($categoryUpdateInput: CategoryUpdateInput!, $hash: String!) {
				    updateCategory(hash: $hash, categoryUpdateInput: $categoryUpdateInput){
				        name
				        color
				    }
				}
				""";

        graphQlTester
                .document(mutation)
                .variable("categoryUpdateInput", toMap(categoryUpdateInput))
                .variable("hash", category.getHash())
                .execute();

        Category actualCategory = categoryService.getCategory(category.getHash());

        assertAll(
                () -> assertThat(actualCategory.getName()).isEqualTo(categoryUpdateInput.name()),
                () -> assertThat(actualCategory.getColor()).isEqualTo(categoryUpdateInput.color()));
    }

    @Test
    void shouldReturnError_whenUserCanNotUpdateCategory() {
        login(USER_1, authService);
        Category category = categoryService.createCategory(getCategoryInput(true));
        CategoryUpdateInput categoryUpdateInput = CategoryUpdateInput.builder()
                .archived(false)
                .color(category.getColor())
                .name("essa")
                .build();
        login(USER_2, authService);
        // language=Graphql
        String mutation =
                """

                        mutation($categoryUpdateInput: CategoryUpdateInput!, $hash: String!) {
				    updateCategory(hash: $hash, categoryUpdateInput: $categoryUpdateInput){
				        name
				        color
				    }
				}
				""";

        graphQlTester
                .document(mutation)
                .variable("categoryUpdateInput", toMap(categoryUpdateInput))
                .variable("hash", category.getHash())
                .execute()
                .errors()
                .expect(errorTypeEquals(NOT_FOUND));
    }

    @Test
    @Transactional
    void shouldRemoveSubCategoryFromParentAndMoveTransactionsToParent_whenGetUpdate() {
        login(USER_1, authService);
        Category parent = categoryService.createCategory(getCategoryInput(true).toBuilder()
                .subCategories(List.of(
                        new SubCategoryInput("child", null), new SubCategoryInput("child_2", null)))
                .build());

        SubCategory child = parent.getSubCategories().stream()
                .filter(c -> c.getName().equals("child"))
                .findFirst()
                .get();
        SubCategory child2 = parent.getSubCategories().stream()
                .filter(c -> c.getName().equals("child_2"))
                .findFirst()
                .get();
        Account account = accountService.createAccount(getAccountInput(""));
        Transaction transaction = transactionService.createTransaction(
                getTransactionInputIncome(parent.getHash(), account.getHash(), child.getHash()));

        CategoryUpdateInput categoryUpdateInput = CategoryUpdateInput.builder()
                .name(parent.getName())
                .color(parent.getColor())
                .archived(parent.getArchived())
                .subCategories(List.of())
                .build();

        // language=GraphQL
        String mutation =
                """
				mutation($hash: String!, $categoryUpdateInput: CategoryUpdateInput!) {
				    updateCategory(hash: $hash, categoryUpdateInput: $categoryUpdateInput){
				    hash
				    }
				}
				""";

        graphQlTester
                .document(mutation)
                .variable("hash", parent.getHash())
                .variable("categoryUpdateInput", toMap(categoryUpdateInput))
                .execute();

        Category finalParent = categoryService.getCategory(parent.getHash());
        Transaction finalTransaction = transactionService.getTransaction(transaction.getHash());

        assertAll(
                () -> assertThrows(
                        AppException.class, () -> categoryService.getCategory(child.getHash())),
                () -> assertThat(finalParent.getSubCategories()).doesNotContain(child),
                () -> assertThat(finalParent.getSubCategories()).contains(child2),
                () -> assertThat(finalParent.getTransactions()).contains(finalTransaction),
                () -> assertThat(finalTransaction.getCategory()).isEqualTo(finalParent),
                () -> assertThat(finalTransaction.getSubCategory()).isNull());
    }
}
