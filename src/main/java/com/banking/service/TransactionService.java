package com.banking.service;

import com.banking.dto.transaction.DepositRequest;
import com.banking.dto.transaction.TransactionDTO;
import com.banking.dto.transaction.TransferRequest;
import com.banking.dto.transaction.WithdrawalRequest;
import com.banking.entity.Transaction;
import com.banking.entity.User;
import com.banking.exception.*;
import com.banking.repository.TransactionRepository;
import com.banking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    // DEPOSIT OPERATION
    public Transaction deposit(String email, DepositRequest depositRequest) {
        // Validate amount
        if (depositRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Deposit amount must be greater than zero");
        }

        // Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Check account status
        validateAccountStatus(user);

        // Create transaction
        BigDecimal previousBalance = user.getBalance();
        user.setBalance(user.getBalance().add(depositRequest.getAmount()));

        Transaction transaction = Transaction.builder()
                .user(user)
                .transactionType(Transaction.TransactionType.DEPOSIT)
                .amount(depositRequest.getAmount())
                .status(Transaction.TransactionStatus.SUCCESS)
                .description(depositRequest.getDescription() != null ? depositRequest.getDescription() : "Cash Deposit")
                .balanceAfterTransaction(user.getBalance())
                .build();

        userRepository.save(user);
        return transactionRepository.save(transaction);
    }

    // WITHDRAWAL OPERATION
    public Transaction withdraw(String email, WithdrawalRequest withdrawalRequest) {
        // Validate amount
        if (withdrawalRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be greater than zero");
        }

        // Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Check account status
        validateAccountStatus(user);

        // Check if account is banned
        if (user.getAccountStatus() == User.AccountStatus.BANNED) {
            throw new AccountBannedException("Your account is banned and cannot perform transactions");
        }

        // Check sufficient balance
        if (user.getBalance().compareTo(withdrawalRequest.getAmount()) < 0) {
            Transaction failedTransaction = createFailedTransaction(
                    user,
                    Transaction.TransactionType.WITHDRAWAL,
                    withdrawalRequest.getAmount(),
                    "Insufficient balance",
                    withdrawalRequest.getDescription()
            );
            transactionRepository.save(failedTransaction);
            throw new InsufficientBalanceException("Insufficient balance. Available balance: " + user.getBalance());
        }

        // Check daily transaction limit
        BigDecimal dailyTransactionSum = transactionRepository.findDailyTransactionSum(user.getUserId());
        if (dailyTransactionSum.add(withdrawalRequest.getAmount()).compareTo(user.getDailyTransactionLimit()) > 0) {
            Transaction failedTransaction = createFailedTransaction(
                    user,
                    Transaction.TransactionType.WITHDRAWAL,
                    withdrawalRequest.getAmount(),
                    "Daily transaction limit exceeded",
                    withdrawalRequest.getDescription()
            );
            transactionRepository.save(failedTransaction);
            throw new DailyLimitExceededException("Daily transaction limit exceeded. Remaining limit: " +
                    user.getDailyTransactionLimit().subtract(dailyTransactionSum));
        }

        // Perform withdrawal
        BigDecimal previousBalance = user.getBalance();
        user.setBalance(user.getBalance().subtract(withdrawalRequest.getAmount()));

        Transaction transaction = Transaction.builder()
                .user(user)
                .transactionType(Transaction.TransactionType.WITHDRAWAL)
                .amount(withdrawalRequest.getAmount())
                .status(Transaction.TransactionStatus.SUCCESS)
                .description(withdrawalRequest.getDescription() != null ? withdrawalRequest.getDescription() : "Cash Withdrawal")
                .balanceAfterTransaction(user.getBalance())
                .build();

        userRepository.save(user);
        return transactionRepository.save(transaction);
    }

    // TRANSFER OPERATION
    public Transaction transfer(String senderEmail, TransferRequest transferRequest) {
        // Validate amount
        if (transferRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Transfer amount must be greater than zero");
        }

        // Find sender
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new UserNotFoundException("Sender not found"));

        // Check sender account status
        validateAccountStatus(sender);

        if (sender.getAccountStatus() == User.AccountStatus.BANNED) {
            throw new AccountBannedException("Your account is banned and cannot perform transactions");
        }

        // Find recipient
        User recipient = userRepository.findByAccountNumber(transferRequest.getRecipientAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Recipient account not found"));

        // Check if sender is trying to transfer to themselves
        if (sender.getUserId().equals(recipient.getUserId())) {
            throw new BankingException("Cannot transfer money to your own account");
        }

        // Check recipient account status
        if (!recipient.getIsActive() || recipient.getAccountStatus() == User.AccountStatus.BANNED) {
            throw new AccountBannedException("Recipient account is not available");
        }

        // Check sufficient balance
        if (sender.getBalance().compareTo(transferRequest.getAmount()) < 0) {
            Transaction failedTransaction = createFailedTransaction(
                    sender,
                    Transaction.TransactionType.TRANSFER,
                    transferRequest.getAmount(),
                    "Insufficient balance",
                    transferRequest.getDescription()
            );
            transactionRepository.save(failedTransaction);
            throw new InsufficientBalanceException("Insufficient balance. Available balance: " + sender.getBalance());
        }

        // Check daily transaction limit
        BigDecimal dailyTransactionSum = transactionRepository.findDailyTransactionSum(sender.getUserId());
        if (dailyTransactionSum.add(transferRequest.getAmount()).compareTo(sender.getDailyTransactionLimit()) > 0) {
            Transaction failedTransaction = createFailedTransaction(
                    sender,
                    Transaction.TransactionType.TRANSFER,
                    transferRequest.getAmount(),
                    "Daily transaction limit exceeded",
                    transferRequest.getDescription()
            );
            transactionRepository.save(failedTransaction);
            throw new DailyLimitExceededException("Daily transaction limit exceeded. Remaining limit: " +
                    sender.getDailyTransactionLimit().subtract(dailyTransactionSum));
        }

        // Perform transfer
        sender.setBalance(sender.getBalance().subtract(transferRequest.getAmount()));
        recipient.setBalance(recipient.getBalance().add(transferRequest.getAmount()));

        Transaction transaction = Transaction.builder()
                .user(sender)
                .transactionType(Transaction.TransactionType.TRANSFER)
                .amount(transferRequest.getAmount())
                .status(Transaction.TransactionStatus.SUCCESS)
                .recipientAccountNumber(transferRequest.getRecipientAccountNumber())
                .recipientName(recipient.getFullName())
                .description(transferRequest.getDescription() != null ? transferRequest.getDescription() : "Fund Transfer")
                .balanceAfterTransaction(sender.getBalance())
                .build();

        userRepository.save(sender);
        userRepository.save(recipient);
        return transactionRepository.save(transaction);
    }

    // GET TRANSACTION HISTORY
    public List<TransactionDTO> getTransactionHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Transaction> transactions = transactionRepository.findByUserId(user.getUserId());
        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // GET TRANSACTION HISTORY BY DATE RANGE
    public List<TransactionDTO> getTransactionHistoryByDateRange(String email, LocalDateTime startDate, LocalDateTime endDate) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Transaction> transactions = transactionRepository.findByUserIdAndDateRange(user.getUserId(), startDate, endDate);
        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // GET TODAY'S TRANSACTIONS
    public List<TransactionDTO> getTodayTransactions(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Transaction> transactions = transactionRepository.findTodayTransactions(user.getUserId());
        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // PRIVATE HELPER METHODS
    private void validateAccountStatus(User user) {
        if (!user.getIsActive()) {
            throw new BankingException("Your account is inactive");
        }
        if (user.getAccountStatus() == User.AccountStatus.BANNED) {
            throw new AccountBannedException("Your account has been banned");
        }
        if (user.getAccountStatus() == User.AccountStatus.SUSPENDED) {
            throw new AccountSuspendedException("Your account has been suspended");
        }
    }

    private Transaction createFailedTransaction(User user, Transaction.TransactionType type,
                                                BigDecimal amount, String failureReason, String description) {
        return Transaction.builder()
                .user(user)
                .transactionType(type)
                .amount(amount)
                .status(Transaction.TransactionStatus.FAILED)
                .description(description)
                .failureReason(failureReason)
                .balanceAfterTransaction(user.getBalance())
                .build();
    }

    private TransactionDTO convertToDTO(Transaction transaction) {
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
