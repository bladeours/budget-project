package com.budget.project.service.repository;

import com.budget.project.model.db.Category;
import com.budget.project.model.db.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CategoryRepository
        extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {
    Optional<Category> findCategoryByHashAndUsersContainingIgnoreCase(String hash, User user);

    Page<Category> findAllByUsersContaining(PageRequest pageRequest, User user);
}
