package com.bdbank.view.user;

import com.bdbank.model.Account;
import com.bdbank.service.AccountService;
import com.bdbank.service.AccountService.TransferChannel;
import com.bdbank.util.AlertUtil;
import com.bdbank.util.ExecutorServiceManager;
import com.bdbank.view.Theme;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class TransferPanel {
    private final Account acc;
    public TransferPanel(Account acc) { this.acc = acc; }

    public VBox build() {
        VBox root = new VBox(16);
        root.getChildren().add(Theme.h1("Balance Transfer"));

        VBox card = new VBox(14);
        card.setMaxWidth(480);
        Theme.card(card);

        Label groupLabel = new Label("Transfer Method");
        groupLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<TransferChannel> channelBox = new ComboBox<>();
        channelBox.getItems().addAll(TransferChannel.values());
        channelBox.setPromptText("Choose channel (Bank: NPSB/BEFTN, Card, or MFS)");
        channelBox.setMaxWidth(Double.MAX_VALUE);

        TextField toField = new TextField();
        toField.setPromptText("Recipient account / card / wallet number");
        TextField amountField = new TextField();
        amountField.setPromptText("Amount (Tk)");

        Label chargeInfo = Theme.muted("Select a channel and amount to see the charge.");
        Label channelHelp = Theme.muted(
                "NPSB: instant, Tk 10 charge under 1,00,000; Tk 20/lac above.\nBEFTN: free, settles next working day.\n" +
                "Card: 1.5% charge.\nbKash / Nagad / Rocket: free.");
        channelHelp.setWrapText(true);

        Runnable updateCharge = () -> {
            try {
                TransferChannel ch = channelBox.getValue();
                double amt = Double.parseDouble(amountField.getText());
                if (ch != null) {
                    double charge = AccountService.get().calculateTransferCharge(ch, amt);
                    chargeInfo.setText(String.format("Charge: Tk %.2f | Total debit: Tk %.2f", charge, amt + charge));
                }
            } catch (Exception ignore) {
                chargeInfo.setText("Select a channel and enter a valid amount to see the charge.");
            }
        };
        channelBox.valueProperty().addListener((o, a, b) -> updateCharge.run());
        amountField.textProperty().addListener((o, a, b) -> updateCharge.run());

        Button sendBtn = Theme.primaryButton("Send Money");
        sendBtn.setMaxWidth(Double.MAX_VALUE);
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setMaxSize(20, 20);
        spinner.setVisible(false);
        Label statusLabel = new Label();
        statusLabel.setWrapText(true);

        sendBtn.setOnAction(e -> {
            statusLabel.setStyle("-fx-text-fill:" + Theme.DANGER + ";");
            try {
                TransferChannel ch = channelBox.getValue();
                if (ch == null) throw new IllegalArgumentException("Please choose a transfer channel.");
                if (toField.getText().isBlank()) throw new IllegalArgumentException("Please enter a recipient.");
                double amt = Double.parseDouble(amountField.getText());

                sendBtn.setDisable(true);
                spinner.setVisible(true);
                statusLabel.setText("");

                // Processing happens off the UI thread - simulates real network/bank-switch latency
                // without ever freezing the interface, then reports back via Platform.runLater.
                ExecutorServiceManager.get().scheduler().submit(() -> {
                    try {
                        Thread.sleep(ch == TransferChannel.BEFTN ? 400 : 900); // simulated processing delay
                        var txn = AccountService.get().transfer(acc, toField.getText().trim(), ch, amt);
                        Platform.runLater(() -> {
                            statusLabel.setStyle("-fx-text-fill:" + Theme.SUCCESS + ";");
                            statusLabel.setText("Success! Transaction ID: " + txn.getId() + ". New balance: Tk " + acc.getBalance());
                            sendBtn.setDisable(false);
                            spinner.setVisible(false);
                            toField.clear(); amountField.clear();
                        });
                    } catch (Exception ex) {
                        Platform.runLater(() -> {
                            statusLabel.setText(ex.getMessage());
                            sendBtn.setDisable(false);
                            spinner.setVisible(false);
                        });
                    }
                });
            } catch (Exception ex) {
                statusLabel.setText(ex.getMessage());
            }
        });

        card.getChildren().addAll(groupLabel, channelBox, toField, amountField, chargeInfo, channelHelp,
                sendBtn, spinner, statusLabel);
        root.getChildren().add(card);
        return root;
    }
}
