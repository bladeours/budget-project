package com.budget.project.service;

import com.budget.project.exception.AppException;
import com.budget.project.model.db.Category;
import com.budget.project.model.dto.request.CategoryInput;
import com.budget.project.service.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    @SneakyThrows
    public Category getCategory(String hash) {
        return categoryRepository.findCategoryByHashAndUsersContainingIgnoreCase(
                hash, userService.getLoggedUser()).orElseThrow(
                () -> {
                    log.debug("can't find category with hash: {}", hash);
                    return new AppException("can't find category with hash: " + hash, HttpStatus.NOT_FOUND);
                });
    }

    public Category createCategory(CategoryInput categoryInput) {
        Category category = Category.of(categoryInput, userService.getLoggedUser());
        category = categoryRepository.save(category);
        userService.getLoggedUser().getCategories().add(category);
        return category;
    }
}
