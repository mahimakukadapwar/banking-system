package com.banking.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {

    private Long transactionId;
    private String transactionType;
    private BigDecimal amount;
    private String status;
    private String recipientAccountNumber;
    private String recipientName;
    private LocalDateTime transactionDate;
    private String description;
    private BigDecimal balanceAfterTransaction;
}
