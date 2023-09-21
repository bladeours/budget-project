package com.budget.project.model.db;

public enum Currency {

    PLN("zł"),
    EUR("€"),
    USD("$");

    final public String symbol;
    Currency(String symbol) {
        this.symbol = symbol;
    }
}
