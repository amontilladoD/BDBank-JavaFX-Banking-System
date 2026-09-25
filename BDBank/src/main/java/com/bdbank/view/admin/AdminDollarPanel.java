package com.bdbank.view.admin;

import com.bdbank.exception.BankException;
import com.bdbank.model.BankConfig;
import com.bdbank.model.Enums;
import com.bdbank.model.ServiceRequest;
import com.bdbank.service.ConfigService;
import com.bdbank.service.RequestService;
import com.bdbank.util.AlertUtil;
import com.bdbank.util.Util;
import com.bdbank.view.Theme;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AdminDollarPanel {
    private static final double MAX_ENDORSEMENT_USD = 12000; // annual individual travel-quota style limit

    public VBox build() {
        VBox root = new VBox(16);
        root.getChildren().add(Theme.h1("Dollar Endorsement Management"));

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(new Tab("Daily Rate", buildRateTab()), new Tab("Applications", buildApplicationsTab()));
        root.getChildren().add(tabs);
        return root;
    }

    private VBox buildRateTab() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(16));
        box.setMaxWidth(420);
        BankConfig cfg = ConfigService.get().config();

        Label info = Theme.muted("This rate also auto-fluctuates every ~30-45 seconds via a background job to simulate a live market feed. You can override it manually here.");
        info.setWrapText(true);
        TextField buyField = new TextField(String.valueOf(cfg.dollarBuyRate));
        TextField sellField = new TextField(String.valueOf(cfg.dollarSellRate));
        Label status = new Label();
        Button save = Theme.primaryButton("Update Rate Manually");
        save.setOnAction(e -> {
            try {
                cfg.dollarBuyRate = Double.parseDouble(buyField.getText());
                cfg.dollarSellRate = Double.parseDouble(sellField.getText());
                cfg.dollarRateUpdatedAt = java.time.LocalDateTime.now();
                ConfigService.get().save();
                status.setStyle("-fx-text-fill:" + Theme.SUCCESS + ";");
                status.setText("Rate updated. This will reflect for users immediately.");
            } catch (Exception ex) { status.setStyle("-fx-text-fill:" + Theme.DANGER + ";"); status.setText("Please enter valid numbers."); }
        });
        box.getChildren().addAll(info, new Label("Buy Rate (Tk):"), buyField, new Label("Sell Rate (Tk):"), sellField, save, status);
        return box;
    }

    private VBox buildApplicationsTab() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(16));
        ListView<ServiceRequest> list = new ListView<>();
        Runnable refresh = () -> list.setItems(FXCollections.observableArrayList(RequestService.get().allOfType(Enums.RequestType.DOLLAR_ENDORSEMENT)));
        list.setCellFactory(v -> new ListCell<>() {
            @Override protected void updateItem(ServiceRequest r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? null : r.getAccountNumber() + " | " + r.summary() + " | Status: " + r.getStatus() +
                        " | " + Util.format(r.getSubmittedAt()));
            }
        });
        refresh.run();
        VBox.setVgrow(list, Priority.ALWAYS);
        Label limitInfo = Theme.muted("Bank policy limit per endorsement: $" + MAX_ENDORSEMENT_USD + ". Approval debits the account in Tk at the current sell rate.");
        TextField remarksField = new TextField(); remarksField.setPromptText("Remarks (optional)");
        Button approve = Theme.successButton("Approve (within limit)");
        Button reject = Theme.dangerButton("Reject");
        approve.setOnAction(e -> {
            ServiceRequest sel = list.getSelectionModel().getSelectedItem();
            if (sel == null) { AlertUtil.error("No selection", "Please select an application."); return; }
            if (sel.getStatus() != Enums.RequestStatus.PENDING) { AlertUtil.error("Already processed", "Not pending."); return; }
            if (sel.getDouble("amountUsd") > MAX_ENDORSEMENT_USD) {
                AlertUtil.error("Exceeds limit", "Requested amount exceeds the $" + MAX_ENDORSEMENT_USD + " policy limit."); return;
            }
            try { RequestService.get().approve(sel, remarksField.getText()); refresh.run(); }
            catch (BankException ex) { AlertUtil.error("Error", ex.getMessage()); }
        });
        reject.setOnAction(e -> {
            ServiceRequest sel = list.getSelectionModel().getSelectedItem();
            if (sel == null) { AlertUtil.error("No selection", "Please select an application."); return; }
            RequestService.get().reject(sel, remarksField.getText());
            refresh.run();
        });
        box.getChildren().addAll(limitInfo, list, remarksField, new HBox(10, approve, reject));
        return box;
    }
}
