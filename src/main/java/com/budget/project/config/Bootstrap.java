package com.budget.project.config;

import com.budget.project.auth.service.AuthService;
import com.budget.project.model.db.AccountType;
import com.budget.project.model.db.Currency;
import com.budget.project.model.dto.request.AccountInput;
import com.budget.project.model.dto.request.AuthenticationRequest;
import com.budget.project.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class Bootstrap implements ApplicationRunner {

    private final AuthService authService;
    private final AccountService accountService;
    private final AuthenticationManager authenticationManager;

    @Override
    public void run(ApplicationArguments args) {
        var authenticationRequest = new AuthenticationRequest("email@email.com", "123");
        System.out.println(authService.register(authenticationRequest).token());
        var auth =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                authenticationRequest.email(), authenticationRequest.password()));
        SecurityContextHolder.getContext().setAuthentication(auth);
        accountService.createAccount(
                AccountInput.builder()
                        .accountType(AccountType.REGULAR)
                        .balance(21.36)
                        .currency(Currency.PLN)
                        .color("#ffffff")
                        .name("test_1")
                        .build());
        accountService.createAccount(
                AccountInput.builder()
                        .accountType(AccountType.SAVINGS)
                        .balance(14.21)
                        .currency(Currency.PLN)
                        .color("#ffffff")
                        .name("test_2")
                        .build());
    }
}
