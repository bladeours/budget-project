package com.budget.project.service.repository;

import com.budget.project.model.db.Account;
import com.budget.project.model.db.User;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository
        extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {
    Set<Account> findAccountsByUsersContainingIgnoreCase(
            User user, PageRequest pageRequest, Specification<Account> specification);

    Optional<Account> findByHashAndUsersContainingIgnoreCase(String hash, User user);
}
