package com.budget.project.utils;

import com.budget.project.exception.AppException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class DateUtils {
    public static LocalDateTime parse(String date) {
        try {
            return LocalDateTime.parse(date, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (Exception ex) {
            log.warn("problem with date parsing: " + ex);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static LocalDateTime parse(String date, String format) {
        try {
            LocalDate dateTime = LocalDate.parse(date, DateTimeFormatter.ofPattern(format));
            return LocalDateTime.of(dateTime, LocalTime.MIN);
        } catch (Exception ex) {
            log.warn("problem with date parsing: " + ex);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
