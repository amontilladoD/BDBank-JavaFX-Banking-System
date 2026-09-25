package com.bdbank.view.admin;

import com.bdbank.model.Account;
import com.bdbank.service.AdminBankingService;
import com.bdbank.service.AuthService;
import com.bdbank.view.Theme;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/** Simulates a teller taking physical cash from a customer and depositing it into their account. */
public class AdminDepositPanel {
    public VBox build() {
        VBox root = new VBox(16);
        root.getChildren().add(Theme.h1("Cash Deposit"));

        VBox card = new VBox(12);
        card.setMaxWidth(460);
        Theme.card(card);

        ComboBox<Account> accountBox = new ComboBox<>(FXCollections.observableArrayList(AuthService.get().allAccounts()));
        accountBox.setPromptText("Choose customer account");
        accountBox.setMaxWidth(Double.MAX_VALUE);
        accountBox.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Account a) { return a == null ? "" : a.getAccountNumber() + " - " + a.getAccountHolderName(); }
            @Override public Account fromString(String s) { return null; }
        });

        Label currentBalance = new Label();
        accountBox.valueProperty().addListener((obs, old, val) ->
                currentBalance.setText(val == null ? "" : "Current balance: Tk " + val.getBalance()));

        TextField amountField = new TextField();
        amountField.setPromptText("Cash amount to deposit (Tk)");

        Label status = new Label();
        status.setWrapText(true);

        Button depositBtn = Theme.primaryButton("Deposit Cash");
        depositBtn.setMaxWidth(Double.MAX_VALUE);
        depositBtn.setOnAction(e -> {
            try {
                Account acc = accountBox.getValue();
                if (acc == null) throw new IllegalArgumentException("Please choose an account.");
                double amt = Double.parseDouble(amountField.getText());
                AdminBankingService.get().depositCash(acc, amt);
                status.setStyle("-fx-text-fill:" + Theme.SUCCESS + ";");
                status.setText("Deposited Tk " + amt + " into " + acc.getAccountNumber() + ". New balance: Tk " + acc.getBalance());
                currentBalance.setText("Current balance: Tk " + acc.getBalance());
                amountField.clear();
            } catch (Exception ex) {
                status.setStyle("-fx-text-fill:" + Theme.DANGER + ";");
                status.setText(ex.getMessage());
            }
        });

        card.getChildren().addAll(accountBox, currentBalance, amountField, depositBtn, status);
        root.getChildren().addAll(card, Theme.muted(
                "This represents a customer depositing physical cash at a branch counter. " +
                "It's recorded as a normal DEPOSIT transaction on the account's statement, timestamped automatically."));
        return root;
    }
}
