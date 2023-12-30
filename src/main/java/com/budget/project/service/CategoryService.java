package com.budget.project.service;

import com.budget.project.exception.AppException;
import com.budget.project.filter.model.Filter;
import com.budget.project.filter.service.FilterService;
import com.budget.project.model.db.Category;
import com.budget.project.model.db.SubCategory;
import com.budget.project.model.db.Transaction;
import com.budget.project.model.dto.CustomPage;
import com.budget.project.model.dto.request.input.CategoryInput;
import com.budget.project.model.dto.request.input.CategoryUpdateInput;
import com.budget.project.model.dto.request.input.SubCategoryInput;
import com.budget.project.service.repository.CategoryRepository;
import com.budget.project.service.repository.SubCategoryRepository;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final FilterService filterService;
    private final TransactionService transactionService;
    private final SubCategoryRepository subCategoryRepository;

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
        if (getCategoryByNameAndIncome(categoryInput.name(), categoryInput.income())
                .isPresent()) {
            log.warn("there is category with name: {}", categoryInput.name());
            throw new AppException("bad category name", HttpStatus.BAD_REQUEST);
        }
        category = Category.of(categoryInput, userService.getLoggedUser());
        category = categoryRepository.save(category);
        for (SubCategoryInput subCategoryInput : categoryInput.subCategories()) {
            category = this.addSubCategory(subCategoryInput.name(), category);
        }
        userService.getLoggedUser().getCategories().add(category);
        return category;
    }

    private Category addSubCategory(String subCategoryName, Category parentCategory) {
        SubCategory subCategory = SubCategory.of(subCategoryName, parentCategory);
        subCategory = subCategoryRepository.save(subCategory);
        if (Objects.isNull(parentCategory.getSubCategories())) {
            parentCategory.setSubCategories(new HashSet<>(Set.of(subCategory)));
        } else {
            parentCategory.getSubCategories().add(subCategory);
        }
        return parentCategory;
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
        return categoryRepository.findAll(filterService.getSpecification(filter, Category.class));
    }

    public void deleteCategory(String hash) {
        Category category = this.getCategory(hash);

        if (Objects.nonNull(category.getSubCategories())) {
            for (SubCategory child : category.getSubCategories()) {
                deleteSubCategory(child);
            }
        }

        for (Transaction transaction : category.getTransactions()) {
            transactionService.deleteTransaction(transaction);
        }

        userService.getLoggedUser().getCategories().remove(category);
        categoryRepository.delete(category);
    }

    private void deleteSubCategory(SubCategory subCategory) {
        subCategory.getParent().getTransactions().stream()
                .filter(t -> Objects.nonNull(t.getSubCategory())
                        && t.getSubCategory().equals(subCategory))
                .forEach(transaction -> transaction.setSubCategory(null));

        subCategory.getParent().getSubCategories().remove(subCategory);
        subCategoryRepository.delete(subCategory);
    }

    public Category updateCategory(String hash, CategoryUpdateInput categoryUpdateInput) {
        Category category = this.getCategory(hash);
        category.getSubCategories().stream()
                .filter(c -> !categoryUpdateInput
                        .subCategories()
                        .contains(new SubCategoryInput(c.getName(), c.getHash())))
                .forEach(this::deleteSubCategory);

        Set<SubCategoryInput> subCategoriesToAdd = new HashSet<>();
        if (Objects.nonNull(categoryUpdateInput.subCategories())) {
            subCategoriesToAdd = categoryUpdateInput.subCategories().stream()
                    .filter(c -> Objects.isNull(c.hash()))
                    .collect(Collectors.toCollection(HashSet::new));
        }

        for (SubCategoryInput subCategoryInput : subCategoriesToAdd) {
            category = this.addSubCategory(subCategoryInput.name(), category);
        }

        category = category.toBuilder()
                .name(categoryUpdateInput.name())
                .color(categoryUpdateInput.color())
                .archived(categoryUpdateInput.archived())
                .build();
        return categoryRepository.save(category);
    }

    public SubCategory getSubCategory(String hash) {
        return subCategoryRepository
                .findByHashAndUser(hash, userService.getLoggedUser())
                .orElseThrow(() -> {
                    log.debug("can't find subCategory with hash: {}", hash);
                    return new AppException(
                            "can't find subCategory with hash: " + hash, HttpStatus.NOT_FOUND);
                });
    }

    public Optional<Category> getCategoryByNameAndIncome(String name, Boolean income) {
        return categoryRepository.findCategoryByNameAndUsersContainingIgnoreCaseAndIncome(
                name, userService.getLoggedUser(), income);
    }
}
