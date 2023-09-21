package com.budget.project.model.db;

public enum AccountType {
    REGULAR("regular"), SAVINGS("savings");

    public final String type;

    AccountType(String type) {
        this.type = type;
    }
}
