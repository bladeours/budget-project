package com.budget.project.service.repository;

import com.budget.project.model.db.Account;
import com.budget.project.model.db.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository
        extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {

    Page<Account> findAllByUsersContaining(PageRequest pageRequest, User user);

    List<Account> findAllByUsersContaining(User user);

    Optional<Account> findByHashAndUsersContainingIgnoreCase(String hash, User user);

    Optional<Account> findByNameAndUsersContainingIgnoreCase(String name, User user);

    @Query("SELECT a FROM Account a"
            + " where (:user MEMBER OF a.users) and "
            + "a.archived = false"
            + " order by a.balance desc limit 3")
    List<Account> getTopAccounts(User user);
}
