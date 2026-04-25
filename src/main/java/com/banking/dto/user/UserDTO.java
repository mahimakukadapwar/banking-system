package com.banking.dto.user;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long userId;
    private String email;
    private String fullName;
    private String accountNumber;
    private BigDecimal balance;
    private String phoneNumber;
    private String address;
    private String city;
    private String state;
    private String pinCode;
    private String accountStatus;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private BigDecimal dailyTransactionLimit;
}
