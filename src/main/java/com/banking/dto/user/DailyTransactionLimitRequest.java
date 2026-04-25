package com.banking.dto.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyTransactionLimitRequest {

    @NotNull(message = "Daily limit is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Daily limit should be greater than 0")
    private BigDecimal dailyLimit;
}