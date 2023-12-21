package com.budget.project.utils;

import com.budget.project.service.UserService;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Objects;

@Converter
@Component
public class YearMonthDateAttributeConverter implements AttributeConverter<YearMonth, Date> {
    private final UserService userService;

    public YearMonthDateAttributeConverter(@Lazy UserService userService) {
        this.userService = userService;
    }

    @Override
    public Date convertToDatabaseColumn(YearMonth yearMonth) {
        Integer firstDayOfTheMonth =
                userService.getLoggedUser().getSettings().getFirstDayOfTheMonth();
        if (Objects.nonNull(yearMonth)) {
            return Date.valueOf(yearMonth.atDay(firstDayOfTheMonth));
        }
        return null;
    }

    @Override
    public YearMonth convertToEntityAttribute(Date date) {
        Integer firstDayOfTheMonth =
                userService.getLoggedUser().getSettings().getFirstDayOfTheMonth();
        if (Objects.nonNull(date)) {
            LocalDate localDate = Instant.ofEpochMilli(date.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            if (localDate.getDayOfMonth() < firstDayOfTheMonth) {
                localDate = localDate.minusMonths(1);
            }
            return YearMonth.from(localDate);
        }
        return null;
    }
}
