package com.budget.project.model.db;

import com.budget.project.utils.YearMonthDateAttributeConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;

@Data
@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Budget {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private Double plannedBudget;

    @Column(nullable = false)
    @Convert(converter = YearMonthDateAttributeConverter.class)
    private YearMonth date;

    @Column(nullable = false, unique = true)
    private String hash;

    @ManyToOne
    private User user;

    @ManyToOne
    private Trip trip;

    @ManyToOne
    @JoinColumn(nullable = false, name = "category_id")
    private Category category;
}
