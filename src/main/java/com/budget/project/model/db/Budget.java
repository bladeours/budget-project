package com.budget.project.model.db;

import com.budget.project.utils.YearMonthDateAttributeConverter;

import jakarta.persistence.*;

import lombok.*;

import java.time.YearMonth;

@Data
@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Budget {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private Double plannedBudget;

    @Column(nullable = false)
    @Convert(converter = YearMonthDateAttributeConverter.class)
    private YearMonth date;

    @EqualsAndHashCode.Include
    @Column(nullable = false, unique = true)
    private String hash;

    @ManyToOne(cascade = CascadeType.DETACH)
    private User user;

    @ManyToOne
    private Trip trip;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(nullable = false, name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "sub_category_id")
    private SubCategory subCategory;
}
