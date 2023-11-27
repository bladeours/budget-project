package com.budget.project.filter;

import static com.budget.project.utils.TestUtils.*;
import static com.budget.project.utils.TestUtils.USER_1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.budget.project.auth.service.AuthService;
import com.budget.project.model.db.*;
import com.budget.project.model.dto.request.input.AccountInput;
import com.budget.project.model.dto.request.input.CategoryInput;
import com.budget.project.service.AccountService;
import com.budget.project.service.CategoryService;
import com.budget.project.service.TransactionService;
import com.budget.project.utils.TestUtils;

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
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class FilterTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        registerUsers(authService, authenticationManager);
    }

    @Test
    public void nestedFilterTest() {
        login(USER_1, authService);

        Account account_1 = accountService.createAccount(AccountInput.builder()
                .accountType(AccountType.REGULAR)
                .balance(21.36)
                .currency(Currency.PLN)
                .color("#ffffff")
                .name("test_1")
                .description("test_1_desc")
                .build());
        Account account_2 = accountService.createAccount(AccountInput.builder()
                .accountType(AccountType.SAVINGS)
                .balance(14.21)
                .currency(Currency.PLN)
                .color("#ffffff")
                .name("test_2")
                .description("test_2_desc")
                .build());
        Account account_3 = accountService.createAccount(AccountInput.builder()
                .accountType(AccountType.SAVINGS)
                .balance(14.21)
                .currency(Currency.PLN)
                .color("#ffffff")
                .name("test_3")
                .description("test_2_desc")
                .build());

        // language=GraphQL
        String query =
                """
				{
				  getAccountsPage(
				    page: {number: 0, size: 10}
				    filter:
				        {
				    		logicOperator: AND,
				            subFilters: [
				        		{
				            	logicOperator: AND,
				            	doubleFilters: [
				            		{field:"balance", operator: GT, value: 10.00},
				            		{field:"balance", operator: LT, value: 20.00}],
				        		},
				        		{
				            	logicOperator: OR,
				            	subFilters: [{
				            		logicOperator: OR,
				            		stringFilters: [
				                        {field: "name", operator: EQUALS, value: "test_2"},
				                        {field: "name", operator: EQUALS, value: "not_exists"}
				            		]
				            		},
				            		{
				            		logicOperator: AND,
				            		stringFilters: [
				            		    {field: "name", operator: EQUALS, value: "test_3"}
				            		],
				            		doubleFilters: [
				            		    {field: "balance", operator: EQ, value: 14.21}
				            		]
				            		}
				            	]
				        }
				            ]}
				  ) {
				    content {
				        hash
				    }
				  }
				}
				""";
        GraphQlTester.EntityList<Account> accounts = graphQlTester
                .document(query)
                .execute()
                .path("data.getAccountsPage.content")
                .entityList(Account.class);
        assertAll(
                () -> assertThat(accounts.get().stream()
                                .anyMatch(a -> a.getHash().equals(account_2.getHash())))
                        .isTrue(),
                () -> assertThat(accounts.get().stream()
                                .anyMatch(a -> a.getHash().equals(account_3.getHash())))
                        .isTrue(),
                () -> assertThat(accounts.get()).hasSize(2));
    }

    @Test
    public void transactionFilterTest() {
        login(USER_1, authService);

        Account account_1 = accountService.createAccount(AccountInput.builder()
                .accountType(AccountType.REGULAR)
                .balance(21.36)
                .currency(Currency.PLN)
                .color("#ffffff")
                .name("test_1")
                .description("test_1_desc")
                .build());
        Category category = categoryService.createCategory(CategoryInput.builder()
                .name("name_category")
                .income(true)
                .color("434")
                .archived(false)
                .build());

        Transaction transaction = transactionService.createTransaction(
                TestUtils.getTransactionInputIncome(category.getHash(), account_1.getHash()));

        // language=GraphQL
        String query =
                """
                query($hash: String!){
                  getTransactionsPage(
                    page: {number: 0, size: 10}
                    filter:
                        {
                            logicOperator: AND,
                            stringFilters: [{
                            	operator: EQUALS
                          	 	value: $hash
                          	 	field: "category.hash"
                          	 }
                            ]
                            }
                  ) {
                    content {
                        hash
                        name
                    }
                  }
                }
                """;
        List<Transaction> transactions = graphQlTester
                .document(query)
                .variable("hash", category.getHash())
                .execute()
                .path("data.getTransactionsPage.content")
                .entityList(Transaction.class)
                .get();
        assertAll(
                () -> assertThat(transactions.stream()
                                .anyMatch(a -> a.getHash().equals(transaction.getHash())))
                        .isTrue(),
                () -> assertThat(transactions).hasSize(1));
    }

    @Test
    public void transactionFilterTest2() {
        login(USER_1, authService);

        Account account_1 = accountService.createAccount(AccountInput.builder()
                .accountType(AccountType.REGULAR)
                .balance(21.36)
                .currency(Currency.PLN)
                .color("#ffffff")
                .name("test_1")
                .description("test_1_desc")
                .build());
        Category category = categoryService.createCategory(CategoryInput.builder()
                .name("name_category")
                .income(true)
                .color("434")
                .archived(false)
                .build());

        Transaction transaction = transactionService.createTransaction(
                TestUtils.getTransactionInputIncome(category.getHash(), account_1.getHash()));

        // language=GraphQL
        String query =
                """
                query($hash: String!){
                  getTransactionsPage(
                    page: {number: 0, size: 10}
                    filter:
                        {
                            logicOperator: AND,
                            stringFilters: [{
                            	operator: EQUALS
                          	 	value: $hash
                          	 	field: "category.hash"
                          	 }
                            ]
                            }
                  ) {
                    content {
                        hash
                        name
                    }
                  }
                }
                """;
        List<Transaction> transactions = graphQlTester
                .document(query)
                .variable("hash", category.getHash())
                .execute()
                .path("data.getTransactionsPage.content")
                .entityList(Transaction.class)
                .get();
        assertAll(
                () -> assertThat(transactions.stream()
                                .anyMatch(a -> a.getHash().equals(transaction.getHash())))
                        .isTrue(),
                () -> assertThat(transactions).hasSize(1));
    }
}
