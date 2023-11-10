package com.budget.project.model.db;

import com.budget.project.model.dto.request.input.AccountInput;

import jakarta.persistence.*;

import lombok.*;

import java.util.*;

@Entity
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
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

    @OneToMany
    private Set<Account> subAccounts = new HashSet<>();

    @ManyToOne
    @ToString.Exclude
    private Account parent;

    @EqualsAndHashCode.Include
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
    private Set<User> users = new HashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "accountFrom", cascade = CascadeType.ALL)
    private Set<Transaction> transactions = new HashSet<>();

    public static Account of(AccountInput accountInput, User user, Account parent) {
        return Account.of(accountInput, user).toBuilder().parent(parent).build();
    }

    public static Account of(AccountInput accountInput, User user) {
        Account account = new Account();
        return account.toBuilder()
                .color(accountInput.color())
                .accountType(accountInput.accountType())
                .hash(UUID.randomUUID().toString())
                .archived(false)
                .name(accountInput.name())
                .currency(accountInput.currency())
                .description(accountInput.description())
                .balance(accountInput.balance())
                .users(Set.of(user))
                .build();
    }

    public void removeTransaction(Transaction transaction) {
        this.transactions.remove(transaction);
    }
}
