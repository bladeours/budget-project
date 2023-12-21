package com.budget.project.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.budget.project.model.db.Settings;
import com.budget.project.model.db.User;
import com.budget.project.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Date;
import java.time.YearMonth;

@SpringBootTest
@ActiveProfiles("test")
class YearMonthDateAttributeConverterTest {
    @MockBean
    private UserService userService;

    @Autowired
    private YearMonthDateAttributeConverter yearMonthDateAttributeConverter;

    @BeforeEach
    void setup() {
        Settings settings = new Settings();
        settings.setFirstDayOfTheMonth(10);
        User user = User.builder().settings(settings).build();
        Mockito.when(userService.getLoggedUser()).thenReturn(user);
    }

    @Test
    void shouldConvertToDatabaseColumnWithProperFirstDayOfTheMonth() {
        YearMonth yearMonth = YearMonth.of(2023, 1);
        Date actualDate = yearMonthDateAttributeConverter.convertToDatabaseColumn(yearMonth);
        assertThat(actualDate).isEqualTo(Date.valueOf("2023-01-10"));
    }

    @Test
    void shouldConvertToDatabaseColumnWithProperFirstDayOfTheMonth_whenIsFirst() {
        Settings settings = new Settings();
        settings.setFirstDayOfTheMonth(1);
        User user = User.builder().settings(settings).build();
        Mockito.when(userService.getLoggedUser()).thenReturn(user);
        YearMonth yearMonth = YearMonth.of(2023, 1);
        Date actualDate = yearMonthDateAttributeConverter.convertToDatabaseColumn(yearMonth);
        assertThat(actualDate).isEqualTo(Date.valueOf("2023-01-01"));
    }

    @Test
    void shouldConvertToEntityAttribute_whenDayIsLaterThanFirstDayOfTheMonth() {
        YearMonth yearMonth = YearMonth.of(2023, 1);
        YearMonth actualYearMonth = yearMonthDateAttributeConverter.convertToEntityAttribute(
                Date.valueOf("2023-01-10"));
        assertThat(actualYearMonth).isEqualTo(yearMonth);
    }

    @Test
    void shouldConvertToEntityAttribute_whenDayIsNotLaterThanFirstDayOfTheMonth() {
        YearMonth yearMonth = YearMonth.of(2022, 12);
        YearMonth actualYearMonth = yearMonthDateAttributeConverter.convertToEntityAttribute(
                Date.valueOf("2023-01-09"));
        assertThat(actualYearMonth).isEqualTo(yearMonth);
    }

    @Test
    void shouldConvertToEntityAttribute_whenFirstDayOfTheMonthIsFirst() {
        Settings settings = new Settings();
        settings.setFirstDayOfTheMonth(1);
        User user = User.builder().settings(settings).build();
        Mockito.when(userService.getLoggedUser()).thenReturn(user);
        YearMonth yearMonth = YearMonth.of(2023, 1);
        YearMonth actualYearMonth = yearMonthDateAttributeConverter.convertToEntityAttribute(
                Date.valueOf("2023-01-01"));
        assertThat(actualYearMonth).isEqualTo(yearMonth);
    }
}
