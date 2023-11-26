package com.budget.project.utils;

import com.budget.project.exception.AppException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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
}
