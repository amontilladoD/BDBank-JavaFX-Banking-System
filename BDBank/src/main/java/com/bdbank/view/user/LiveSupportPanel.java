package com.bdbank.view.user;

import com.bdbank.model.ChatMessage;
import com.bdbank.service.ChatService;
import com.bdbank.util.Util;
import com.bdbank.view.Theme;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class LiveSupportPanel {
    private final String accountNumber;
    private final boolean isAdmin;

    public LiveSupportPanel(String accountNumber, boolean isAdmin) {
        this.accountNumber = accountNumber;
        this.isAdmin = isAdmin;
    }

    public VBox build() {
        VBox root = new VBox(12);
        root.getChildren().add(Theme.h1(isAdmin ? "Support Chat: " + accountNumber : "Live Support"));

        VBox messages = new VBox(8);
        messages.setPadding(new Insets(10));
        ScrollPane scroll = new ScrollPane(messages);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(420);
        scroll.setStyle("-fx-background-color: white; -fx-background: white;");

        Runnable rerender = () -> {
            messages.getChildren().clear();
            for (ChatMessage m : ChatService.get().liveConversation(accountNumber)) {
                messages.getChildren().add(bubble(m));
            }
        };
        rerender.run();
        ChatService.get().liveConversation(accountNumber).addListener((ListChangeListener<ChatMessage>) c -> rerender.run());

        TextField input = new TextField();
        input.setPromptText(isAdmin ? "Type a reply to the customer..." : "Type your message to support...");
        Button send = Theme.primaryButton("Send");
        Runnable doSend = () -> {
            String text = input.getText().trim();
            if (text.isEmpty()) return;
            if (isAdmin) ChatService.get().sendFromAdmin(accountNumber, text);
            else ChatService.get().sendFromUser(accountNumber, text);
            input.clear();
        };
        send.setOnAction(e -> doSend.run());
        input.setOnAction(e -> doSend.run());

        HBox inputBar = new HBox(10, input, send);
        HBox.setHgrow(input, javafx.scene.layout.Priority.ALWAYS);

        root.getChildren().addAll(scroll, inputBar);
        return root;
    }

    private HBox bubble(ChatMessage m) {
        boolean fromMe = (isAdmin && m.getSender().equals("ADMIN")) || (!isAdmin && m.getSender().equals("USER"));
        Label text = new Label(m.getText() + "\n" + Util.format(m.getTimestamp()));
        text.setWrapText(true);
        text.setMaxWidth(320);
        text.setStyle("-fx-background-color: " + (fromMe ? Theme.ACCENT_BLUE : "#eceff1") + "; " +
                "-fx-text-fill: " + (fromMe ? "white" : Theme.TEXT_DARK) + "; -fx-padding: 8 12 8 12; -fx-background-radius: 10; -fx-font-size: 11px;");
        HBox row = new HBox(text);
        row.setAlignment(fromMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        return row;
    }
}
