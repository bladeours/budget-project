package com.budget.project.service;

import com.budget.project.model.db.*;
import com.budget.project.model.dto.request.input.AccountInput;
import com.budget.project.model.dto.request.input.CategoryInput;
import com.budget.project.model.dto.request.input.TransactionInput;
import com.budget.project.utils.DateUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Optional;

import static com.budget.project.utils.CSVUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ImportService {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final CategoryService categoryService;
    private final OneMoneyService oneMoneyService;


    @SneakyThrows
    public void importCSV(MultipartFile file) {
        importCSV(file.getInputStream());
    }

    @SneakyThrows
    private void importCSV(InputStream inputStream) {
        Reader reader = new InputStreamReader(inputStream);
        Iterable<CSVRecord> records = CSVFormat.DEFAULT
                .withHeader(
                        DATE_HEADER,
                        TYPE_HEADER,
                        FROM_ACCOUNT_CATEGORY_HEADER,
                        TO_ACCOUNT_CATEGORY_HEADER,
                        AMOUNT_HEADER,
                        NOTE_HEADER,
                        NEED_HEADER)
                .withSkipHeaderRecord()
                .parse(reader);
        for (CSVRecord record : records) {
            createTransaction(record);
        }
    }

    private void createTransaction(CSVRecord record) {
        LocalDateTime localDateTime = DateUtils.parse(record.get(DATE_HEADER), "dd/MM/yyyy");
        TransactionType transactionType = TransactionType.valueOf(record.get(TYPE_HEADER));
        TransactionInput.TransactionInputBuilder transactionInputBuilder =
                TransactionInput.builder()
                        .date(OffsetDateTime.of(localDateTime, ZoneOffset.UTC).toString())
                        .name("")
                        .need(record.get(NEED_HEADER).equals("true"))
                        .currency(Currency.PLN)
                        .amount(Double.valueOf(record.get(AMOUNT_HEADER)))
                        .note(record.get(NOTE_HEADER))
                        .transactionType(transactionType);

        switch (transactionType) {
            case EXPENSE -> transactionInputBuilder =
                    handleExpense(transactionInputBuilder, record);
            case INCOME -> transactionInputBuilder = handleIncome(transactionInputBuilder, record);
            case TRANSFER -> transactionInputBuilder = handleTransfer(transactionInputBuilder, record);
        }

        transactionService.createTransaction(transactionInputBuilder.build());
    }

    private TransactionInput.TransactionInputBuilder handleTransfer(TransactionInput.TransactionInputBuilder builder, CSVRecord record) {
        Account accountFrom = getAccount(record.get(FROM_ACCOUNT_CATEGORY_HEADER));
        Account accountTo = getAccount(record.get(TO_ACCOUNT_CATEGORY_HEADER));
        return builder.accountFromHash(accountFrom.getHash()).accountToHash(accountTo.getHash());
    }

    private TransactionInput.TransactionInputBuilder handleIncome(
            TransactionInput.TransactionInputBuilder builder, CSVRecord record) {
        return handleIncomeOrExpense(
                builder, record, TO_ACCOUNT_CATEGORY_HEADER, FROM_ACCOUNT_CATEGORY_HEADER, true);
    }

    private TransactionInput.TransactionInputBuilder handleExpense(
            TransactionInput.TransactionInputBuilder builder, CSVRecord record) {
        return handleIncomeOrExpense(
                builder, record, FROM_ACCOUNT_CATEGORY_HEADER, TO_ACCOUNT_CATEGORY_HEADER, false);
    }

    private TransactionInput.TransactionInputBuilder handleIncomeOrExpense(
            TransactionInput.TransactionInputBuilder builder,
            CSVRecord record,
            String accountHeader,
            String categoryHeader,
            boolean income) {
        Account account = getAccount(record.get(accountHeader));

        if (income) {
            builder.accountToHash(account.getHash());
        } else {
            builder.accountFromHash(account.getHash());
        }

        Category category = categoryService.getCategoryByName(record.get(categoryHeader)).orElse(categoryService.createCategory(CategoryInput.builder()
                .name(record.get(categoryHeader))
                .income(income)
                .subCategories(new ArrayList<>())
                .color("#CCFF1A")
                .build()));


        return builder.categoryHash(category.getHash());
    }

    private Account getAccount(String name) {
        return accountService.getAccountByName(name).orElseGet(() -> accountService.createAccount(AccountInput.builder()
                .accountType(AccountType.REGULAR)
                .balance(0D)
                .description("")
                .color("#CCFF1A")
                .archived(false)
                .currency(Currency.PLN)
                .name(name)
                .build()));

    }

    public void importFromOneMoney(MultipartFile file) {
        importCSV(oneMoneyService.transformFile(file));
    }
}
