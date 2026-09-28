package com.bdbank.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

public class Account implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String accountNumber;
    private String accountHolderName;
    private String nid;
    private String phone;
    private String email;
    private String passwordHash;
    private Enums.AccountType accountType;
    private final AtomicReference<Double> balance;
    private Enums.AccountStatus status;
    private LocalDateTime createdAt;

    public Account(String accountNumber, String accountHolderName, String nid, String phone, String email,
                    String passwordHash, Enums.AccountType accountType, double openingBalance, Enums.AccountStatus status) {
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.nid = nid;
        this.phone = phone;
        this.email = email;
        this.passwordHash = passwordHash;
        this.accountType = accountType;
        this.balance = new AtomicReference<>(openingBalance);
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public void credit(double amount) {
        balance.updateAndGet(b -> round2(b + amount));
    }

    public synchronized void debit(double amount) throws com.bdbank.exception.BankException {
        double current = balance.get();
        if (current < amount) {
            throw new com.bdbank.exception.BankException("Insufficient balance. Available: " + current + " Tk, Requested: " + amount + " Tk");
        }
        balance.set(round2(current - amount));
    }

    private double round2(double v) { return Math.round(v * 100.0) / 100.0; }

    public String getAccountNumber() { return accountNumber; }
    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String v) { this.accountHolderName = v; }
    public String getNid() { return nid; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { this.phone = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String v) { this.passwordHash = v; }
    public Enums.AccountType getAccountType() { return accountType; }
    public double getBalance() { return balance.get(); }
    public Enums.AccountStatus getStatus() { return status; }
    public void setStatus(Enums.AccountStatus s) { this.status = s; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void hydrateCreatedAt(LocalDateTime original) { this.createdAt = original; }

    @Override
    public String toString() {
        return accountNumber + " - " + accountHolderName + " (" + accountType.label + ")";
    }
}
