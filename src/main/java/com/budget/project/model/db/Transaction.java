package com.budget.project.model.db;

import com.budget.project.model.dto.request.input.TransactionInput;
import com.budget.project.utils.DateUtils;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Transaction {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String note;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDateTime date;

    private Boolean need;

    @EqualsAndHashCode.Include
    @Column(nullable = false, unique = true)
    private String hash;

    @ManyToOne
    @JoinColumn(name = "account_to_id")
    private Account accountTo;

    @ManyToOne
    @JoinColumn(name = "account_from_id")
    private Account accountFrom;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @ManyToOne(cascade = CascadeType.DETACH)
    private Category category;

    @ManyToOne(cascade = CascadeType.DETACH)
    private SubCategory subCategory;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    private Boolean future = false;

    public static Transaction of(
            TransactionInput transactionInput,
            Account accountFrom,
            Account accountTo,
            Category category,
            SubCategory subCategory) {
        return Transaction.builder()
                .name(transactionInput.name())
                .note(transactionInput.note())
                .amount(transactionInput.amount())
                .date(DateUtils.parse(transactionInput.date()))
                .need(transactionInput.need())
                .hash(UUID.randomUUID().toString())
                .accountTo(accountTo)
                .accountFrom(accountFrom)
                .category(category)
                .future(false)
                .subCategory(subCategory)
                .currency(transactionInput.currency())
                .transactionType(transactionInput.transactionType())
                .build();
    }
}
