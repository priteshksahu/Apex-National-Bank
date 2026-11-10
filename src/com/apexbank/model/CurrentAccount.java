package com.apexbank.model;

import com.apexbank.exception.BankingException;

public class CurrentAccount extends Account {
    private static final long serialVersionUID = 1L;
    private final double overdraftLimit;

    public CurrentAccount(String accNum, String holderName, String email, String phone, String pin, double initialDeposit, double overdraftLimit)
            throws BankingException {
        super(accNum, holderName, email, phone, pin, initialDeposit);
        if (initialDeposit < 0 || overdraftLimit < 0) {
            throw new BankingException("Initial deposit and overdraft limit cannot be negative.");
        }
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public Transaction withdraw(double amount, String description) throws BankingException {
        if (amount <= 0) {
            throw new BankingException("Withdrawal amount must be greater than zero.");
        }
        if (this.balance - amount < -overdraftLimit) {
            throw new BankingException("Overdraft limit exceeded! Max available withdrawal: ₹" +
                    String.format("%.2f", (this.balance + overdraftLimit)));
        }
        this.balance -= amount;
        return addTransaction("WITHDRAWAL", amount, description);
    }

    public double getOverdraftLimit() {
        return overdraftLimit;
    }

    @Override
    public String getAccountType() {
        return "CURRENT";
    }

    @Override
    public double getOverdraftOrMinBalance() {
        return overdraftLimit;
    }
}
