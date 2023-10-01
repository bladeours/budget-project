package com.budget.project.service.repository;

import com.budget.project.model.db.Account;
import com.budget.project.model.db.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Set<Account> findAccountsByUsersContainingIgnoreCase(User users);
    Optional<Account> findByHash (String hash);
}
