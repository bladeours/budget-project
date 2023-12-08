package com.budget.project.controller;

import com.budget.project.filter.model.Filter;
import com.budget.project.model.db.Transaction;
import com.budget.project.model.dto.CustomPage;
import com.budget.project.model.dto.request.input.TransactionInput;
import com.budget.project.service.TransactionService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    @QueryMapping
    public Page<Transaction> getTransactionsPage(
            @Argument CustomPage page, @Argument Filter filter) {
        return transactionService.getTransactionsPage(page, filter);
    }

    @QueryMapping
    public Transaction getTransaction(@Argument String hash) {
        return transactionService.getTransaction(hash);
    }

    @MutationMapping
    public Transaction addTransaction(@Argument TransactionInput transactionInput) {
        return transactionService.createTransaction(transactionInput);
    }

    @MutationMapping
    public Transaction updateTransaction(
            @Argument TransactionInput transactionInput, @Argument String hash) {
        return transactionService.updateTransaction(hash, transactionInput);
    }

    @MutationMapping
    public boolean deleteTransaction(@Argument String hash) {
        transactionService.deleteTransaction(hash);
        return true;
    }
}
