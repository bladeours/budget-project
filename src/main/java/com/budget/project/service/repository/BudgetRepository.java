package com.budget.project.service.repository;

import com.budget.project.model.db.Budget;

import com.budget.project.model.db.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findAllByDateAndUser(YearMonth date, User user);
    Optional<Budget> findByHashAndUser(String hash, User user);
}
