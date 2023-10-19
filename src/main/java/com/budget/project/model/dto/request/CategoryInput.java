package com.budget.project.model.dto.request;

import lombok.Builder;

@Builder
public record CategoryInput(String name, String color, Boolean income, Long parentId) {}
