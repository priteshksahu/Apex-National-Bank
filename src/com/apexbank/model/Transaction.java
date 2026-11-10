package com.apexbank.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id;
    private final String accountNumber;
    private final String timestamp;
    private final String type;
    private final double amount;
    private final double postBalance;
    private final String description;

    public Transaction(String accountNumber, String type, double amount, double postBalance, String description) {
        this.id = "TXN" + (System.currentTimeMillis() % 1000000) + (int)(Math.random() * 90 + 10);
        this.accountNumber = accountNumber;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.type = type;
        this.amount = amount;
        this.postBalance = postBalance;
        this.description = description;
    }

    public Transaction(String id, String accountNumber, String timestamp, String type, double amount, double postBalance, String description) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.timestamp = timestamp;
        this.type = type;
        this.amount = amount;
        this.postBalance = postBalance;
        this.description = description;
    }

    public String getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public String getTimestamp() { return timestamp; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public double getPostBalance() { return postBalance; }
    public String getDescription() { return description; }
}
