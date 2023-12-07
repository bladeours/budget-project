package com.budget.project.model.dto.request.input;

import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record CategoryInput(
        String name, String color, Boolean income, List<SubCategoryInput> subCategories) {}
