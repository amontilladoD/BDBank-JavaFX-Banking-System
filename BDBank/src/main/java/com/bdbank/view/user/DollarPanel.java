package com.bdbank.view.user;

import com.bdbank.model.Account;
import com.bdbank.model.Enums;
import com.bdbank.model.RequestTypes;
import com.bdbank.model.ServiceRequest;
import com.bdbank.service.ConfigService;
import com.bdbank.service.RequestService;
import com.bdbank.util.Util;
import com.bdbank.view.Theme;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DollarPanel {
    private final Account acc;
    public DollarPanel(Account acc) { this.acc = acc; }

    public VBox build() {
        VBox root = new VBox(16);
        root.getChildren().add(Theme.h1("Dollar Endorsement"));

        var cfg = ConfigService.get().config();
        VBox rateCard = new VBox(6);
        Theme.card(rateCard);
        Label rateLabel = new Label(String.format("Buy: Tk %.2f   |   Sell: Tk %.2f", cfg.dollarBuyRate, cfg.dollarSellRate));
        rateLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill:" + Theme.NAVY + ";");
        Label updated = Theme.muted("Last updated: " + Util.format(cfg.dollarRateUpdatedAt) + " (auto-updates periodically)");
        rateCard.getChildren().addAll(rateLabel, updated);

        VBox formCard = new VBox(12);
        formCard.setMaxWidth(420);
        Theme.card(formCard);
        TextField amountField = new TextField(); amountField.setPromptText("Amount in USD");
        TextField purposeField = new TextField(); purposeField.setPromptText("Purpose (Travel, Education, Medical, etc.)");
        Label status = new Label(); status.setWrapText(true);
        Button submit = Theme.primaryButton("Submit Endorsement Request"); submit.setMaxWidth(Double.MAX_VALUE);
        submit.setOnAction(e -> {
            try {
                double usd = Double.parseDouble(amountField.getText());
                if (purposeField.getText().isBlank()) throw new IllegalArgumentException("Please state a purpose.");
                var req = new RequestTypes.DollarEndorsementRequest(Util.nextId("DLR"), acc.getAccountNumber());
                req.put("amountUsd", String.valueOf(usd)); req.put("purpose", purposeField.getText().trim());
                req.put("rateAtRequest", String.valueOf(cfg.dollarSellRate));
                RequestService.get().submit(req);
                status.setStyle("-fx-text-fill:" + Theme.SUCCESS + ";");
                status.setText("Request submitted for admin approval.");
                amountField.clear(); purposeField.clear();
            } catch (Exception ex) { status.setStyle("-fx-text-fill:" + Theme.DANGER + ";"); status.setText(ex.getMessage()); }
        });
        formCard.getChildren().addAll(amountField, purposeField, submit, status);

        VBox myReqs = new VBox(8);
        for (ServiceRequest r : RequestService.get().forAccount(acc.getAccountNumber(), Enums.RequestType.DOLLAR_ENDORSEMENT)) {
            VBox c = new VBox(4);
            Theme.card(c);
            c.getChildren().addAll(new Label(r.summary()), new Label("Status: " + r.getStatus()));
            myReqs.getChildren().add(c);
        }

        root.getChildren().addAll(rateCard, formCard, Theme.h2("My Requests"), myReqs);
        return root;
    }
}
