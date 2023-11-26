package com.budget.project.service;

import com.budget.project.exception.AppException;
import com.budget.project.filter.model.Filter;
import com.budget.project.filter.service.FilterService;
import com.budget.project.model.db.Category;
import com.budget.project.model.db.Transaction;
import com.budget.project.model.dto.request.CustomPage;
import com.budget.project.model.dto.request.input.CategoryInput;
import com.budget.project.model.dto.request.input.CategoryUpdateInput;
import com.budget.project.service.repository.CategoryRepository;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

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
                .orElseThrow(() -> {
                    log.debug("can't find category with hash: {}", hash);
                    return new AppException(
                            "can't find category with hash: " + hash, HttpStatus.NOT_FOUND);
                });
    }

    public Category createCategory(CategoryInput categoryInput) {
        Category category;
        if (Objects.nonNull(categoryInput.parentHash())) {
            Category parent = this.getCategory(categoryInput.parentHash());
            if (Objects.nonNull(parent.getParent())) {
                log.warn("only one level of subCategories is possible");
                throw new AppException(
                        "only one level of subCategories is possible", HttpStatus.BAD_REQUEST);
            }
            if (!categoryInput.income().equals(parent.getIncome())) {
                log.warn("subCategory must have the same type");
                throw new AppException(
                        "subCategory must have the same type", HttpStatus.BAD_REQUEST);
            }
            category = Category.of(categoryInput, userService.getLoggedUser(), parent);
            category = categoryRepository.save(category);
            if (Objects.isNull(parent.getSubCategories())) {
                parent.setSubCategories(Set.of(category));
            } else {
                parent.getSubCategories().add(category);
            }
        } else {
            category = Category.of(categoryInput, userService.getLoggedUser());
            category = categoryRepository.save(category);
        }
        userService.getLoggedUser().getCategories().add(category);
        return category;
    }

    public Page<Category> getCategoriesPage(CustomPage page, Filter filter) {
        if (Objects.isNull(filter) || Objects.isNull(filter.logicOperator())) {
            return categoryRepository.findAllByUsersContaining(
                    PageRequest.of(page.number(), page.size()), userService.getLoggedUser());
        }
        return categoryRepository.findAll(
                filterService.getSpecification(filter, Category.class),
                PageRequest.of(page.number(), page.size()));
    }

    public List<Category> getCategories(Filter filter) {
        if (Objects.isNull(filter) || Objects.isNull(filter.logicOperator())) {
            return categoryRepository.findAllByUsersContaining(userService.getLoggedUser());
        }
        return categoryRepository.findAll(
                filterService.getSpecification(filter, Category.class));
    }

    public void deleteCategory(String hash) {
        Category category = this.getCategory(hash);
        for (Category child : category.getSubCategories()) {
            deleteCategory(child.getHash());
        }
        for (Transaction transaction : category.getTransactions()) {
            transactionService.deleteTransaction(transaction);
        }
        userService.getLoggedUser().getCategories().remove(category);
        categoryRepository.delete(category);
    }

    public Category updateCategory(String hash, CategoryUpdateInput categoryUpdateInput) {
        Category category = this.getCategory(hash);
        category = category.toBuilder()
                .name(categoryUpdateInput.name())
                .color(categoryUpdateInput.color())
                .archived(categoryUpdateInput.archived())
                .build();
        return categoryRepository.save(category);
    }


}
