-- =====================================================
-- Banking System Database Schema
-- =====================================================

-- Create Database
CREATE DATABASE IF NOT EXISTS banking_db;
USE banking_db;

-- =====================================================
-- Users Table
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    account_number VARCHAR(16) NOT NULL UNIQUE,
    balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    phone_number VARCHAR(10) NOT NULL,
    address VARCHAR(500) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    pin_code VARCHAR(6) NOT NULL,
    account_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    is_active BOOLEAN NOT NULL DEFAULT true,
    daily_transaction_limit DECIMAL(19,2) NOT NULL DEFAULT 100000.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP NULL,

    -- Indexes
    INDEX idx_email (email),
    INDEX idx_account_number (account_number),
    INDEX idx_account_status (account_status),
    INDEX idx_is_active (is_active),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Transactions Table
-- =====================================================
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'SUCCESS',
    recipient_account_number VARCHAR(16) NULL,
    recipient_name VARCHAR(255) NULL,
    description VARCHAR(500) NULL,
    failure_reason VARCHAR(500) NULL,
    balance_after_transaction DECIMAL(19,2) NOT NULL,
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Key
    CONSTRAINT fk_transaction_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,

    -- Indexes
    INDEX idx_user_id (user_id),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_status (status),
    INDEX idx_transaction_date (transaction_date),
    INDEX idx_recipient_account (recipient_account_number),
    INDEX idx_user_date (user_id, transaction_date)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Views for Reporting
-- =====================================================

-- View: Daily Transaction Summary
CREATE OR REPLACE VIEW vw_daily_transaction_summary AS
SELECT
    DATE(transaction_date) as transaction_date,
    user_id,
    transaction_type,
    COUNT(*) as transaction_count,
    SUM(amount) as total_amount,
    status
FROM transactions
GROUP BY DATE(transaction_date), user_id, transaction_type, status;

-- View: User Account Summary
CREATE OR REPLACE VIEW vw_user_account_summary AS
SELECT
    u.user_id,
    u.email,
    u.full_name,
    u.account_number,
    u.balance,
    u.account_status,
    u.daily_transaction_limit,
    COUNT(DISTINCT CASE WHEN DATE(t.transaction_date) = CURDATE() THEN t.transaction_id END) as today_transactions,
    COALESCE(SUM(CASE WHEN DATE(t.transaction_date) = CURDATE() AND t.transaction_type IN ('WITHDRAWAL', 'TRANSFER') THEN t.amount ELSE 0 END), 0) as today_total_spent,
    u.created_at,
    u.last_login_at
FROM users u
LEFT JOIN transactions t ON u.user_id = t.user_id AND t.status = 'SUCCESS'
GROUP BY u.user_id;

-- =====================================================
-- Stored Procedures
-- =====================================================

-- Procedure: Get User Account Balance
DELIMITER //
CREATE PROCEDURE sp_get_user_balance(IN p_user_id BIGINT)
BEGIN
    SELECT
        user_id,
        email,
        full_name,
        account_number,
        balance,
        daily_transaction_limit,
        account_status
    FROM users
    WHERE user_id = p_user_id;
END //
DELIMITER ;

-- Procedure: Get Daily Transaction Total
DELIMITER //
CREATE PROCEDURE sp_get_daily_transaction_total(IN p_user_id BIGINT)
BEGIN
    SELECT
        COALESCE(SUM(amount), 0) as daily_total
    FROM transactions
    WHERE user_id = p_user_id
    AND transaction_type IN ('WITHDRAWAL', 'TRANSFER')
    AND DATE(transaction_date) = CURDATE()
    AND status = 'SUCCESS';
END //
DELIMITER ;

-- Procedure: Get User Transaction History
DELIMITER //
CREATE PROCEDURE sp_get_user_transactions(IN p_user_id BIGINT, IN p_limit INT)
BEGIN
    SELECT
        transaction_id,
        transaction_type,
        amount,
        status,
        recipient_account_number,
        recipient_name,
        description,
        balance_after_transaction,
        transaction_date
    FROM transactions
    WHERE user_id = p_user_id
    ORDER BY transaction_date DESC
    LIMIT p_limit;
END //
DELIMITER ;

-- =====================================================
-- Sample Data (Optional - for testing)
-- =====================================================

-- Uncomment below to insert sample data

-- INSERT INTO users (email, full_name, password, account_number, balance, phone_number, address, city, state, pin_code, account_status, is_active)
-- VALUES
-- ('john.doe@example.com', 'John Doe', '$2a$10$...', 'ACC0000000000001', 100000.00, '9876543210', '123 Main St', 'Pune', 'Maharashtra', '411001', 'ACTIVE', true),
-- ('jane.smith@example.com', 'Jane Smith', '$2a$10$...', 'ACC0000000000002', 50000.00, '9876543211', '456 Oak Ave', 'Mumbai', 'Maharashtra', '400001', 'ACTIVE', true);

-- =====================================================
-- Data Integrity Checks
-- =====================================================

-- Check for orphaned transactions
-- SELECT t.transaction_id FROM transactions t LEFT JOIN users u ON t.user_id = u.user_id WHERE u.user_id IS NULL;

-- Check for duplicate email
-- SELECT email, COUNT(*) FROM users GROUP BY email HAVING COUNT(*) > 1;

-- =====================================================
-- Performance Optimization Notes
-- =====================================================

-- 1. Indexes are created on frequently queried columns:
--    - email (used in login)
--    - account_number (used in transfers)
--    - user_id (used in transaction queries)
--    - transaction_date (used in date range queries)
--    - account_status (used in account validation)

-- 2. Foreign key constraint ensures referential integrity

-- 3. CASCADE delete on user deletion will remove all related transactions

-- 4. Charset set to UTF-8 for international character support

-- 5. Using BIGINT for IDs to support large datasets

-- =====================================================
-- Backup & Recovery
-- =====================================================

-- Backup Database:
-- mysqldump -u root -p banking_db > banking_db_backup.sql

-- Restore Database:
-- mysql -u root -p banking_db < banking_db_backup.sql

-- =====================================================
-- End of Database Schema
-- =====================================================
