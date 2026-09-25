package com.bdbank.view.admin;

import com.bdbank.model.Transaction;
import com.bdbank.service.AccountService;
import com.bdbank.util.Util;
import com.bdbank.view.Theme;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Collectors;

public class AdminTransactionPanel {
    public VBox build() {
        VBox root = new VBox(16);
        root.getChildren().add(Theme.h1("Transaction Management"));

        TextField searchField = new TextField();
        searchField.setPromptText("Filter by account number...");
        Button refreshBtn = Theme.secondaryButton("Refresh");

        TableView<Transaction> table = new TableView<>();
        TableColumn<Transaction, String> accCol = new TableColumn<>("Account");
        accCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date/Time");
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(Util.format(c.getValue().getTimestamp())));
        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        TableColumn<Transaction, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        TableColumn<Transaction, Double> amtCol = new TableColumn<>("Amount");
        amtCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        TableColumn<Transaction, Double> chargeCol = new TableColumn<>("Charge");
        chargeCol.setCellValueFactory(new PropertyValueFactory<>("charge"));
        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(260);
        table.getColumns().addAll(List.of(accCol, dateCol, typeCol, catCol, amtCol, chargeCol, descCol));

        Runnable refresh = () -> {
            List<Transaction> all = AccountService.get().allTransactions();
            String filter = searchField.getText().trim();
            if (!filter.isEmpty()) all = all.stream().filter(t -> t.getAccountNumber().contains(filter)).collect(Collectors.toList());
            table.setItems(FXCollections.observableArrayList(all));
        };
        refreshBtn.setOnAction(e -> refresh.run());
        searchField.setOnAction(e -> refresh.run());
        refresh.run();

        VBox.setVgrow(table, Priority.ALWAYS);
        root.getChildren().addAll(new HBox(10, searchField, refreshBtn), table);
        return root;
    }
}
