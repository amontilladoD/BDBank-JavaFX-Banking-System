package com.bdbank.service;

import com.bdbank.model.ChatMessage;
import com.bdbank.util.ExecutorServiceManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/** Live support chat, backed by SQLite's `chat_messages` table (append-only, like transactions). */
public class ChatService {
    private static final ChatService INSTANCE = new ChatService();
    public static ChatService get() { return INSTANCE; }

    private final List<ChatMessage> log;
    private final ConcurrentHashMap<String, ObservableList<ChatMessage>> liveByAccount = new ConcurrentHashMap<>();

    private ChatService() {
        log = new CopyOnWriteArrayList<>(
                Db.get().query("SELECT * FROM chat_messages ORDER BY timestamp ASC", this::mapMessage));
    }

    private ChatMessage mapMessage(ResultSet rs) throws SQLException {
        ChatMessage m = new ChatMessage(rs.getString("account_number"), rs.getString("sender"), rs.getString("text"));
        m.hydrateTimestamp(LocalDateTime.parse(rs.getString("timestamp")));
        return m;
    }

    public ObservableList<ChatMessage> liveConversation(String accountNumber) {
        return liveByAccount.computeIfAbsent(accountNumber, k -> {
            ObservableList<ChatMessage> list = FXCollections.observableArrayList();
            List<ChatMessage> existing = log.stream().filter(m -> m.getAccountNumber().equals(accountNumber)).toList();
            Platform.runLater(() -> list.setAll(existing));
            return list;
        });
    }

    public List<String> allConversationAccountNumbers() {
        return log.stream().map(ChatMessage::getAccountNumber).distinct().toList();
    }

    public void sendFromUser(String accountNumber, String text) {
        addMessage(new ChatMessage(accountNumber, "USER", text));
        // Demo auto-reply: simulates a support agent typing back, off the UI thread.
        ExecutorServiceManager.get().scheduler().schedule(() ->
                addMessage(new ChatMessage(accountNumber, "ADMIN",
                        "Thanks for reaching out! A support agent will assist you shortly regarding: \"" + trim(text) + "\"")),
                2, TimeUnit.SECONDS);
    }

    public void sendFromAdmin(String accountNumber, String text) {
        addMessage(new ChatMessage(accountNumber, "ADMIN", text));
    }

    private String trim(String s) { return s.length() > 40 ? s.substring(0, 40) + "..." : s; }

    private void addMessage(ChatMessage m) {
        log.add(m);
        Db.get().update("INSERT INTO chat_messages (account_number, sender, text, timestamp) VALUES (?,?,?,?)",
                m.getAccountNumber(), m.getSender(), m.getText(), m.getTimestamp().toString());
        ObservableList<ChatMessage> live = liveConversation(m.getAccountNumber());
        Platform.runLater(() -> live.add(m));
    }
}
