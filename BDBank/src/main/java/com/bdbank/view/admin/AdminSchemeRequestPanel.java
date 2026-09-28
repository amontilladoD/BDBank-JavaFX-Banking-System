package com.bdbank.view.admin;

import com.bdbank.exception.BankException;
import com.bdbank.model.Enums;
import com.bdbank.model.Schemes.DPSScheme;
import com.bdbank.model.Schemes.FDRScheme;
import com.bdbank.model.ServiceRequest;
import com.bdbank.service.RequestService;
import com.bdbank.service.SchemeService;
import com.bdbank.util.AlertUtil;
import com.bdbank.util.Util;
import com.bdbank.view.Theme;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AdminSchemeRequestPanel {
    private final Enums.RequestType type;
    public AdminSchemeRequestPanel(Enums.RequestType type) { this.type = type; }

    public VBox build() {
        VBox root = new VBox(16);
        root.getChildren().add(Theme.h1(type + " Management"));

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(new Tab("Applications", buildApplicationsTab()), new Tab("Schemes / Rates", buildSchemesTab()));
        root.getChildren().add(tabs);
        return root;
    }

    private VBox buildApplicationsTab() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(16));
        ListView<ServiceRequest> list = new ListView<>();
        Runnable refresh = () -> list.setItems(FXCollections.observableArrayList(RequestService.get().allOfType(type)));
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
            try { RequestService.get().approve(sel, remarksField.getText()); refresh.run(); }
            catch (BankException ex) { AlertUtil.error("Error", ex.getMessage()); }
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

    private VBox buildSchemesTab() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(16));
        Label status = new Label();

        if (type == Enums.RequestType.DPS) {
            ListView<DPSScheme> list = new ListView<>(FXCollections.observableArrayList(SchemeService.get().dpsSchemes()));
            VBox.setVgrow(list, Priority.ALWAYS);
            TextField nameField = new TextField(); nameField.setPromptText("Scheme Name");
            TextField rateField = new TextField(); rateField.setPromptText("Annual Rate (%)");
            TextField tenureField = new TextField(); tenureField.setPromptText("Tenure (months)");
            TextField minField = new TextField(); minField.setPromptText("Min Monthly Installment (Tk)");
            Button addBtn = Theme.primaryButton("Add Scheme");
            Button removeBtn = Theme.dangerButton("Remove Selected");
            addBtn.setOnAction(e -> {
                try {
                    DPSScheme s = new DPSScheme(nameField.getText().trim(), Double.parseDouble(rateField.getText()),
                            Integer.parseInt(tenureField.getText()), Double.parseDouble(minField.getText()));
                    SchemeService.get().addDpsScheme(s);
                    list.setItems(FXCollections.observableArrayList(SchemeService.get().dpsSchemes()));
                    status.setStyle("-fx-text-fill:" + Theme.SUCCESS + ";"); status.setText("Scheme added.");
                } catch (Exception ex) { status.setStyle("-fx-text-fill:" + Theme.DANGER + ";"); status.setText("Please fill all fields correctly."); }
            });
            removeBtn.setOnAction(e -> {
                DPSScheme sel = list.getSelectionModel().getSelectedItem();
                if (sel == null) return;
                SchemeService.get().removeDpsScheme(sel);
                list.setItems(FXCollections.observableArrayList(SchemeService.get().dpsSchemes()));
            });
            box.getChildren().addAll(list, new HBox(10, nameField, rateField, tenureField, minField), new HBox(10, addBtn, removeBtn), status);

        } else if (type == Enums.RequestType.FDR) {
            ListView<FDRScheme> list = new ListView<>(FXCollections.observableArrayList(SchemeService.get().fdrSchemes()));
            VBox.setVgrow(list, Priority.ALWAYS);
            TextField nameField = new TextField(); nameField.setPromptText("Scheme Name");
            TextField rateField = new TextField(); rateField.setPromptText("Annual Rate (%)");
            TextField tenureField = new TextField(); tenureField.setPromptText("Tenure (months)");
            TextField minField = new TextField(); minField.setPromptText("Min Amount (Tk)");
            Button addBtn = Theme.primaryButton("Add Scheme");
            Button removeBtn = Theme.dangerButton("Remove Selected");
            addBtn.setOnAction(e -> {
                try {
                    FDRScheme s = new FDRScheme(nameField.getText().trim(), Double.parseDouble(rateField.getText()),
                            Integer.parseInt(tenureField.getText()), Double.parseDouble(minField.getText()));
                    SchemeService.get().addFdrScheme(s);
                    list.setItems(FXCollections.observableArrayList(SchemeService.get().fdrSchemes()));
                    status.setStyle("-fx-text-fill:" + Theme.SUCCESS + ";"); status.setText("Scheme added.");
                } catch (Exception ex) { status.setStyle("-fx-text-fill:" + Theme.DANGER + ";"); status.setText("Please fill all fields correctly."); }
            });
            removeBtn.setOnAction(e -> {
                FDRScheme sel = list.getSelectionModel().getSelectedItem();
                if (sel == null) return;
                SchemeService.get().removeFdrScheme(sel);
                list.setItems(FXCollections.observableArrayList(SchemeService.get().fdrSchemes()));
            });
            box.getChildren().addAll(list, new HBox(10, nameField, rateField, tenureField, minField), new HBox(10, addBtn, removeBtn), status);
        }
        return box;
    }
}
