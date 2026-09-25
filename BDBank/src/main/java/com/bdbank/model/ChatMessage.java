package com.bdbank.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String accountNumber; // conversation key
    private final String sender; // "USER" or "ADMIN"
    private final String text;
    private LocalDateTime timestamp;

    public ChatMessage(String accountNumber, String sender, String text) {
        this.accountNumber = accountNumber;
        this.sender = sender;
        this.text = text;
        this.timestamp = LocalDateTime.now();
    }

    public String getAccountNumber() { return accountNumber; }
    public String getSender() { return sender; }
    public String getText() { return text; }
    public LocalDateTime getTimestamp() { return timestamp; }

    /** Used only when rebuilding this object from a database row. */
    public void hydrateTimestamp(LocalDateTime original) { this.timestamp = original; }
}
