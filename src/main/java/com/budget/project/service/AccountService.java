package com.budget.project.service;

import com.budget.project.exception.AppException;
import com.budget.project.model.db.Account;
import com.budget.project.model.dto.request.AccountInput;
import com.budget.project.model.dto.request.Page;
import com.budget.project.service.repository.AccountRepository;
import com.budget.project.service.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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

    public List<Account> getAccounts(Page page, Specification<Account> specification) {
        return accountRepository.findAll(specification);
        //        return accountRepository.findAccountsByUsersContainingIgnoreCase(
        //                userService.getLoggedUser(), PageRequest.of(page.number(), page.size()),
        // specification);
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
        //        if (isLoggedUserCoOwner(account)) {
        //            userService.getLoggedUser().getAccounts().remove(account);
        //            accountRepository.delete(account);
        //        } else {
        //            log.debug(
        //                    "user: {} doesn't have access to delete account: {}",
        //                    userService.getLoggedUser().getHash(),
        //                    hash);
        //            throw new AppException("you don't have access to delete",
        // HttpStatus.FORBIDDEN);
        //        }
    }

    //    private boolean isLoggedUserCoOwner(Account account) {
    //        return account.getUsers().contains(userService.getLoggedUser());
    //    }
}
