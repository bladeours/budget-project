package com.budget.project.model.db;

import com.budget.project.model.dto.request.input.CategoryInput;

import jakarta.persistence.*;

import lombok.*;

import java.util.*;

@Entity
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Category {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    private String color;

    @EqualsAndHashCode.Include
    @Column(nullable = false, unique = true)
    private String hash;

    @Column(nullable = false)
    private Boolean income;

    @ManyToOne
    @ToString.Exclude
    private Category parent;

    @OneToMany
    private Set<Category> subCategories = new HashSet<>();

    @Column(nullable = false)
    private Boolean archived;

    @ManyToMany(mappedBy = "categories")
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.DETACH)
    private Set<Transaction> transactions = new HashSet<>();

    @OneToMany(mappedBy = "category")
    private Set<Budget> budgets = new HashSet<>();

    public static Category of(CategoryInput categoryInput, User user) {
        return Category.builder()
                .name(categoryInput.name())
                .color(categoryInput.color())
                .income(categoryInput.income())
                .users(Set.of(user))
                .hash(UUID.randomUUID().toString())
                .archived(categoryInput.archived())
                .build();
    }

    public static Category of(CategoryInput categoryInput, User user, Category parent) {
        return Category.of(categoryInput, user).toBuilder().parent(parent).build();
    }
}
