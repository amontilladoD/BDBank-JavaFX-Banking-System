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

public class AdminLockerPanel {
    public VBox build() {
        VBox root = new VBox(16);
        root.getChildren().add(Theme.h1("Locker Management"));

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(new Tab("Applications", buildApplicationsTab()), new Tab("Rates & Availability", buildRatesTab()));
        root.getChildren().add(tabs);
        return root;
    }

    private VBox buildApplicationsTab() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(16));
        ListView<ServiceRequest> list = new ListView<>();
        Runnable refresh = () -> list.setItems(FXCollections.observableArrayList(RequestService.get().allOfType(Enums.RequestType.LOCKER)));
        list.setCellFactory(v -> new ListCell<>() {
            @Override protected void updateItem(ServiceRequest r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? null : r.getAccountNumber() + " | " + r.summary() + " | Status: " + r.getStatus() +
                        " | " + Util.format(r.getSubmittedAt()));
            }
        });
        refresh.run();
        VBox.setVgrow(list, Priority.ALWAYS);
        TextField remarksField = new TextField(); remarksField.setPromptText("Remarks (optional)");
        Button approve = Theme.successButton("Approve");
        Button reject = Theme.dangerButton("Reject");
        approve.setOnAction(e -> {
            ServiceRequest sel = list.getSelectionModel().getSelectedItem();
            if (sel == null) { AlertUtil.error("No selection", "Please select an application."); return; }
            if (sel.getStatus() != Enums.RequestStatus.PENDING) { AlertUtil.error("Already processed", "Not pending."); return; }
            try {
                RequestService.get().approve(sel, remarksField.getText());
                BankConfig cfg = ConfigService.get().config();
                switch (sel.get("size")) {
                    case "Small" -> cfg.lockerAvailableSmall = Math.max(0, cfg.lockerAvailableSmall - 1);
                    case "Medium" -> cfg.lockerAvailableMedium = Math.max(0, cfg.lockerAvailableMedium - 1);
                    default -> cfg.lockerAvailableLarge = Math.max(0, cfg.lockerAvailableLarge - 1);
                }
                ConfigService.get().save();
                refresh.run();
            } catch (BankException ex) { AlertUtil.error("Error", ex.getMessage()); }
        });
        reject.setOnAction(e -> {
            ServiceRequest sel = list.getSelectionModel().getSelectedItem();
            if (sel == null) { AlertUtil.error("No selection", "Please select an application."); return; }
            RequestService.get().reject(sel, remarksField.getText());
            refresh.run();
        });
        box.getChildren().addAll(list, remarksField, new HBox(10, approve, reject));
        return box;
    }

    private VBox buildRatesTab() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(16));
        box.setMaxWidth(480);
        BankConfig cfg = ConfigService.get().config();

        TextField smallFee = new TextField(String.valueOf(cfg.lockerSmallFee));
        TextField smallAvail = new TextField(String.valueOf(cfg.lockerAvailableSmall));
        TextField medFee = new TextField(String.valueOf(cfg.lockerMediumFee));
        TextField medAvail = new TextField(String.valueOf(cfg.lockerAvailableMedium));
        TextField largeFee = new TextField(String.valueOf(cfg.lockerLargeFee));
        TextField largeAvail = new TextField(String.valueOf(cfg.lockerAvailableLarge));

        Label status = new Label();
        Button save = Theme.primaryButton("Save Locker Settings");
        save.setOnAction(e -> {
            try {
                cfg.lockerSmallFee = Double.parseDouble(smallFee.getText());
                cfg.lockerAvailableSmall = Integer.parseInt(smallAvail.getText());
                cfg.lockerMediumFee = Double.parseDouble(medFee.getText());
                cfg.lockerAvailableMedium = Integer.parseInt(medAvail.getText());
                cfg.lockerLargeFee = Double.parseDouble(largeFee.getText());
                cfg.lockerAvailableLarge = Integer.parseInt(largeAvail.getText());
                ConfigService.get().save();
                status.setStyle("-fx-text-fill:" + Theme.SUCCESS + ";");
                status.setText("Locker settings updated.");
            } catch (Exception ex) { status.setStyle("-fx-text-fill:" + Theme.DANGER + ";"); status.setText("Please enter valid numbers."); }
        });

        box.getChildren().addAll(
                new Label("Small Locker - Fee (Tk/yr) & Availability:"), smallFee, smallAvail,
                new Label("Medium Locker - Fee (Tk/yr) & Availability:"), medFee, medAvail,
                new Label("Large Locker - Fee (Tk/yr) & Availability:"), largeFee, largeAvail,
                save, status);
        return box;
    }
}
