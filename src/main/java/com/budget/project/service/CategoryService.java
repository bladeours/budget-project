package com.budget.project.service;

import com.budget.project.model.db.Category;
import com.budget.project.model.dto.request.CategoryInput;
import com.budget.project.service.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    public Optional<Category> getCategory(String hash) {
        return categoryRepository.findCategoryByHashAndUsersContainingIgnoreCase(
                hash, userService.getLoggedUser());
    }

    public Category createCategory(CategoryInput categoryInput) {
        Category category = Category.of(categoryInput, userService.getLoggedUser());
        category = categoryRepository.save(category);
        userService.getLoggedUser().getCategories().add(category);
        return category;
    }
}
