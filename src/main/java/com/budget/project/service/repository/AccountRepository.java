package com.budget.project.service.repository;

import com.budget.project.model.db.Account;
import com.budget.project.model.db.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository
        extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {

    Optional<Account> findByHashAndUsersContainingIgnoreCase(String hash, User user);
}
