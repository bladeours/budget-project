package com.budget.project.model.db;

import lombok.Data;

@Data
public class Settings {
    private Boolean darkMode = true;
    private Integer firstDayOfTheMonth = 10;
}
