package com.budget.project.service;

import static com.budget.project.utils.CSVUtils.*;

import com.budget.project.exception.AppException;
import com.budget.project.model.db.TransactionType;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OneMoneyService {

    @SneakyThrows
    public InputStream transformFile(MultipartFile file) {
        StringWriter sw = new StringWriter();
        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(
                DATE_HEADER,
                TYPE_HEADER,
                FROM_ACCOUNT_CATEGORY_HEADER,
                TO_ACCOUNT_CATEGORY_HEADER,
                AMOUNT_HEADER,
                NOTE_HEADER,
                NEED_HEADER);

        Reader reader = new InputStreamReader(file.getInputStream());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT
                .withHeader(
                        "DATE",
                        "TYPE",
                        "FROM ACCOUNT",
                        "TO ACCOUNT / TO CATEGORY",
                        "AMOUNT",
                        "CURRENCY",
                        "AMOUNT 2",
                        "CURRENCY 2",
                        "TAGS",
                        "NOTES")
                .withSkipHeaderRecord()
                .withAllowMissingColumnNames(true)
                .parse(reader);
        try (final CSVPrinter printer = new CSVPrinter(sw, csvFormat)) {
            records.forEach((record) -> {
                try {
                    printer.printRecord(transformRecord(record));
                } catch (IOException e) {
                    log.warn("problem with printing to csv: {}", e.getMessage());
                    throw new AppException("CSV issue", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            });
        }
        return new ByteArrayInputStream(sw.toString().getBytes());
    }

    private List<String> transformRecord(CSVRecord record) {
        return List.of(
                transformDate(record.get("DATE")),
                record.get("TYPE").toUpperCase(),
                getFromAccountOrCategory(record),
                getToAccountOrCategory(record),
                record.get("AMOUNT"),
                record.get("NOTES"),
                "true");
    }

    private String getToAccountOrCategory(CSVRecord record) {
        switch (TransactionType.valueOf(record.get("TYPE").toUpperCase())) {
            case INCOME -> {
                return record.get("FROM ACCOUNT");
            }
            case EXPENSE, TRANSFER -> {
                return transformCategory(record.get("TO ACCOUNT / TO CATEGORY"));
            }
            default -> throw new AppException("bad type", HttpStatus.BAD_REQUEST);
        }
    }

    private String getFromAccountOrCategory(CSVRecord record) {
        switch (TransactionType.valueOf(record.get("TYPE").toUpperCase())) {
            case INCOME -> {
                return transformCategory(record.get("TO ACCOUNT / TO CATEGORY"));
            }
            case EXPENSE, TRANSFER -> {
                return record.get("FROM ACCOUNT");
            }
            default -> throw new AppException("bad type", HttpStatus.BAD_REQUEST);
        }
    }

    private String transformDate(String date) {
        SimpleDateFormat fromOneMoney = new SimpleDateFormat("MM/dd/yy");
        SimpleDateFormat myFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            return myFormat.format(fromOneMoney.parse(date));
        } catch (ParseException e) {
            log.warn("problem with parsing date: {}", e.getMessage());
            throw new AppException("bad date format", HttpStatus.BAD_REQUEST);
        }
    }

    private String transformCategory(String categoryName){
        if(categoryName.indexOf("(") > 0) {
            return categoryName.substring(0, categoryName.indexOf("(") - 1);
        }
        return categoryName;
    }
}
