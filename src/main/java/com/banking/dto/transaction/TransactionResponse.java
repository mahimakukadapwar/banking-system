package com.banking.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private boolean success;
    private String message;
    private TransactionDTO transaction;
    private BigDecimal updatedBalance;
    private LocalDateTime timestamp;
}
