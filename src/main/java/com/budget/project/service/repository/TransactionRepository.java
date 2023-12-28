package com.budget.project.service.repository;

import com.budget.project.model.db.Transaction;
import com.budget.project.model.db.User;
import com.budget.project.service.projection.DayExpense;
import com.budget.project.service.projection.MonthExpense;
import com.budget.project.service.projection.TransactionCategoryNameSum;
import com.budget.project.service.projection.TransactionCategorySum;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository
        extends JpaRepository<Transaction, Integer>, JpaSpecificationExecutor<Transaction> {

    @Query("SELECT t FROM Transaction t "
            + "LEFT JOIN t.accountFrom af LEFT JOIN t.accountTo at "
            + "WHERE :user MEMBER OF af.users OR :user MEMBER OF at.users")
    Page<Transaction> findTransactionsForUser(User user, PageRequest pageRequest);

    @Query("SELECT t FROM Transaction t LEFT JOIN t.accountFrom af LEFT JOIN t.accountTo at WHERE"
            + " (:user MEMBER OF af.users OR :user MEMBER OF at.users) AND t.hash like :hash")
    Optional<Transaction> findByHashForUser(String hash, User user);

    @Query(
            "SELECT t.category.name as categoryName, sum(t.amount) as sumForCategory, t.category.color as categoryColor FROM Transaction t"
                    + " where (:user MEMBER OF t.category.users) and (t.category.income = :income) and "
                    + "(t.date between :startDate and :endDate)"
                    + " group by t.category.name order by sum(t.amount)"
                    + " desc ")
    List<TransactionCategoryNameSum> sumTransactionAmountForCategoriesNameAndUser(
            Boolean income, User user, LocalDateTime startDate, LocalDateTime endDate);

    List<Transaction> findAllByFutureTrue();

    @Query("SELECT t.category as category, sum(t.amount) as sumForCategory FROM Transaction t"
            + " where (:user MEMBER OF t.category.users) and "
            + "(t.date between :startDate and :endDate)"
            + " group by t.category.hash order by sum(t.amount)"
            + " desc ")
    List<TransactionCategorySum> sumTransactionAmountForCategoriesAndUser(
            User user, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT sum(t.amount) FROM Transaction t"
            + " where (:user MEMBER OF t.category.users) and "
            + "(t.date between :startDate and :endDate) and (t.transactionType = 'INCOME')"
            + " order by sum(t.amount)"
            + " desc ")
    Optional<Double> getIncome(LocalDateTime startDate, LocalDateTime endDate, User user);

    @Query("SELECT sum(t.amount) FROM Transaction t"
            + " where (:user MEMBER OF t.category.users) and "
            + "(t.date between :startDate and :endDate) and (t.transactionType = 'EXPENSE')"
            + " order by sum(t.amount)"
            + " desc ")
    Optional<Double> getExpense(LocalDateTime startDate, LocalDateTime endDate, User user);

    @Query("SELECT sum(t.amount) as expense, EXTRACT(day FROM t.date) as day FROM Transaction t"
            + " where (:user MEMBER OF t.category.users) and "
            + "(t.date between :startDate and :endDate) and (t.transactionType = 'EXPENSE')"
            + " GROUP BY EXTRACT(day FROM t.date) order by sum(t.amount)"
            + " desc ")
    List<DayExpense> getExpensesPerDayOfTheWeek(
            LocalDateTime startDate, LocalDateTime endDate, User user);

    @Query("SELECT sum(t.amount) as expense, EXTRACT(month FROM t.date) as month FROM Transaction t"
            + " where (:user MEMBER OF t.category.users) and "
            + "(t.date between :startDate and :endDate) and (t.transactionType = 'EXPENSE')"
            + " GROUP BY EXTRACT(month FROM t.date) order by sum(t.amount)"
            + " desc ")
    List<MonthExpense> getExpensesMonth(LocalDateTime startDate, LocalDateTime endDate, User user);
}
