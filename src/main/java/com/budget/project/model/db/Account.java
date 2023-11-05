package com.budget.project.model.db;

import com.budget.project.model.dto.request.input.AccountInput;

import jakarta.persistence.*;

import lombok.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

    @OneToMany
    private List<Account> subAccounts = new ArrayList<>();

    @ManyToOne
    @ToString.Exclude
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
    @ToString.Exclude
    @OneToMany(mappedBy = "accountFrom", cascade = CascadeType.ALL)
    private List<Transaction> transactions = new ArrayList<>();

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
                .users(List.of(user))
                .build();
    }

    public void removeTransaction(Transaction transaction){
        this.transactions.remove(transaction);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Account account = (Account) object;
        return Objects.equals(hash, account.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }
}
