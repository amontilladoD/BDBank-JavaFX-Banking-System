package com.bdbank.controller;

import com.bdbank.model.AdminUser;
import com.bdbank.model.Enums;
import com.bdbank.util.AlertUtil;
import com.bdbank.util.SessionManager;
import com.bdbank.view.admin.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.List;

/** Controller bound to fxml/AdminDashboard.fxml - same shell pattern as the user dashboard. */
public class AdminDashboardController {

    @FXML private Button btnTxn, btnDeposit, btnAccountOpening, btnStatement, btnAssets, btnRates, btnLoan, btnFdr, btnDps,
            btnCardCheque, btnLocker, btnDollar, btnSupport;
    @FXML private Label welcomeLabel;
    @FXML private StackPane contentHolder;

    private List<Button> navButtons;

    @FXML
    private void initialize() {
        AdminUser admin = SessionManager.get().getCurrentAdmin();
        welcomeLabel.setText("Welcome, " + admin.getName() + " (" + admin.getAdminId() + ")");

        navButtons = List.of(btnTxn, btnDeposit, btnAccountOpening, btnStatement, btnAssets, btnRates, btnLoan, btnFdr, btnDps,
                btnCardCheque, btnLocker, btnDollar, btnSupport);

        onTransactions(); // default view
    }

    private void setActive(Button active) {
        for (Button b : navButtons) b.getStyleClass().setAll(b == active ? "sidebar-btn-active" : "sidebar-btn");
    }

    private void setContent(javafx.scene.Node node) { contentHolder.getChildren().setAll(node); }

    @FXML private void onTransactions()     { setActive(btnTxn); setContent(new AdminTransactionPanel().build()); }
    @FXML private void onDeposit()          { setActive(btnDeposit); setContent(new AdminDepositPanel().build()); }
    @FXML private void onAccountOpening()   { setActive(btnAccountOpening); setContent(new AdminAccountOpeningPanel().build()); }
    @FXML private void onStatement()        { setActive(btnStatement); setContent(new AdminStatementPanel().build()); }
    @FXML private void onAssets()           { setActive(btnAssets); setContent(new AdminAssetsPanel().build()); }
    @FXML private void onRates()            { setActive(btnRates); setContent(new AdminRatesPanel().build()); }
    @FXML private void onLoanManagement()   { setActive(btnLoan); setContent(new AdminLoanManagementPanel().build()); }
    @FXML private void onFdrManagement()    { setActive(btnFdr); setContent(new AdminSchemeRequestPanel(Enums.RequestType.FDR).build()); }
    @FXML private void onDpsManagement()    { setActive(btnDps); setContent(new AdminSchemeRequestPanel(Enums.RequestType.DPS).build()); }
    @FXML private void onCardCheque()       { setActive(btnCardCheque); setContent(new AdminCardChequePanel().build()); }
    @FXML private void onLocker()           { setActive(btnLocker); setContent(new AdminLockerPanel().build()); }
    @FXML private void onDollar()           { setActive(btnDollar); setContent(new AdminDollarPanel().build()); }
    @FXML private void onSupport()          { setActive(btnSupport); setContent(new AdminSupportPanel().build()); }

    @FXML
    private void onLogout() {
        if (AlertUtil.confirm("Logout", "Are you sure you want to log out?")) {
            SessionManager.get().logout();
            LoginController.showLoginScreen(com.bdbank.App.primaryStage);
        }
    }

    /** Loads AdminDashboard.fxml and shows it. Called right after a successful admin login. */
    public static void show(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(AdminDashboardController.class.getResource("/com/bdbank/fxml/AdminDashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1150, 720);
            scene.getStylesheets().add(AdminDashboardController.class.getResource("/com/bdbank/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("BD Bank - Admin Dashboard");
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load AdminDashboard.fxml", e);
        }
    }
}
