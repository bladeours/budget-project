package com.budget.project.model.db;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Double balance;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private Boolean archived;

    private Long parent_id;

    @Column(nullable = false, unique = true)
    private String hash;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @ManyToMany(mappedBy = "accounts")
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "accountFrom")
    private Set<Transaction> transactions = new HashSet<>();


}
