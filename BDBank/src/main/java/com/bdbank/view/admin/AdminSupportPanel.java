package com.bdbank.view.admin;

import com.bdbank.service.ChatService;
import com.bdbank.view.Theme;
import com.bdbank.view.user.LiveSupportPanel;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class AdminSupportPanel {
    public VBox build() {
        VBox root = new VBox(16);
        root.getChildren().add(Theme.h1("Customer Support"));

        HBox layout = new HBox(16);
        VBox.setVgrow(layout, Priority.ALWAYS);

        VBox convoList = new VBox(8);
        convoList.setPrefWidth(240);
        List<String> accounts = ChatService.get().allConversationAccountNumbers();
        StackPane chatHolder = new StackPane();
        HBox.setHgrow(chatHolder, Priority.ALWAYS);

        if (accounts.isEmpty()) {
            convoList.getChildren().add(Theme.muted("No customer conversations yet."));
        }
        for (String accNo : accounts) {
            Button b = Theme.secondaryButton(accNo);
            b.setMaxWidth(Double.MAX_VALUE);
            b.setOnAction(e -> chatHolder.getChildren().setAll(new LiveSupportPanel(accNo, true).build()));
            convoList.getChildren().add(b);
        }
        if (!accounts.isEmpty()) chatHolder.getChildren().setAll(new LiveSupportPanel(accounts.get(0), true).build());
        else chatHolder.getChildren().setAll(Theme.muted("Select a conversation once a customer messages support."));

        layout.getChildren().addAll(convoList, chatHolder);
        root.getChildren().add(layout);
        return root;
    }
}
