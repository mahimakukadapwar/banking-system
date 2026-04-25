package com.banking.repository;

import com.banking.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // All transactions of user
    @Query("""
        SELECT t FROM Transaction t 
        WHERE t.user.userId = :userId 
        ORDER BY t.transactionDate DESC
    """)
    List<Transaction> findByUserId(@Param("userId") Long userId);

    // Transactions in date range
    @Query("""
        SELECT t FROM Transaction t 
        WHERE t.user.userId = :userId 
        AND t.transactionDate BETWEEN :startDate AND :endDate 
        ORDER BY t.transactionDate DESC
    """)
    List<Transaction> findByUserIdAndDateRange(@Param("userId") Long userId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    // ✅ FIXED: Daily transaction sum (ONLY SUCCESS + ONLY WITHDRAW/TRANSFER)
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.user.userId = :userId
        AND t.status = 'SUCCESS'
        AND t.transactionType IN ('WITHDRAWAL', 'TRANSFER')
        AND t.transactionDate >= CURRENT_DATE
    """)
    BigDecimal findDailyTransactionSum(@Param("userId") Long userId);

    // ✅ FIXED: Today’s transactions (no DATE() hack)
    @Query("""
        SELECT t FROM Transaction t
        WHERE t.user.userId = :userId
        AND t.transactionDate >= CURRENT_DATE
        ORDER BY t.transactionDate DESC
    """)
    List<Transaction> findTodayTransactions(@Param("userId") Long userId);

    // Count daily transactions (also fixed)
    @Query("""
        SELECT COUNT(t)
        FROM Transaction t
        WHERE t.user.userId = :userId
        AND t.status = 'SUCCESS'
        AND t.transactionType IN ('WITHDRAWAL', 'TRANSFER')
        AND t.transactionDate >= CURRENT_DATE
    """)
    long countDailyTransactions(@Param("userId") Long userId);

    // Filter by type
    @Query("""
        SELECT t FROM Transaction t 
        WHERE t.user.userId = :userId 
        AND t.transactionType = :transactionType 
        ORDER BY t.transactionDate DESC
    """)
    List<Transaction> findByUserIdAndTransactionType(@Param("userId") Long userId,
                                                     @Param("transactionType") String transactionType);

    // All transactions including received ones
    @Query("""
        SELECT t FROM Transaction t 
        WHERE (t.user.userId = :userId 
        OR t.recipientAccountNumber = (
            SELECT u.accountNumber FROM User u WHERE u.userId = :userId
        ))
        ORDER BY t.transactionDate DESC
    """)
    List<Transaction> findAllTransactionsForUser(@Param("userId") Long userId);

    // Check ownership
    @Query("""
        SELECT COUNT(t) > 0 
        FROM Transaction t 
        WHERE t.transactionId = :transactionId 
        AND t.user.userId = :userId
    """)
    boolean existsByTransactionIdAndUserId(@Param("transactionId") Long transactionId,
                                           @Param("userId") Long userId);
}