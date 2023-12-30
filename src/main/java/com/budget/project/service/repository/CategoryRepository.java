package com.budget.project.service.repository;

import com.budget.project.model.db.Category;
import com.budget.project.model.db.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository
        extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {
    Optional<Category> findCategoryByHashAndUsersContainingIgnoreCase(String hash, User user);

    Optional<Category> findCategoryByNameAndUsersContainingIgnoreCaseAndIncome(
            String hash, User user, Boolean income);

    Page<Category> findAllByUsersContaining(PageRequest pageRequest, User user);

    List<Category> findAllByUsersContaining(User user);
}
