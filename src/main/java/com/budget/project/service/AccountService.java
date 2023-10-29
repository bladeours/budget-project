package com.budget.project.service;

import com.budget.project.exception.AppException;
import com.budget.project.filter.AccountFilter;
import com.budget.project.model.db.Account;
import com.budget.project.model.dto.request.AccountInput;
import com.budget.project.model.dto.request.CustomPage;
import com.budget.project.service.repository.AccountRepository;
import jakarta.transaction.Transactional;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AccountService {
    private final UserService userService;
    private final AccountRepository accountRepository;

    public Account createAccount(AccountInput accountInput) {
        Account account = Account.of(accountInput, userService.getLoggedUser());
        account = accountRepository.save(account);
        userService.getLoggedUser().getAccounts().add(account);
        return account;
    }

    public Page<Account> getAccounts(CustomPage customPage, AccountFilter filter) {
        if (Objects.isNull(filter)) {
            return accountRepository.findAll(
                    PageRequest.of(customPage.number(), customPage.size()));
        }
        return accountRepository.findAll(
                filter.getSpecification(userService.getLoggedUser()),
                PageRequest.of(customPage.number(), customPage.size()));
    }

    public Optional<Account> getAccount(String hash) {
        return accountRepository.findByHashAndUsersContainingIgnoreCase(
                hash, userService.getLoggedUser());
    }

    @SneakyThrows
    public void deleteAccount(String hash) {
        Account account =
                accountRepository
                        .findByHashAndUsersContainingIgnoreCase(hash, userService.getLoggedUser())
                        .orElseThrow(
                                () -> {
                                    log.debug(
                                            "there is no account with hash: {}, at least for logged"
                                                    + " user",
                                            hash);
                                    return new AppException(
                                            "this account doesn't exist", HttpStatus.NOT_FOUND);
                                });
        userService.getLoggedUser().getAccounts().remove(account);
        accountRepository.delete(account);
    }
}
