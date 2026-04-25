package com.banking.dto.common;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {

    private BigDecimal balance;
    private BigDecimal dailyTransactionLimit;
    private LocalDateTime timestamp;
}
