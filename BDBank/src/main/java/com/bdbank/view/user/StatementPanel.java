package com.bdbank.view.user;

import com.bdbank.model.Account;
import com.bdbank.model.Transaction;
import com.bdbank.service.AccountService;
import com.bdbank.service.FileManager;
import com.bdbank.util.AlertUtil;
import com.bdbank.util.JsonUtil;
import com.bdbank.util.Util;
import com.bdbank.view.Theme;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StatementPanel {
    private final Account acc;
    public StatementPanel(Account acc) { this.acc = acc; }

    public VBox build() {
        VBox root = new VBox(16);
        root.getChildren().add(Theme.h1("Monthly Statement"));

        ComboBox<Integer> yearBox = new ComboBox<>(FXCollections.observableArrayList(
                IntStream.rangeClosed(LocalDate.now().getYear() - 5, LocalDate.now().getYear()).boxed().collect(Collectors.toList())));
        yearBox.setValue(LocalDate.now().getYear());
        ComboBox<Integer> monthBox = new ComboBox<>(FXCollections.observableArrayList(IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList())));
        monthBox.setValue(LocalDate.now().getMonthValue());
        Button generateBtn = Theme.primaryButton("Generate");
        Button exportBtn = Theme.secondaryButton("Export as File");
        Button exportJsonBtn = Theme.secondaryButton("Export as JSON");

        TableView<Transaction> table = buildTable();

        HBox controls = new HBox(10, new Label("Year:"), yearBox, new Label("Month:"), monthBox, generateBtn, exportBtn, exportJsonBtn);
        controls.setStyle("-fx-alignment: center-left;");

        Runnable refresh = () -> {
            List<Transaction> list = AccountService.get().monthlyStatement(acc.getAccountNumber(), yearBox.getValue(), monthBox.getValue());
            table.setItems(FXCollections.observableArrayList(list));
        };
        generateBtn.setOnAction(e -> refresh.run());
        exportBtn.setOnAction(e -> {
            List<Transaction> list = table.getItems();
            StringBuilder sb = new StringBuilder();
            sb.append("BD BANK - MONTHLY STATEMENT\n");
            sb.append("Account: ").append(acc.getAccountNumber()).append(" - ").append(acc.getAccountHolderName()).append("\n");
            sb.append("Period: ").append(YearMonth.of(yearBox.getValue(), monthBox.getValue())).append("\n");
            sb.append("Generated: ").append(Util.now()).append("\n");
            sb.append("----------------------------------------------------\n");
            for (Transaction t : list) {
                sb.append(Util.format(t.getTimestamp())).append(" | ").append(t.getType()).append(" | ")
                        .append(t.getCategory()).append(" | Tk ").append(t.getAmount())
                        .append(" | Charge Tk ").append(t.getCharge()).append(" | Bal Tk ").append(t.getBalanceAfter())
                        .append(" | ").append(t.getDescription()).append("\n");
            }
            var path = FileManager.get().writeReport("statement_" + acc.getAccountNumber(), sb.toString());
            AlertUtil.info("Statement Exported", "Saved to: " + (path != null ? path.toAbsolutePath() : "unknown"));
        });
        exportJsonBtn.setOnAction(e -> {
            // Builds the same statement as a JSON document with Jackson - the "write" side of
            // JSON handling, complementing the JSON parsing used for requests/config/the rate API.
            List<Transaction> list = table.getItems();
            ObjectNode jsonRoot = JsonUtil.MAPPER.createObjectNode();
            jsonRoot.put("accountNumber", acc.getAccountNumber());
            jsonRoot.put("accountHolder", acc.getAccountHolderName());
            jsonRoot.put("period", YearMonth.of(yearBox.getValue(), monthBox.getValue()).toString());
            jsonRoot.put("generatedAt", Util.now());
            ArrayNode txns = JsonUtil.MAPPER.createArrayNode();
            for (Transaction t : list) {
                ObjectNode row = JsonUtil.MAPPER.createObjectNode();
                row.put("timestamp", t.getTimestamp().toString());
                row.put("type", t.getType().name());
                row.put("category", t.getCategory().name());
                row.put("amount", t.getAmount());
                row.put("charge", t.getCharge());
                row.put("balanceAfter", t.getBalanceAfter());
                row.put("description", t.getDescription());
                txns.add(row);
            }
            jsonRoot.set("transactions", txns);
            var path = FileManager.get().writeJsonReport("statement_" + acc.getAccountNumber(), jsonRoot);
            AlertUtil.info("Statement Exported as JSON", "Saved to: " + (path != null ? path.toAbsolutePath() : "unknown"));
        });

        refresh.run();
        VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);
        root.getChildren().addAll(controls, table);
        return root;
    }

    private TableView<Transaction> buildTable() {
        TableView<Transaction> table = new TableView<>();
        TableColumn<Transaction, String> date = new TableColumn<>("Date/Time");
        date.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(Util.format(c.getValue().getTimestamp())));
        TableColumn<Transaction, String> type = new TableColumn<>("Type");
        type.setCellValueFactory(new PropertyValueFactory<>("type"));
        TableColumn<Transaction, String> cat = new TableColumn<>("Category");
        cat.setCellValueFactory(new PropertyValueFactory<>("category"));
        TableColumn<Transaction, Double> amt = new TableColumn<>("Amount (Tk)");
        amt.setCellValueFactory(new PropertyValueFactory<>("amount"));
        TableColumn<Transaction, Double> charge = new TableColumn<>("Charge (Tk)");
        charge.setCellValueFactory(new PropertyValueFactory<>("charge"));
        TableColumn<Transaction, Double> bal = new TableColumn<>("Balance After");
        bal.setCellValueFactory(new PropertyValueFactory<>("balanceAfter"));
        TableColumn<Transaction, String> desc = new TableColumn<>("Description");
        desc.setCellValueFactory(new PropertyValueFactory<>("description"));
        desc.setPrefWidth(250);
        table.getColumns().addAll(List.of(date, type, cat, amt, charge, bal, desc));
        return table;
    }
}
