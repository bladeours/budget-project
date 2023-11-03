package com.budget.project.service;

import com.budget.project.exception.AppException;
import com.budget.project.filter.model.Filter;
import com.budget.project.filter.service.FilterService;
import com.budget.project.model.db.Category;
import com.budget.project.model.db.Transaction;
import com.budget.project.model.dto.request.CategoryInput;
import com.budget.project.model.dto.request.CustomPage;
import com.budget.project.service.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final FilterService filterService;
    private final TransactionService transactionService;

    @SneakyThrows
    public Category getCategory(String hash) {
        return categoryRepository
                .findCategoryByHashAndUsersContainingIgnoreCase(hash, userService.getLoggedUser())
                .orElseThrow(
                        () -> {
                            log.debug("can't find category with hash: {}", hash);
                            return new AppException(
                                    "can't find category with hash: " + hash, HttpStatus.NOT_FOUND);
                        });
    }

    public Category createCategory(CategoryInput categoryInput) {
        Category category = Category.of(categoryInput, userService.getLoggedUser());
        category = categoryRepository.save(category);
        userService.getLoggedUser().getCategories().add(category);
        return category;
    }

    public Page<Category> getCategoriesPage(CustomPage page, Filter filter) {
        if (Objects.isNull(filter)) {
            return categoryRepository.findAllByUsersContaining(
                    PageRequest.of(page.number(), page.size()), userService.getLoggedUser());
        }
        return categoryRepository.findAll(
                filterService.getSpecification(filter, Category.class),
                PageRequest.of(page.number(), page.size()));
    }

    public void deleteCategory(String hash) {
        Category category = this.getCategory(hash);
        for (Transaction transaction : category.getTransactions()) {
            transactionService.deleteTransaction(transaction);
        }
        categoryRepository.delete(category);
    }
}
