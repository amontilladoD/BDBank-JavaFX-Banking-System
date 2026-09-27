package com.bdbank.controller;
import com.bdbank.model.Account;
import com.bdbank.model.Enums;
import com.bdbank.service.AuthService;
import com.bdbank.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {
    @FXML private TextField nameField;
    @FXML private TextField nidField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private ComboBox<Enums.AccountType> typeBox;
    @FXML private PasswordField pwField;
    @FXML private PasswordField pw2Field;
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        typeBox.setItems(FXCollections.observableArrayList(Enums.AccountType.values()));
    }

    @FXML
    private void onRegister() {
        try {
            if (typeBox.getValue() == null) throw new com.bdbank.exception.BankException("Please choose an account type.");
            if (!pwField.getText().equals(pw2Field.getText())) throw new com.bdbank.exception.BankException("Passwords do not match.");
            Account acc = AuthService.get().registerPending(nameField.getText().trim(), nidField.getText().trim(),
                    phoneField.getText().trim(), emailField.getText().trim(), pwField.getText(), typeBox.getValue());
            AlertUtil.info("Registration Submitted",
                    "Your account number is " + acc.getAccountNumber() + ".\nIt is pending admin approval; you'll be able to log in once approved.");
            LoginController.showLoginScreen(com.bdbank.App.primaryStage);
        } catch (Exception ex) {
            statusLabel.setText(ex.getMessage());
        }
    }

    @FXML
    private void onBackToLogin() {
        LoginController.showLoginScreen(com.bdbank.App.primaryStage);
    }

    public static void showRegisterScreen(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(RegisterController.class.getResource("/com/bdbank/fxml/Register.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(RegisterController.class.getResource("/com/bdbank/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("BD Bank - Register");
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Register.fxml", e);
        }
    }
}
