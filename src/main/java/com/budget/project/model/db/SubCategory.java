package com.budget.project.model.db;

import jakarta.persistence.*;

import lombok.*;

import java.util.UUID;

@Entity
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SubCategory {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    private String color;

    @EqualsAndHashCode.Include
    @Column(nullable = false, unique = true)
    private String hash;

    @JoinColumn(nullable = false, name = "parent_id")
    @ManyToOne(cascade = CascadeType.DETACH)
    @ToString.Exclude
    private Category parent;

    public static SubCategory of(String name, Category parent) {
        return SubCategory.builder()
                .name(name)
                .color(parent.getColor())
                .hash(UUID.randomUUID().toString())
                .parent(parent)
                .build();
    }
}
