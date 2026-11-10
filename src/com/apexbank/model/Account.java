package com.apexbank.model;

import com.apexbank.exception.BankingException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Account implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String accountNumber;
    private String holderName;
    private String email;
    private String phone;
    private String pin;
    protected double balance;
    private final List<Transaction> transactions = new ArrayList<>();

    public Account(String accountNumber, String holderName, String email, String phone, String pin, double initialDeposit) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.email = email;
        this.phone = phone;
        this.pin = pin;
        this.balance = initialDeposit;
        addTransaction("OPENING", initialDeposit, "Initial account opening deposit");
    }

    public Transaction addTransaction(String type, double amount, String description) {
        Transaction txn = new Transaction(this.accountNumber, type, amount, this.balance, description);
        transactions.add(txn);
        return txn;
    }

    public void loadTransaction(Transaction txn) {
        transactions.add(txn);
    }

    public String getAccountNumber() { return accountNumber; }
    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPin() { return pin; }
    public double getBalance() { return balance; }
    public List<Transaction> getTransactions() { return transactions; }

    public boolean validatePin(String inputPin) {
        return this.pin != null && this.pin.equals(inputPin);
    }

    public void changePin(String newPin) throws BankingException {
        if (newPin == null || !newPin.matches("\\d{4}")) {
            throw new BankingException("PIN must be exactly 4 numeric digits.");
        }
        this.pin = newPin;
    }

    public Transaction deposit(double amount, String description) throws BankingException {
        if (amount <= 0) {
            throw new BankingException("Deposit amount must be greater than zero.");
        }
        this.balance += amount;
        return addTransaction("DEPOSIT", amount, description);
    }

    public abstract Transaction withdraw(double amount, String description) throws BankingException;
    public abstract String getAccountType();
    public abstract double getOverdraftOrMinBalance();
}
