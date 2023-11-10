package com.budget.project.model.dto.request.input;

import lombok.Builder;

@Builder(toBuilder = true)
public record CategoryInput(
        String name, String color, Boolean income, String parentHash, Boolean archived) {}
