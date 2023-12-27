package com.budget.project.service.repository;

import com.budget.project.model.db.PlannedIncome;
import com.budget.project.model.db.User;

import java.time.YearMonth;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlannedIncomeRepository
        extends JpaRepository<PlannedIncome, Long> {

    Optional<PlannedIncome> findByDateAndUser(YearMonth date, User user);
    Optional<PlannedIncome> findByHashAndUser(String hash, User user);

}
