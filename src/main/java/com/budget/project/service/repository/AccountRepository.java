package com.budget.project.service.repository;

import com.budget.project.model.db.Account;
import com.budget.project.model.db.User;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Set<Account> findAccountsByUsersContainingIgnoreCase(User users);

    Optional<Account> findByHash(String hash);
}
