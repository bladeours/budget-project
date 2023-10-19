package com.budget.project.controller;

import com.budget.project.model.db.Transaction;
import com.budget.project.model.dto.request.TransactionInput;
import com.budget.project.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    //    @QueryMapping
    //    public List<Transaction> getTransactions(@Argument Page page) {
    //        return transactionService.getTransactions(page);
    //    }

    @MutationMapping
    public Transaction addTransaction(@Argument TransactionInput transactionInput) {
        return transactionService.createTransaction(transactionInput);
    }
    //
    //    @MutationMapping
    //    public boolean deleteAccount(@Argument String hash) {
    //        accountService.deleteAccount(hash);
    //        return true;
    //    }

}
