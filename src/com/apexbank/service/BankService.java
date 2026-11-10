package com.apexbank.service;

import com.apexbank.database.DatabaseRepository;
import com.apexbank.exception.BankingException;
import com.apexbank.model.*;
import java.util.*;

public class BankService {
    private final DatabaseRepository repository;
    private final Map<String, Account> accounts;
    private long nextAccSequence = 100101L;

    public BankService() {
        this.repository = new DatabaseRepository();
        this.accounts = repository.loadAllAccounts();
        updateSequence();
        seedDemoAccountsIfEmpty();
    }

    private void updateSequence() {
        for (String accNum : accounts.keySet()) {
            if (accNum.startsWith("ACC")) {
                try {
                    long num = Long.parseLong(accNum.substring(3));
                    if (num >= nextAccSequence) {
                        nextAccSequence = num + 1;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    private void seedDemoAccountsIfEmpty() {
        if (accounts.isEmpty()) {
            try {
                createSavingsAccount("Aarav Sharma", "aarav.sharma@apexbank.com", "9876543210", "1234", 12500.0);
                createCurrentAccount("Apex Tech Solutions Pvt Ltd", "finance@apextech.in", "9123456789", "1234", 45000.0, 20000.0);
                createSavingsAccount("Priya Verma", "priya.verma@apexbank.com", "9988776655", "1234", 8200.0);
            } catch (BankingException ignored) {}
        }
    }

    private String generateAccountNumber() {
        return "ACC" + (nextAccSequence++);
    }

    private void validateFields(String name, String email, String phone, String pin) throws BankingException {
        if (name == null || name.trim().isEmpty()) {
            throw new BankingException("Account holder name cannot be empty.");
        }
        if (email == null || !email.contains("@")) {
            throw new BankingException("Please enter a valid email address.");
        }
        if (phone == null || !phone.matches("\\d{10}")) {
            throw new BankingException("Phone number must be 10 digits.");
        }
        if (pin == null || !pin.matches("\\d{4}")) {
            throw new BankingException("Security PIN must be exactly 4 numeric digits.");
        }
    }

    public Account createSavingsAccount(String name, String email, String phone, String pin, double initialDeposit)
            throws BankingException {
        validateFields(name, email, phone, pin);
        String accNum = generateAccountNumber();
        SavingsAccount acc = new SavingsAccount(accNum, name, email, phone, pin, initialDeposit);
        accounts.put(accNum, acc);
        repository.saveAll(accounts);
        return acc;
    }

    public Account createCurrentAccount(String name, String email, String phone, String pin, double initialDeposit, double overdraft)
            throws BankingException {
        validateFields(name, email, phone, pin);
        String accNum = generateAccountNumber();
        CurrentAccount acc = new CurrentAccount(accNum, name, email, phone, pin, initialDeposit, overdraft);
        accounts.put(accNum, acc);
        repository.saveAll(accounts);
        return acc;
    }

    public Account authenticate(String accNum, String pin) throws BankingException {
        if (accNum == null || accNum.trim().isEmpty()) {
            throw new BankingException("Please enter your Account Number.");
        }
        Account acc = accounts.get(accNum.trim().toUpperCase());
        if (acc == null) {
            throw new BankingException("Account '" + accNum + "' does not exist.");
        }
        if (!acc.validatePin(pin)) {
            throw new BankingException("Invalid 4-digit PIN entered.");
        }
        return acc;
    }

    public void deposit(Account account, double amount, String note) throws BankingException {
        account.deposit(amount, note == null || note.isEmpty() ? "Cash Deposit" : note);
        repository.saveAll(accounts);
    }

    public void withdraw(Account account, double amount, String note) throws BankingException {
        account.withdraw(amount, note == null || note.isEmpty() ? "Cash Withdrawal" : note);
        repository.saveAll(accounts);
    }

    public void transfer(Account sender, String recipientAccNum, double amount, String remarks) throws BankingException {
        if (recipientAccNum == null || recipientAccNum.trim().isEmpty()) {
            throw new BankingException("Please specify a recipient Account Number.");
        }
        String targetKey = recipientAccNum.trim().toUpperCase();
        if (sender.getAccountNumber().equalsIgnoreCase(targetKey)) {
            throw new BankingException("Cannot transfer funds to the same account.");
        }
        Account receiver = accounts.get(targetKey);
        if (receiver == null) {
            throw new BankingException("Recipient Account '" + targetKey + "' not found.");
        }
        String cleanNote = (remarks == null || remarks.trim().isEmpty()) ? "IMPS Transfer" : remarks.trim();
        sender.withdraw(amount, "Transfer to " + receiver.getAccountNumber() + " (" + receiver.getHolderName() + ") - " + cleanNote);
        receiver.deposit(amount, "Transfer from " + sender.getAccountNumber() + " (" + sender.getHolderName() + ") - " + cleanNote);
        repository.saveAll(accounts);
    }

    public double applyInterest(Account account) throws BankingException {
        if (account instanceof SavingsAccount) {
            SavingsAccount sa = (SavingsAccount) account;
            Transaction t = sa.applyInterest();
            repository.saveAll(accounts);
            return t.getAmount();
        }
        throw new BankingException("Annual interest is only applicable to Savings Accounts.");
    }

    public int applyAnnualInterestToAllSavings() {
        int count = 0;
        for (Account a : accounts.values()) {
            if (a instanceof SavingsAccount) {
                ((SavingsAccount) a).applyInterest();
                count++;
            }
        }
        repository.saveAll(accounts);
        return count;
    }

    public void updateProfile(Account account, String name, String email, String phone) throws BankingException {
        validateFields(name, email, phone, account.getPin());
        account.setHolderName(name);
        account.setEmail(email);
        account.setPhone(phone);
        repository.saveAll(accounts);
    }

    public void changePin(Account account, String oldPin, String newPin) throws BankingException {
        if (!account.validatePin(oldPin)) {
            throw new BankingException("Current PIN is incorrect.");
        }
        account.changePin(newPin);
        repository.saveAll(accounts);
    }

    public void deleteAccount(String accNum, String pin) throws BankingException {
        Account acc = authenticate(accNum, pin);
        accounts.remove(acc.getAccountNumber());
        repository.saveAll(accounts);
    }

    public Collection<Account> getAllAccounts() {
        return accounts.values();
    }

    public double getTotalBankDeposits() {
        double sum = 0;
        for (Account a : accounts.values()) {
            if (a.getBalance() > 0) sum += a.getBalance();
        }
        return sum;
    }

    public String getDatabaseStatus() {
        return repository.getStorageStatus();
    }
}
