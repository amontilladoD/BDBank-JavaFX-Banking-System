package com.bdbank.view.admin;

import com.bdbank.model.Account;
import com.bdbank.model.Enums;
import com.bdbank.service.AdminBankingService;
import com.bdbank.service.AuthService;
import com.bdbank.util.AlertUtil;
import com.bdbank.view.Theme;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;

public class AdminAccountOpeningPanel {
    public VBox build() {
        VBox root = new VBox(16);
        root.getChildren().add(Theme.h1("Account Opening"));

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(new Tab("Open New Account", buildOpenForm()), new Tab("All Accounts", buildAccountsList()));
        root.getChildren().add(tabs);
        return root;
    }

    private VBox buildOpenForm() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(16));
        box.setMaxWidth(460);

        TextField nameField = new TextField(); nameField.setPromptText("Full Name");
        TextField nidField = new TextField(); nidField.setPromptText("NID");
        TextField phoneField = new TextField(); phoneField.setPromptText("Phone (01XXXXXXXXX)");
        TextField emailField = new TextField(); emailField.setPromptText("Email");
        PasswordField pwField = new PasswordField(); pwField.setPromptText("Initial Password");
        ComboBox<Enums.AccountType> typeBox = new ComboBox<>(FXCollections.observableArrayList(Enums.AccountType.values()));
        typeBox.setPromptText("Account Type"); typeBox.setMaxWidth(Double.MAX_VALUE);
        TextField depositField = new TextField(); depositField.setPromptText("Opening Deposit (Tk)");

        TextField studentIdField = new TextField(); studentIdField.setPromptText("Student ID (Student account only)");
        ComboBox<String> genderBox = new ComboBox<>(FXCollections.observableArrayList("Male", "Female"));
        genderBox.setPromptText("Gender (Woman account only)"); genderBox.setMaxWidth(Double.MAX_VALUE);
        TextField employerField = new TextField(); employerField.setPromptText("Employer / Company (Worker account only)");

        Label status = new Label(); status.setWrapText(true);
        Button submit = Theme.primaryButton("Open Account"); submit.setMaxWidth(Double.MAX_VALUE);
        submit.setOnAction(e -> {
            try {
                double deposit = depositField.getText().isBlank() ? 0 : Double.parseDouble(depositField.getText());
                if (typeBox.getValue() == null) throw new IllegalArgumentException("Please choose an account type.");
                Account acc = AdminBankingService.get().openAccount(typeBox.getValue(), nameField.getText().trim(),
                        nidField.getText().trim(), phoneField.getText().trim(), emailField.getText().trim(), pwField.getText(),
                        deposit, studentIdField.getText().trim(), genderBox.getValue(), employerField.getText().trim());
                status.setStyle("-fx-text-fill:" + Theme.SUCCESS + ";");
                status.setText("Account opened successfully! Account Number: " + acc.getAccountNumber());
                nameField.clear(); nidField.clear(); phoneField.clear(); emailField.clear(); pwField.clear();
                depositField.clear(); studentIdField.clear(); genderBox.setValue(null); employerField.clear();
            } catch (Exception ex) { status.setStyle("-fx-text-fill:" + Theme.DANGER + ";"); status.setText(ex.getMessage()); }
        });

        box.getChildren().addAll(new Label("Basic Information"), nameField, nidField, phoneField, emailField, pwField,
                typeBox, depositField, new Separator(), new Label("Type-specific Criteria"),
                studentIdField, genderBox, employerField, submit, status);
        return box;
    }

    private VBox buildAccountsList() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(16));

        TableView<Account> table = new TableView<>();
        TableColumn<Account, String> accCol = new TableColumn<>("Account No."); accCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        TableColumn<Account, String> nameCol = new TableColumn<>("Name"); nameCol.setCellValueFactory(new PropertyValueFactory<>("accountHolderName"));
        TableColumn<Account, Enums.AccountType> typeCol = new TableColumn<>("Type"); typeCol.setCellValueFactory(new PropertyValueFactory<>("accountType"));
        TableColumn<Account, Double> balCol = new TableColumn<>("Balance"); balCol.setCellValueFactory(new PropertyValueFactory<>("balance"));
        TableColumn<Account, Enums.AccountStatus> statusCol = new TableColumn<>("Status"); statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        table.getColumns().addAll(List.of(accCol, nameCol, typeCol, balCol, statusCol));
        table.setItems(FXCollections.observableArrayList(AuthService.get().allAccounts()));
        VBox.setVgrow(table, Priority.ALWAYS);

        Button approveBtn = Theme.successButton("Approve Selected (Pending -> Active)");
        Button blockBtn = Theme.dangerButton("Block Selected");
        approveBtn.setOnAction(e -> {
            Account sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { AlertUtil.error("Select an account", "Please select an account first."); return; }
            AdminBankingService.get().approveExistingAccount(sel);
            table.refresh();
        });
        blockBtn.setOnAction(e -> {
            Account sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { AlertUtil.error("Select an account", "Please select an account first."); return; }
            AdminBankingService.get().blockAccount(sel);
            table.refresh();
        });

        box.getChildren().addAll(new HBox(10, approveBtn, blockBtn), table);
        return box;
    }
}
