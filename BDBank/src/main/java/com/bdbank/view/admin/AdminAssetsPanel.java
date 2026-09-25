package com.bdbank.view.admin;

import com.bdbank.model.BankConfig;
import com.bdbank.service.ConfigService;
import com.bdbank.view.Theme;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class AdminAssetsPanel {
    public VBox build() {
        VBox root = new VBox(16);
        root.getChildren().add(Theme.h1("Bank Assets"));

        BankConfig cfg = ConfigService.get().config();
        VBox card = new VBox(14);
        card.setMaxWidth(520);
        Theme.card(card);

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12);

        TextField bdtField = new TextField(String.valueOf(cfg.totalBDT));
        TextField reserveField = new TextField(String.valueOf(cfg.totalForeignReserveUSD));
        TextField goldField = new TextField(String.valueOf(cfg.totalGoldAndValuablesBDT));
        TextField maxLoanField = new TextField(String.valueOf(cfg.maxLoanCapacity));
        Label totalLoanLabel = new Label("Tk " + cfg.totalLoanDisbursed + " (auto-tracked from approved loans)");

        int r = 0;
        grid.addRow(r++, new Label("Total BDT Reserve:"), bdtField);
        grid.addRow(r++, new Label("Total Foreign Currency Reserve (USD):"), reserveField);
        grid.addRow(r++, new Label("Total Gold / Valuables (BDT valuation):"), goldField);
        grid.addRow(r++, new Label("Maximum Loan Capacity (BDT):"), maxLoanField);
        grid.addRow(r++, new Label("Total Loan Disbursed by Bank:"), totalLoanLabel);

        Label status = new Label();
        Button save = Theme.primaryButton("Save Changes");
        save.setOnAction(e -> {
            try {
                cfg.totalBDT = Double.parseDouble(bdtField.getText());
                cfg.totalForeignReserveUSD = Double.parseDouble(reserveField.getText());
                cfg.totalGoldAndValuablesBDT = Double.parseDouble(goldField.getText());
                cfg.maxLoanCapacity = Double.parseDouble(maxLoanField.getText());
                ConfigService.get().save();
                status.setStyle("-fx-text-fill:" + Theme.SUCCESS + ";");
                status.setText("Assets updated successfully.");
            } catch (Exception ex) {
                status.setStyle("-fx-text-fill:" + Theme.DANGER + ";");
                status.setText("Please enter valid numbers.");
            }
        });

        card.getChildren().addAll(grid, save, status);
        root.getChildren().add(card);
        return root;
    }
}
