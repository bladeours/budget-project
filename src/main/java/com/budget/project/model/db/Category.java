package com.budget.project.model.db;

import com.budget.project.model.dto.request.CategoryInput;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id @GeneratedValue private Long id;

    @Column(nullable = false)
    private String name;

    private String color;

    @Column(nullable = false, unique = true)
    private String hash;

    @Column(nullable = false)
    private Boolean income;

    private Long parentId;

    @ManyToMany(mappedBy = "categories")
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "category")
    private Set<Transaction> transactions = new HashSet<>();

    @OneToMany(mappedBy = "category")
    private Set<Budget> budgets = new HashSet<>();

    public static Category of(CategoryInput categoryInput, User user) {
        return Category.builder()
                .name(categoryInput.name())
                .color(categoryInput.color())
                .income(categoryInput.income())
                .parentId(categoryInput.parentId())
                .users(Set.of(user))
                .hash(UUID.randomUUID().toString())
                .build();
    }
}
