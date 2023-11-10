package com.budget.project.model.dto.request.input;

import lombok.Builder;

@Builder(toBuilder = true)
public record CategoryUpdateInput(String name, String color, Boolean archived) {}
