package com.budget.project.service;

import com.budget.project.exception.AppException;
import com.budget.project.model.db.Account;
import com.budget.project.model.dto.request.AccountInput;
import com.budget.project.service.repository.AccountRepository;
import com.budget.project.service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AccountService {
    private final UserService userService;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public Account createAccount(AccountInput accountInput) {
        Account account = Account.of(accountInput, userService.getLoggedUser());
        account = accountRepository.save(account);
        userService.getLoggedUser().getAccounts().add(account);
        return account;
    }

    public Set<Account> getAccounts() {
        return accountRepository.findAccountsByUsersContainingIgnoreCase(userService.getLoggedUser());
    }

    @SneakyThrows
    public void deleteAccount(String hash) {
        Account account = accountRepository.findByHash(hash).orElseThrow(
                () -> {
                    log.debug("there is no account with hash: {}", hash);
                    return new AppException("this account doesn't exist", HttpStatus.NOT_FOUND);
                }
        );
        if(isLoggedUserCoOwner(account)) {
            userService.getLoggedUser().getAccounts().remove(account);
            accountRepository.delete(account);
        } else {
            log.debug("user: {} doesn't have access to delete account: {}", userService.getLoggedUser().getHash(), hash);
            throw new AppException("you don't have access to delete", HttpStatus.FORBIDDEN);
        }


    }

    private boolean isLoggedUserCoOwner(Account account) {
        return account.getUsers().contains(userService.getLoggedUser());
    }
}
