package com.budget.project.model.db;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Trip {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    private Date startDate;

    private Date endDate;

    @Column(nullable = false)
    private String hash;

    @ManyToMany(mappedBy = "trips")
    Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "trip")
    Set<Budget> budgets = new HashSet<>();
}
