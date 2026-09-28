package com.bdbank.controller;

import com.bdbank.model.Account;
import com.bdbank.model.Enums;
import com.bdbank.service.NotificationService;
import com.bdbank.util.AlertUtil;
import com.bdbank.util.SessionManager;
import com.bdbank.view.user.*;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.List;

public class UserDashboardController {

    @FXML private Button btnBalance, btnStatement, btnTransfer, btnRecharge, btnLoan, btnDps, btnFdr,
            btnCardCheque, btnLocker, btnDollar, btnToll, btnUtility, btnSupport;
    @FXML private Label welcomeLabel;
    @FXML private Label acctInfoLabel;
    @FXML private Label bellBadge;
    @FXML private StackPane contentHolder;

    private Account acc;
    private List<Button> navButtons;

    @FXML
    private void initialize() {
        acc = SessionManager.get().getCurrentAccount();
        welcomeLabel.setText("Welcome, " + acc.getAccountHolderName());
        acctInfoLabel.setText(acc.getAccountNumber() + " | " + acc.getAccountType().label + " Account");

        navButtons = List.of(btnBalance, btnStatement, btnTransfer, btnRecharge, btnLoan, btnDps, btnFdr,
                btnCardCheque, btnLocker, btnDollar, btnToll, btnUtility, btnSupport);

        refreshBadge();
        NotificationService.get().liveList().addListener((ListChangeListener<Object>) c -> refreshBadge());

        onBalanceInquiry(); // default view
    }

    private void refreshBadge() {
        long unread = NotificationService.get().unreadCountFor(acc.getAccountNumber());
        bellBadge.setText(unread > 0 ? String.valueOf(unread) : "");
        bellBadge.setVisible(unread > 0);
    }

    private void setActive(Button active) {
        for (Button b : navButtons) b.getStyleClass().setAll(b == active ? "sidebar-btn-active" : "sidebar-btn");
    }

    private void setContent(javafx.scene.Node node) { contentHolder.getChildren().setAll(node); }

    @FXML private void onBalanceInquiry() { setActive(btnBalance); setContent(new BalanceInquiryPanel(acc).build()); }
    @FXML private void onStatement()      { setActive(btnStatement); setContent(new StatementPanel(acc).build()); }
    @FXML private void onTransfer()       { setActive(btnTransfer); setContent(new TransferPanel(acc).build()); }
    @FXML private void onRecharge()       { setActive(btnRecharge); setContent(new RechargePanel(acc).build()); }
    @FXML private void onLoan()           { setActive(btnLoan); setContent(new LoanPanel(acc).build()); }
    @FXML private void onDps()            { setActive(btnDps); setContent(new RequestHubPanel(acc, Enums.RequestType.DPS).build()); }
    @FXML private void onFdr()            { setActive(btnFdr); setContent(new RequestHubPanel(acc, Enums.RequestType.FDR).build()); }
    @FXML private void onCardCheque()     { setActive(btnCardCheque); setContent(new CardChequePanel(acc).build()); }
    @FXML private void onLocker()         { setActive(btnLocker); setContent(new RequestHubPanel(acc, Enums.RequestType.LOCKER).build()); }
    @FXML private void onDollar()         { setActive(btnDollar); setContent(new DollarPanel(acc).build()); }
    @FXML private void onToll()           { setActive(btnToll); setContent(new BillPanel(acc, Enums.TxnCategory.TOLL_SERVICE, "Toll Service").build()); }
    @FXML private void onUtility()        { setActive(btnUtility); setContent(new UtilityBillsPanel(acc).build()); }
    @FXML private void onLiveSupport()    { setActive(btnSupport); setContent(new LiveSupportPanel(acc.getAccountNumber(), false).build()); }

    @FXML
    private void onBell() {
        for (Button b : navButtons) b.getStyleClass().setAll("sidebar-btn");
        setContent(new NotificationPanel(acc.getAccountNumber()).build());
    }

    @FXML
    private void onLogout() {
        if (AlertUtil.confirm("Logout", "Are you sure you want to log out?")) {
            SessionManager.get().logout();
            LoginController.showLoginScreen(com.bdbank.App.primaryStage);
        }
    }

    public static void show(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(UserDashboardController.class.getResource("/com/bdbank/fxml/UserDashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1100, 700);
            scene.getStylesheets().add(UserDashboardController.class.getResource("/com/bdbank/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("BD Bank - Customer Dashboard");
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load UserDashboard.fxml", e);
        }
    }
}
