package com.budget.project.controller;

import com.budget.project.filter.model.Filter;
import com.budget.project.model.db.Category;
import com.budget.project.model.dto.request.CustomPage;
import com.budget.project.model.dto.request.input.CategoryInput;
import com.budget.project.model.dto.request.input.CategoryUpdateInput;
import com.budget.project.service.CategoryService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @QueryMapping
    public Page<Category> getCategoriesPage(@Argument CustomPage page, @Argument Filter filter) {
        return categoryService.getCategoriesPage(page, filter);
    }

    @QueryMapping
    public List<Category> getCategories(@Argument Filter filter) {
        return categoryService.getCategories(filter);
    }

    @QueryMapping
    public Category getCategory(@Argument String hash) {
        return categoryService.getCategory(hash);
    }

    @MutationMapping
    public Category addCategory(@Argument CategoryInput categoryInput) {
        return categoryService.createCategory(categoryInput);
    }

    @MutationMapping
    public Boolean deleteCategory(@Argument String hash) {
        categoryService.deleteCategory(hash);
        return true;
    }

    @MutationMapping
    public Category updateCategory(
            @Argument String hash, @Argument CategoryUpdateInput categoryUpdateInput) {
        return categoryService.updateCategory(hash, categoryUpdateInput);
    }
}
