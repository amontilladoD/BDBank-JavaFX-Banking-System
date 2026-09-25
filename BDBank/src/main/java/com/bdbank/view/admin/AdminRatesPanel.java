package com.bdbank.view.admin;

import com.bdbank.model.BankConfig;
import com.bdbank.model.Enums;
import com.bdbank.service.ConfigService;
import com.bdbank.view.Theme;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

public class AdminRatesPanel {
    public VBox build() {
        VBox root = new VBox(16);
        root.getChildren().add(Theme.h1("Annual Interest Rates on Balance"));

        BankConfig cfg = ConfigService.get().config();
        VBox card = new VBox(14);
        card.setMaxWidth(480);
        Theme.card(card);

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12);
        Map<Enums.AccountType, TextField> fields = new HashMap<>();
        int r = 0;
        for (Enums.AccountType type : Enums.AccountType.values()) {
            TextField f = new TextField(String.valueOf(cfg.interestRates.getOrDefault(type, type.defaultRate)));
            fields.put(type, f);
            grid.addRow(r++, new Label(type.label + " (%):"), f);
        }

        Label status = new Label();
        Button save = Theme.primaryButton("Update Rates");
        save.setOnAction(e -> {
            try {
                for (var entry : fields.entrySet()) cfg.interestRates.put(entry.getKey(), Double.parseDouble(entry.getValue().getText()));
                ConfigService.get().save();
                status.setStyle("-fx-text-fill:" + Theme.SUCCESS + ";");
                status.setText("Interest rates updated. New accruals will use these rates.");
            } catch (Exception ex) {
                status.setStyle("-fx-text-fill:" + Theme.DANGER + ";");
                status.setText("Please enter valid percentages.");
            }
        });

        card.getChildren().addAll(grid, save, status);
        root.getChildren().add(card);
        return root;
    }
}
