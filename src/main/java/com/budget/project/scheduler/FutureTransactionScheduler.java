package com.budget.project.scheduler;

import com.budget.project.model.db.Account;
import com.budget.project.model.db.Transaction;
import com.budget.project.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class FutureTransactionScheduler {

    private final TransactionService transactionService;
    @Async
    @Scheduled(cron = "0 0 0 * * ?")
    //TODO write tests
    public void addFutureTransactionToBalance() {
        for (Transaction transaction: transactionService.getFutureTransactions()){
            switch (transaction.getTransactionType()){
                case EXPENSE ->
                    subtractFromBalance(transaction.getAccountFrom(), transaction.getAmount());

                case INCOME -> {
                    addToBalance(transaction.getAccountTo(), transaction.getAmount());
                }
                case TRANSFER -> {
                    subtractFromBalance(transaction.getAccountFrom(), transaction.getAmount());
                    addToBalance(transaction.getAccountTo(), transaction.getAmount());
                }
            }
        }
    }

    private void subtractFromBalance(Account account, Double amount) {
        account.setBalance(account.getBalance() - amount);
    }

    private void addToBalance(Account account, Double amount) {
        account.setBalance(account.getBalance() + amount);
    }
}
