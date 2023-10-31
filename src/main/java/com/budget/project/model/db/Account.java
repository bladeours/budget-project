package com.budget.project.model.db;

import com.budget.project.model.dto.request.AccountInput;
import jakarta.persistence.*;
import java.util.*;

import lombok.*;

@Entity
@Data
@EqualsAndHashCode
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id @GeneratedValue private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Double balance;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private Boolean archived;

    @OneToMany
    private List<Account> subAccounts = new ArrayList<>();

    @ManyToOne
    private Account parent;

    @Column(nullable = false, unique = true)
    private String hash;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @ManyToMany(
            mappedBy = "accounts",
            cascade = {CascadeType.DETACH})
    private List<User> users = new ArrayList<>();

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "accountFrom")
    private Set<Transaction> transactions = new HashSet<>();

    public static Account of(AccountInput accountInput, User user, Account parent) {
        return Account.of(accountInput, user)
                .toBuilder()
                .parent(parent)
                .build();
    }
    public static Account of(AccountInput accountInput, User user) {
        return Account.builder()
                .color(accountInput.color())
                .accountType(accountInput.accountType())
                .hash(UUID.randomUUID().toString())
                .archived(false)
                .name(accountInput.name())
                .currency(accountInput.currency())
                .description(accountInput.description())
                .balance(accountInput.balance())
                .users(List.of(user))
                .build();
    }
}
