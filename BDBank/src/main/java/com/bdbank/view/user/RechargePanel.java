package com.bdbank.view.user;

import com.bdbank.model.Account;
import com.bdbank.service.AccountService;
import com.bdbank.view.Theme;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class RechargePanel {
    private final Account acc;
    public RechargePanel(Account acc) { this.acc = acc; }

    public VBox build() {
        VBox root = new VBox(16);
        root.getChildren().add(Theme.h1("Mobile Recharge"));

        VBox card = new VBox(12);
        card.setMaxWidth(420);
        Theme.card(card);

        ComboBox<String> operatorBox = new ComboBox<>();
        operatorBox.getItems().addAll("Grameenphone", "Robi", "Banglalink", "Teletalk", "Airtel");
        operatorBox.setPromptText("Select Operator");
        operatorBox.setMaxWidth(Double.MAX_VALUE);
        TextField numberField = new TextField(); numberField.setPromptText("Mobile Number");
        TextField amountField = new TextField(); amountField.setPromptText("Amount (Tk)");
        Label statusLabel = new Label();
        statusLabel.setWrapText(true);

        Button rechargeBtn = Theme.primaryButton("Recharge Now");
        rechargeBtn.setMaxWidth(Double.MAX_VALUE);
        rechargeBtn.setOnAction(e -> {
            try {
                if (operatorBox.getValue() == null) throw new IllegalArgumentException("Please select an operator.");
                double amt = Double.parseDouble(amountField.getText());
                var t = AccountService.get().rechargeMobile(acc, operatorBox.getValue(), numberField.getText().trim(), amt);
                statusLabel.setStyle("-fx-text-fill:" + Theme.SUCCESS + ";");
                statusLabel.setText("Recharge successful! New balance: Tk " + acc.getBalance());
                numberField.clear(); amountField.clear();
            } catch (Exception ex) {
                statusLabel.setStyle("-fx-text-fill:" + Theme.DANGER + ";");
                statusLabel.setText(ex.getMessage());
            }
        });

        card.getChildren().addAll(operatorBox, numberField, amountField, rechargeBtn, statusLabel);
        root.getChildren().add(card);
        return root;
    }
}
