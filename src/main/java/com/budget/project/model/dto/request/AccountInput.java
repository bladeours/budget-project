package com.budget.project.model.dto.request;

import com.budget.project.model.db.AccountType;
import com.budget.project.model.db.Currency;
import lombok.Builder;

@Builder
public record AccountInput(
        AccountType accountType,
        Double balance,
        String color,
        Currency currency,
        String description,
        String name,
        String parentHash) {}
