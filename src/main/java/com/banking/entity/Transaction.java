package com.banking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private String recipientAccountNumber;

    private String recipientName;

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    private String description;

    @Column(length = 500)
    private String failureReason;

    @Column(nullable = false)
    private BigDecimal balanceAfterTransaction;

    @PrePersist
    protected void onCreate() {
        transactionDate = LocalDateTime.now();
        status = TransactionStatus.SUCCESS;
    }

    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, TRANSFER, INTEREST
    }

    public enum TransactionStatus {
        SUCCESS, FAILED, PENDING
    }
}
