package com.bdbank.view.user;

import com.bdbank.model.Notification;
import com.bdbank.service.NotificationService;
import com.bdbank.util.Util;
import com.bdbank.view.Theme;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class NotificationPanel {
    private final String accountNumber;
    public NotificationPanel(String accountNumber) { this.accountNumber = accountNumber; }

    public VBox build() {
        VBox root = new VBox(14);
        root.getChildren().add(Theme.h1("Notifications"));

        VBox list = new VBox(10);
        for (Notification n : NotificationService.get().forAccount(accountNumber)) {
            VBox card = new VBox(4);
            Theme.card(card);
            card.setPadding(new Insets(12));
            if (!n.isRead()) card.setStyle(card.getStyle() + "-fx-border-color:" + Theme.ACCENT_BLUE + "; -fx-border-width: 0 0 0 4;");
            Label msg = new Label(n.getMessage());
            msg.setWrapText(true);
            msg.setStyle("-fx-font-size: 13px;");
            Label time = Theme.muted(Util.format(n.getTimestamp()));
            card.getChildren().addAll(msg, time);
            list.getChildren().add(card);
        }
        if (list.getChildren().isEmpty()) list.getChildren().add(Theme.muted("No notifications yet."));

        NotificationService.get().markAllRead(accountNumber);

        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        root.getChildren().add(scroll);
        return root;
    }
}
