package com.banking.controller;

import com.banking.dto.common.ApiResponse;
import com.banking.dto.transaction.*;

import com.banking.entity.Transaction;
import com.banking.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionDTO>> deposit(
            Authentication authentication,
            @Valid @RequestBody DepositRequest request) {

        String email = authentication.getName();
        Transaction transaction = transactionService.deposit(email, request);

        TransactionDTO dto = map(transaction);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        true,
                        "Deposit successful",
                        dto,
                        LocalDateTime.now()
                ));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionDTO>> withdraw(
            Authentication authentication,
            @Valid @RequestBody WithdrawalRequest request) {

        String email = authentication.getName();
        Transaction transaction = transactionService.withdraw(email, request);

        TransactionDTO dto = map(transaction);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        true,
                        "Withdrawal successful",
                        dto,
                        LocalDateTime.now()
                ));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionDTO>> transfer(
            Authentication authentication,
            @Valid @RequestBody TransferRequest request) {

        String email = authentication.getName();
        Transaction transaction = transactionService.transfer(email, request);

        TransactionDTO dto = map(transaction);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        true,
                        "Transfer successful",
                        dto,
                        LocalDateTime.now()
                ));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> history(Authentication authentication) {

        String email = authentication.getName();
        List<TransactionDTO> list = transactionService.getTransactionHistory(email);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Transaction history fetched",
                        list,
                        LocalDateTime.now()
                )
        );
    }

    @GetMapping("/history/today")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> today(Authentication authentication) {

        String email = authentication.getName();
        List<TransactionDTO> list = transactionService.getTodayTransactions(email);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Today's transactions fetched",
                        list,
                        LocalDateTime.now()
                )
        );
    }

    @GetMapping("/history/range")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> range(
            Authentication authentication,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        String email = authentication.getName();

        List<TransactionDTO> list =
                transactionService.getTransactionHistoryByDateRange(
                        email,
                        LocalDateTime.parse(startDate),
                        LocalDateTime.parse(endDate)
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Filtered transactions fetched",
                        list,
                        LocalDateTime.now()
                )
        );
    }

    // 🔥 centralized mapping (important for clean code)
    private TransactionDTO map(Transaction transaction) {
        return new TransactionDTO(
                transaction.getTransactionId(),
                transaction.getTransactionType().toString(),
                transaction.getAmount(),
                transaction.getStatus().toString(),
                transaction.getRecipientAccountNumber(),
                transaction.getRecipientName(),
                transaction.getTransactionDate(),
                transaction.getDescription(),
                transaction.getBalanceAfterTransaction()
        );
    }
}