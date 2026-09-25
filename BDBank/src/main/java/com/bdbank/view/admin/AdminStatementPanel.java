package com.bdbank.view.admin;

import com.bdbank.model.Enums;
import com.bdbank.model.Transaction;
import com.bdbank.service.AccountService;
import com.bdbank.service.FileManager;
import com.bdbank.util.AlertUtil;
import com.bdbank.util.Util;
import com.bdbank.view.Theme;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AdminStatementPanel {
    public VBox build() {
        VBox root = new VBox(16);
        root.getChildren().add(Theme.h1("Bank Statement"));

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(new Tab("Daily", buildDaily()), new Tab("Monthly", buildMonthly()));
        root.getChildren().add(tabs);
        return root;
    }

    private VBox buildDaily() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(16));
        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextArea output = new TextArea();
        output.setEditable(false);
        output.setPrefRowCount(18);
        Button generate = Theme.primaryButton("Generate Daily Report");
        Button export = Theme.secondaryButton("Export to File");
        generate.setOnAction(e -> output.setText(buildDailyReport(datePicker.getValue())));
        export.setOnAction(e -> {
            var p = FileManager.get().writeReport("daily_statement_" + datePicker.getValue(), output.getText());
            AlertUtil.info("Exported", "Saved to: " + (p != null ? p.toAbsolutePath() : "unknown"));
        });
        output.setText(buildDailyReport(LocalDate.now()));
        box.getChildren().addAll(new HBox(10, datePicker, generate, export), output);
        return box;
    }

    private VBox buildMonthly() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(16));
        ComboBox<Integer> yearBox = new ComboBox<>(); yearBox.getItems().addAll(2023, 2024, 2025, 2026); yearBox.setValue(LocalDate.now().getYear());
        ComboBox<Integer> monthBox = new ComboBox<>();
        for (int i = 1; i <= 12; i++) monthBox.getItems().add(i);
        monthBox.setValue(LocalDate.now().getMonthValue());
        TextArea output = new TextArea();
        output.setEditable(false);
        output.setPrefRowCount(18);
        Button generate = Theme.primaryButton("Generate Monthly Report");
        Button export = Theme.secondaryButton("Export to File");
        generate.setOnAction(e -> output.setText(buildMonthlyReport(yearBox.getValue(), monthBox.getValue())));
        export.setOnAction(e -> {
            var p = FileManager.get().writeReport("monthly_statement_" + yearBox.getValue() + "_" + monthBox.getValue(), output.getText());
            AlertUtil.info("Exported", "Saved to: " + (p != null ? p.toAbsolutePath() : "unknown"));
        });
        output.setText(buildMonthlyReport(yearBox.getValue(), monthBox.getValue()));
        box.getChildren().addAll(new HBox(10, new Label("Year:"), yearBox, new Label("Month:"), monthBox, generate, export), output);
        return box;
    }

    private String buildDailyReport(LocalDate date) {
        List<Transaction> txns = AccountService.get().transactionsOn(date);
        return buildReport("DAILY REPORT for " + date, txns);
    }

    private String buildMonthlyReport(int year, int month) {
        List<Transaction> txns = AccountService.get().allTransactions().stream()
                .filter(t -> t.getTimestamp().getYear() == year && t.getTimestamp().getMonthValue() == month).toList();
        return buildReport("MONTHLY REPORT for " + YearMonth.of(year, month), txns);
    }

    private String buildReport(String heading, List<Transaction> txns) {
        StringBuilder sb = new StringBuilder();
        sb.append("BD BANK - ").append(heading).append("\n");
        sb.append("Generated: ").append(Util.now()).append("\n");
        sb.append("Total transactions: ").append(txns.size()).append("\n");
        double totalCredit = txns.stream().filter(t -> t.getType() == Enums.TxnType.CREDIT).mapToDouble(Transaction::getAmount).sum();
        double totalDebit = txns.stream().filter(t -> t.getType() == Enums.TxnType.DEBIT).mapToDouble(Transaction::getAmount).sum();
        double totalCharges = txns.stream().mapToDouble(Transaction::getCharge).sum();
        sb.append(String.format("Total Credit: Tk %.2f | Total Debit: Tk %.2f | Total Charges Collected: Tk %.2f%n", totalCredit, totalDebit, totalCharges));
        sb.append("---------------------------------------------------------------\n");

        Map<Enums.TxnCategory, Long> byCategory = new TreeMap<>();
        for (Transaction t : txns) byCategory.merge(t.getCategory(), 1L, Long::sum);
        sb.append("By category:\n");
        byCategory.forEach((cat, count) -> sb.append("  ").append(cat).append(": ").append(count).append("\n"));
        sb.append("---------------------------------------------------------------\n");
        for (Transaction t : txns) {
            sb.append(Util.format(t.getTimestamp())).append(" | ").append(t.getAccountNumber()).append(" | ")
                    .append(t.getType()).append(" | ").append(t.getCategory()).append(" | Tk ").append(t.getAmount()).append("\n");
        }
        return sb.toString();
    }
}
