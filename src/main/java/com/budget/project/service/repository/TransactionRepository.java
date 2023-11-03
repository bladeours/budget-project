package com.budget.project.service.repository;

import com.budget.project.model.db.Transaction;
import com.budget.project.model.db.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TransactionRepository
        extends JpaRepository<Transaction, Integer>, JpaSpecificationExecutor<Transaction> {

    @Query(
            "SELECT t FROM Transaction t "
                    + "LEFT JOIN t.accountFrom af LEFT JOIN t.accountTo at "
                    + "WHERE :user MEMBER OF af.users OR :user MEMBER OF at.users")
    Page<Transaction> findTransactionsForUser(User user, PageRequest pageRequest);

    @Query(
            "SELECT t FROM Transaction t LEFT JOIN t.accountFrom af LEFT JOIN t.accountTo at WHERE"
                + " (:user MEMBER OF af.users OR :user MEMBER OF at.users) AND t.hash like :hash")
    Optional<Transaction> findByHashForUser(String hash, User user);
}
