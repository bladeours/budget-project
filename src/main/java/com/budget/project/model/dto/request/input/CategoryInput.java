package com.budget.project.model.dto.request.input;

import lombok.Builder;

@Builder
public record CategoryInput(
        String name, String color, Boolean income, Long parentId, Boolean archived) {}
