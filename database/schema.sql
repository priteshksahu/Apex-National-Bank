-- =========================================================================
-- APEX NATIONAL BANK - COMPLETE SQL DATABASE SCHEMA & INITIAL SEED DATA
-- Compatible with MySQL, PostgreSQL, MariaDB, and SQLite
-- =========================================================================

CREATE DATABASE IF NOT EXISTS apex_bank_db;
USE apex_bank_db;

-- 1. Accounts Table
CREATE TABLE IF NOT EXISTS accounts (
    account_number VARCHAR(20) PRIMARY KEY,
    holder_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    pin VARCHAR(10) NOT NULL,
    account_type VARCHAR(20) NOT NULL, -- 'SAVINGS' or 'CURRENT'
    balance DOUBLE NOT NULL DEFAULT 0.0,
    overdraft_limit DOUBLE NOT NULL DEFAULT 0.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Transactions Ledger Table
CREATE TABLE IF NOT EXISTS transactions (
    txn_id VARCHAR(30) PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL,
    timestamp VARCHAR(30) NOT NULL,
    txn_type VARCHAR(20) NOT NULL, -- 'OPENING', 'DEPOSIT', 'WITHDRAWAL', 'INTEREST'
    amount DOUBLE NOT NULL,
    post_balance DOUBLE NOT NULL,
    description VARCHAR(255),
    FOREIGN KEY (account_number) REFERENCES accounts(account_number) ON DELETE CASCADE
);

-- 3. Pre-Seeded Demo Accounts
INSERT INTO accounts (account_number, holder_name, email, phone, pin, account_type, balance, overdraft_limit) VALUES
('ACC100101', 'Aarav Sharma', 'aarav.sharma@apexbank.com', '9876543210', '1234', 'SAVINGS', 12500.00, 0.00),
('ACC100102', 'Apex Tech Solutions Pvt Ltd', 'finance@apextech.in', '9123456789', '1234', 'CURRENT', 45000.00, 20000.00),
('ACC100103', 'Priya Verma', 'priya.verma@apexbank.com', '9988776655', '1234', 'SAVINGS', 8200.00, 0.00);

-- 4. Pre-Seeded Transactions
INSERT INTO transactions (txn_id, account_number, timestamp, txn_type, amount, post_balance, description) VALUES
('TXN100001', 'ACC100101', '2026-11-01 09:30:00', 'OPENING', 12500.00, 12500.00, 'Initial account opening deposit'),
('TXN100002', 'ACC100102', '2026-11-01 10:15:00', 'OPENING', 45000.00, 45000.00, 'Initial account opening deposit'),
('TXN100003', 'ACC100103', '2026-11-01 11:00:00', 'OPENING', 8200.00, 8200.00, 'Initial account opening deposit');
