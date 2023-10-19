package com.budget.project.service.repository;

import com.budget.project.model.db.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    //    List<Transaction> findTransactionByUsersContainingIgnoreCase(User users, PageRequest
    // pageRequest);

    //    @Query("SELECT t FROM Transaction t where ?1 in t.accountFrom.users")
    //    List<Transaction> findTransactionForUser(User user, Pageable pageable);
}
