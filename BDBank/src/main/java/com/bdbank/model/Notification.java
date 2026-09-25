package com.bdbank.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    /** "ALL" is a broadcast notification (e.g. dollar-rate change), otherwise a specific account number. */
    private final String targetAccountNumber;
    private final String message;
    private LocalDateTime timestamp;
    private volatile boolean read;

    public Notification(String id, String targetAccountNumber, String message) {
        this.id = id;
        this.targetAccountNumber = targetAccountNumber;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.read = false;
    }

    public String getId() { return id; }
    public String getTargetAccountNumber() { return targetAccountNumber; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }

    /** Used only when rebuilding this object from a database row. */
    public void hydrateTimestamp(LocalDateTime original) { this.timestamp = original; }
    public boolean isRead() { return read; }
    public void setRead(boolean r) { this.read = r; }
}
