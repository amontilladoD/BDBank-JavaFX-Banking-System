package com.bdbank.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Every money movement in the system produces one immutable Transaction record, automatically timestamped. */
public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String accountNumber;
    private final Enums.TxnType type;
    private final Enums.TxnCategory category;
    private final double amount;
    private final double charge;
    private final double balanceAfter;
    private final String description;
    private LocalDateTime timestamp;

    public Transaction(String id, String accountNumber, Enums.TxnType type, Enums.TxnCategory category,
                        double amount, double charge, double balanceAfter, String description) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.charge = charge;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public Enums.TxnType getType() { return type; }
    public Enums.TxnCategory getCategory() { return category; }
    public double getAmount() { return amount; }
    public double getCharge() { return charge; }
    public double getBalanceAfter() { return balanceAfter; }
    public String getDescription() { return description; }
    public LocalDateTime getTimestamp() { return timestamp; }

    /** Used only when rebuilding this object from a database row. */
    public void hydrateTimestamp(LocalDateTime original) { this.timestamp = original; }
}
