package com.bdbank.view.user;

import com.bdbank.model.Account;
import com.bdbank.model.Enums;
import com.bdbank.service.AccountService;
import com.bdbank.view.Theme;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class BillPanel {
    private final Account acc;
    private final Enums.TxnCategory category;
    private final String title;

    public BillPanel(Account acc, Enums.TxnCategory category, String title) {
        this.acc = acc; this.category = category; this.title = title;
    }

    public VBox build() {
        VBox root = new VBox(16);
        root.getChildren().add(Theme.h1(title));

        VBox card = new VBox(12);
        card.setMaxWidth(420);
        Theme.card(card);

        TextField refField = new TextField(); refField.setPromptText("Biller / Reference Number");
        TextField amountField = new TextField(); amountField.setPromptText("Amount (Tk)");
        Label statusLabel = new Label();
        statusLabel.setWrapText(true);

        Button payBtn = Theme.primaryButton("Pay Now");
        payBtn.setMaxWidth(Double.MAX_VALUE);
        payBtn.setOnAction(e -> {
            try {
                double amt = Double.parseDouble(amountField.getText());
                AccountService.get().payBill(acc, category, refField.getText().trim(), amt);
                statusLabel.setStyle("-fx-text-fill:" + Theme.SUCCESS + ";");
                statusLabel.setText("Payment successful! New balance: Tk " + acc.getBalance());
                refField.clear(); amountField.clear();
            } catch (Exception ex) {
                statusLabel.setStyle("-fx-text-fill:" + Theme.DANGER + ";");
                statusLabel.setText(ex.getMessage());
            }
        });

        card.getChildren().addAll(refField, amountField, payBtn, statusLabel);
        root.getChildren().add(card);
        return root;
    }
}
