package com.budget.project.utils;

import jakarta.persistence.AttributeConverter;

import java.sql.Date;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Objects;

public class YearMonthDateAttributeConverter implements AttributeConverter<YearMonth, Date> {
    @Override
    public Date convertToDatabaseColumn(YearMonth yearMonth) {
        if (Objects.nonNull(yearMonth)) {
            return Date.valueOf(yearMonth.atDay(1));
        }
        return null;
    }

    @Override
    public YearMonth convertToEntityAttribute(Date date) {
        if (Objects.nonNull(date)) {
            return YearMonth.from(Instant
                    .ofEpochMilli(date.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate());
        }
        return null;
    }
}
