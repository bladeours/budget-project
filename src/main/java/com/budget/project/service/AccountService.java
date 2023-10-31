package com.budget.project.service;

import com.budget.project.exception.AppException;
import com.budget.project.filter.Filter;
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

    @SneakyThrows
    public Account createAccount(AccountInput accountInput) {
        Account account;
        if(Objects.nonNull(accountInput.parentHash())){
            Account parent = this.findByHash(accountInput.parentHash());
            if(Objects.nonNull(parent.getParent())) {
                log.warn("only one level of subAccounts is possible");
                throw new AppException("only one level of subAccounts is possible", HttpStatus.BAD_REQUEST);
            }
            account = Account.of(accountInput, userService.getLoggedUser(), parent);
            account = accountRepository.save(account);
            parent.getSubAccounts().add(account);
        } else {
            account = Account.of(accountInput, userService.getLoggedUser());
            account = accountRepository.save(account);
        }
        userService.getLoggedUser().getAccounts().add(account);
        return account;
    }

    public Page<Account> getAccounts(CustomPage customPage, Filter filter) {
        if (Objects.isNull(filter)) {
            return accountRepository.findAllByUsersContaining(
                    PageRequest.of(customPage.number(), customPage.size()), userService.getLoggedUser());
        }
        return accountRepository.findAll(
                filter.getSpecification(userService.getLoggedUser()),
                PageRequest.of(customPage.number(), customPage.size()));
    }

    @SneakyThrows
    public Account getAccount(String hash) {
        return accountRepository.findByHashAndUsersContainingIgnoreCase(
                hash, userService.getLoggedUser()).orElseThrow(
                () -> {
                    log.warn(
                            "can't find account with hash: {}", hash);
                    return new AppException(
                            "can't find account with hash: " + hash,
                            HttpStatus.NOT_FOUND);
                });
    }

    @SneakyThrows
    public void deleteAccount(String hash) {
        Account account = this.findByHash(hash);
        userService.getLoggedUser().getAccounts().remove(account);
        accountRepository.delete(account);
    }

    @SneakyThrows
    private Account findByHash(String hash) {
        return accountRepository
                .findByHashAndUsersContainingIgnoreCase(hash, userService.getLoggedUser())
                .orElseThrow(
                        () -> {
                            log.debug("there is no account with hash: {}, at least for logged user", hash);
                            return new AppException("this account doesn't exist", HttpStatus.NOT_FOUND);
                        });
    }
}
