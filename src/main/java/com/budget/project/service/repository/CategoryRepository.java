package com.budget.project.service.repository;

import com.budget.project.model.db.Category;
import com.budget.project.model.db.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findCategoryByHashAndUsersContainingIgnoreCase(String hash, User user);
}
