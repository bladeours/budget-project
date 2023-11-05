package com.budget.project.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.budget.project.auth.service.AuthService;
import com.budget.project.model.db.Account;
import com.budget.project.model.db.AccountType;
import com.budget.project.model.db.Currency;
import com.budget.project.model.dto.request.AuthenticationRequest;
import com.budget.project.model.dto.request.input.AccountInput;
import com.budget.project.service.AccountService;
import com.budget.project.service.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Objects;

@AutoConfigureGraphQlTester
@SpringBootTest
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
    private UserRepository userRepository;

    @Test
    public void nestedFilterTest() {
        if (Objects.isNull(userRepository.findByEmail("jd"))) {
            var authenticationRequest = new AuthenticationRequest("jd", "123");
            authService.register(authenticationRequest);
            var auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authenticationRequest.email(), authenticationRequest.password()));
        } else {
            authService.authenticate(new AuthenticationRequest("jd", "123"));
        }

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
}
