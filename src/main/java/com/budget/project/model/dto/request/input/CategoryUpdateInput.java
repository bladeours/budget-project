package com.budget.project.model.dto.request.input;

import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record CategoryUpdateInput(
        String name, String color, Boolean archived, List<SubCategoryInput> subCategories) {}
