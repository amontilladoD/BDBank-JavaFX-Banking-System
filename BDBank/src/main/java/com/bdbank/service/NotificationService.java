package com.bdbank.service;

import com.bdbank.model.Notification;
import com.bdbank.util.Util;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Notifications are produced by many different threads at once (a transfer just completed on a
 * background Task, the dollar-rate ticker just fired on the scheduler, an admin just approved a
 * loan). This service is the single, thread-safe funnel: any thread can call push(), and the
 * JavaFX ObservableList (only ever mutated on the FX thread via Platform.runLater) is what the
 * bell-icon UI binds to, so the badge count updates live without the UI thread ever blocking.
 * Backed by SQLite's `notifications` table.
 */
public class NotificationService {
    private static final NotificationService INSTANCE = new NotificationService();
    public static NotificationService get() { return INSTANCE; }

    private final List<Notification> store;
    private final ObservableList<Notification> live = FXCollections.observableArrayList();

    private NotificationService() {
        store = new CopyOnWriteArrayList<>(
                Db.get().query("SELECT * FROM notifications ORDER BY timestamp DESC", this::mapNotification));
        Platform.runLater(() -> live.setAll(store));
    }

    private Notification mapNotification(ResultSet rs) throws SQLException {
        Notification n = new Notification(rs.getString("id"), rs.getString("target_account"), rs.getString("message"));
        n.hydrateTimestamp(LocalDateTime.parse(rs.getString("timestamp")));
        n.setRead(rs.getInt("is_read") == 1);
        return n;
    }

    /** Safe to call from ANY thread (UI thread or a background worker thread). */
    public void push(String targetAccountNumberOrAll, String message) {
        Notification n = new Notification(Util.nextId("NTF"), targetAccountNumberOrAll, message);
        store.add(0, n);
        Db.get().update("INSERT INTO notifications (id, target_account, message, timestamp, is_read) VALUES (?,?,?,?,0)",
                n.getId(), n.getTargetAccountNumber(), n.getMessage(), n.getTimestamp().toString());
        Platform.runLater(() -> live.add(0, n));
    }

    public ObservableList<Notification> liveList() { return live; }

    public List<Notification> forAccount(String accountNumber) {
        return store.stream()
                .filter(n -> n.getTargetAccountNumber().equals(accountNumber) || n.getTargetAccountNumber().equals("ALL"))
                .toList();
    }

    public long unreadCountFor(String accountNumber) {
        return forAccount(accountNumber).stream().filter(n -> !n.isRead()).count();
    }

    public void markAllRead(String accountNumber) {
        List<Notification> mine = forAccount(accountNumber);
        for (Notification n : mine) {
            if (!n.isRead()) {
                n.setRead(true);
                Db.get().update("UPDATE notifications SET is_read = 1 WHERE id = ?", n.getId());
            }
        }
        Platform.runLater(() -> live.setAll(store));
    }
}
