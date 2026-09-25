package com.bdbank.view.admin;

import com.bdbank.exception.BankException;
import com.bdbank.model.Enums;
import com.bdbank.model.Schemes.LoanScheme;
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

import java.util.List;

public class AdminLoanManagementPanel {
    public VBox build() {
        VBox root = new VBox(16);
        root.getChildren().add(Theme.h1("Loan Management"));

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(
                new Tab("Applications (Review)", buildApplicationsTab()),
                new Tab("Loan Schemes", buildSchemesTab())
        );
        root.getChildren().add(tabs);
        return root;
    }

    private VBox buildApplicationsTab() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(16));

        ListView<ServiceRequest> list = new ListView<>();
        Runnable refresh = () -> list.setItems(FXCollections.observableArrayList(RequestService.get().allOfType(Enums.RequestType.LOAN)));
        list.setCellFactory(v -> new ListCell<>() {
            @Override protected void updateItem(ServiceRequest r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) { setText(null); }
                else setText(r.getAccountNumber() + " | " + r.summary() + " | Status: " + r.getStatus() +
                        " | Submitted: " + Util.format(r.getSubmittedAt()));
            }
        });
        refresh.run();
        VBox.setVgrow(list, Priority.ALWAYS);

        TextField remarksField = new TextField(); remarksField.setPromptText("Remarks (optional)");
        Button approve = Theme.successButton("Approve & Disburse");
        Button reject = Theme.dangerButton("Reject");
        approve.setOnAction(e -> {
            ServiceRequest sel = list.getSelectionModel().getSelectedItem();
            if (sel == null) { AlertUtil.error("No selection", "Please select a loan application."); return; }
            if (sel.getStatus() != Enums.RequestStatus.PENDING) { AlertUtil.error("Already processed", "This application is not pending."); return; }
            try {
                RequestService.get().approve(sel, remarksField.getText());
                refresh.run();
            } catch (BankException ex) { AlertUtil.error("Error", ex.getMessage()); }
        });
        reject.setOnAction(e -> {
            ServiceRequest sel = list.getSelectionModel().getSelectedItem();
            if (sel == null) { AlertUtil.error("No selection", "Please select a loan application."); return; }
            RequestService.get().reject(sel, remarksField.getText());
            refresh.run();
        });

        box.getChildren().addAll(list, remarksField, new HBox(10, approve, reject));
        return box;
    }

    private VBox buildSchemesTab() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(16));

        ListView<LoanScheme> list = new ListView<>(FXCollections.observableArrayList(SchemeService.get().loanSchemes()));
        VBox.setVgrow(list, Priority.ALWAYS);

        TextField nameField = new TextField(); nameField.setPromptText("Scheme Name");
        TextField rateField = new TextField(); rateField.setPromptText("Annual Rate (%)");
        TextField maxAmtField = new TextField(); maxAmtField.setPromptText("Max Amount (Tk)");
        TextField maxTenureField = new TextField(); maxTenureField.setPromptText("Max Tenure (months)");
        TextField minBalField = new TextField(); minBalField.setPromptText("Min Eligible Balance (Tk)");

        Button addBtn = Theme.primaryButton("Add Scheme");
        Button removeBtn = Theme.dangerButton("Remove Selected");
        Label status = new Label();

        addBtn.setOnAction(e -> {
            try {
                LoanScheme s = new LoanScheme(nameField.getText().trim(), Double.parseDouble(rateField.getText()),
                        Double.parseDouble(maxAmtField.getText()), Integer.parseInt(maxTenureField.getText()),
                        Double.parseDouble(minBalField.getText()));
                SchemeService.get().addLoanScheme(s);
                list.setItems(FXCollections.observableArrayList(SchemeService.get().loanSchemes()));
                status.setStyle("-fx-text-fill:" + Theme.SUCCESS + ";");
                status.setText("Scheme added.");
            } catch (Exception ex) { status.setStyle("-fx-text-fill:" + Theme.DANGER + ";"); status.setText("Please fill all fields with valid numbers."); }
        });
        removeBtn.setOnAction(e -> {
            LoanScheme sel = list.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            SchemeService.get().removeLoanScheme(sel);
            list.setItems(FXCollections.observableArrayList(SchemeService.get().loanSchemes()));
        });

        box.getChildren().addAll(list, new HBox(10, nameField, rateField), new HBox(10, maxAmtField, maxTenureField, minBalField),
                new HBox(10, addBtn, removeBtn), status);
        return box;
    }
}
