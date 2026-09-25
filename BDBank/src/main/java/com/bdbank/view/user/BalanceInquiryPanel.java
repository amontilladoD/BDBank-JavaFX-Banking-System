package com.bdbank.view.user;

import com.bdbank.model.Account;
import com.bdbank.util.Util;
import com.bdbank.view.Theme;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class BalanceInquiryPanel {
    private final Account acc;
    public BalanceInquiryPanel(Account acc) { this.acc = acc; }

    public VBox build() {
        VBox root = new VBox(16);
        root.getChildren().add(Theme.h1("Balance Inquiry"));

        VBox card = new VBox(16);
        card.setMaxWidth(480);
        Theme.card(card);

        Label balanceLabel = new Label("Tk " + acc.getBalance());
        balanceLabel.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: " + Theme.NAVY + ";");
        Label subtitle = Theme.muted("Available Balance");

        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(10);
        grid.setPadding(new Insets(10, 0, 0, 0));
        addRow(grid, 0, "Account Number", acc.getAccountNumber());
        addRow(grid, 1, "Account Holder", acc.getAccountHolderName());
        addRow(grid, 2, "Account Type", acc.getAccountType().label);
        addRow(grid, 3, "Status", acc.getStatus().toString());
        addRow(grid, 4, "Opened On", Util.format(acc.getCreatedAt()));

        card.getChildren().addAll(subtitle, balanceLabel, grid);
        root.getChildren().add(card);
        return root;
    }

    private void addRow(GridPane grid, int row, String label, String value) {
        Label l = Theme.muted(label + ":");
        Label v = new Label(value);
        v.setStyle("-fx-font-weight: bold;");
        grid.addRow(row, l, v);
    }
}
