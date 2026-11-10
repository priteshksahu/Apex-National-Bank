package com.apexbank.model;

import com.apexbank.exception.BankingException;

public class SavingsAccount extends Account {
    private static final long serialVersionUID = 1L;
    public static final double MIN_BALANCE = 500.0;
    public static final double INTEREST_RATE = 4.5;

    public SavingsAccount(String accNum, String holderName, String email, String phone, String pin, double initialDeposit)
            throws BankingException {
        super(accNum, holderName, email, phone, pin, initialDeposit);
        if (initialDeposit < MIN_BALANCE) {
            throw new BankingException("Savings Account requires a minimum initial deposit of ₹" + MIN_BALANCE);
        }
    }

    @Override
    public Transaction withdraw(double amount, String description) throws BankingException {
        if (amount <= 0) {
            throw new BankingException("Withdrawal amount must be greater than zero.");
        }
        if (this.balance - amount < MIN_BALANCE) {
            throw new BankingException("Insufficient funds! Minimum balance of ₹" + MIN_BALANCE + " must be maintained.");
        }
        this.balance -= amount;
        return addTransaction("WITHDRAWAL", amount, description);
    }

    public Transaction applyInterest() {
        double interest = (this.balance * INTEREST_RATE) / 100.0;
        this.balance += interest;
        return addTransaction("INTEREST", interest, "Annual Interest Credited @ " + INTEREST_RATE + "%");
    }

    @Override
    public String getAccountType() {
        return "SAVINGS";
    }

    @Override
    public double getOverdraftOrMinBalance() {
        return MIN_BALANCE;
    }
}
