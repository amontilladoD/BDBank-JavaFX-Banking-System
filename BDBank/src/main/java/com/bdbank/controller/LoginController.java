package com.bdbank.controller;

import com.bdbank.model.Account;
import com.bdbank.model.AdminUser;
import com.bdbank.service.AuthService;
import com.bdbank.util.ExecutorServiceManager;
import com.bdbank.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML private RadioButton userRadio;
    @FXML private RadioButton adminRadio;
    @FXML private Label idLabel;
    @FXML private TextField idField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginBtn;
    @FXML private ProgressIndicator spinner;
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        adminRadio.selectedProperty().addListener((obs, was, isNow) -> {
            idLabel.setText(isNow ? "Admin ID" : "Account Number");
            idField.setPromptText(isNow ? "e.g. admin" : "e.g. BDB1234567890");
        });
    }

    @FXML
    private void onLogin() {
        String id = idField.getText().trim();
        String pw = passwordField.getText();
        if (id.isEmpty() || pw.isEmpty()) {
            statusLabel.setText("Please fill in both fields.");
            return;
        }
        statusLabel.setText("");
        loginBtn.setDisable(true);
        spinner.setVisible(true);

        boolean adminMode = adminRadio.isSelected();
        Stage stage = com.bdbank.App.primaryStage;

        ExecutorServiceManager.get().scheduler().submit(() -> {
            try {
                if (adminMode) {
                    AdminUser admin = AuthService.get().authenticateAdmin(id, pw);
                    Platform.runLater(() -> {
                        SessionManager.get().loginAsAdmin(admin);
                        AdminDashboardController.show(stage);
                    });
                } else {
                    Account acc = AuthService.get().authenticateUser(id, pw);
                    Platform.runLater(() -> {
                        SessionManager.get().loginAsUser(acc);
                        UserDashboardController.show(stage);
                    });
                }
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    statusLabel.setText(ex.getMessage());
                    loginBtn.setDisable(false);
                    spinner.setVisible(false);
                });
            }
        });
    }

    @FXML
    private void onGoRegister() {
        RegisterController.showRegisterScreen(com.bdbank.App.primaryStage);
    }

    /** Loads Login.fxml and shows it on the given stage. Called from Splash, Register and Logout. */
    public static void showLoginScreen(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(LoginController.class.getResource("/com/bdbank/fxml/Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(LoginController.class.getResource("/com/bdbank/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("BD Bank - Login");
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Login.fxml", e);
        }
    }
}
