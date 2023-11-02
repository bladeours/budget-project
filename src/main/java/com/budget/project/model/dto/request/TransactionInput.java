package com.budget.project.model.dto.request;

import com.budget.project.model.db.Currency;
import com.budget.project.model.db.TransactionType;
import lombok.Builder;
import org.springframework.lang.NonNull;

@Builder(toBuilder = true)
public record TransactionInput(
        String name,
        String note,
        @NonNull Double amount,
        @NonNull String date,
        Boolean need,
        String accountToHash,
        String accountFromHash,
        @NonNull TransactionType transactionType,
        String categoryHash,
        @NonNull Currency currency) {}
