package com.budget.project.model.dto.request.input;

import com.budget.project.model.db.AccountType;
import com.budget.project.model.db.Currency;

import lombok.Builder;

@Builder(toBuilder = true)
public record AccountInput(
        AccountType accountType,
        Double balance,
        String color,
        Currency currency,
        String description,
        String name,
        String parentHash,
        Boolean archived) {}
