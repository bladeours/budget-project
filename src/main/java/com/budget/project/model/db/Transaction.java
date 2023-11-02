package com.budget.project.model.db;

import com.budget.project.model.dto.request.TransactionInput;
import com.budget.project.utils.DateUtils;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id @GeneratedValue private Long id;

    private String name;

    private String note;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDateTime date;

    private Boolean need;

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

    @ManyToOne private Category category;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    public static Transaction of(
            TransactionInput transactionInput,
            Account accountFrom,
            Account accountTo,
            Category category) {
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
                .currency(transactionInput.currency())
                .transactionType(transactionInput.transactionType())
                .build();
    }
}
