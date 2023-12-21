package com.budget.project.service.projection;

import com.budget.project.model.db.Category;

public interface TransactionCategorySum {
    Category getCategory();

    Double getSumForCategory();
}
