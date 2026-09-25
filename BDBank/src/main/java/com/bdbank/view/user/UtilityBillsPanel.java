package com.bdbank.view.user;

import com.bdbank.model.Account;
import com.bdbank.model.Enums;
import com.bdbank.view.Theme;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;

import java.util.LinkedHashMap;
import java.util.Map;

public class UtilityBillsPanel {
    private final Account acc;
    public UtilityBillsPanel(Account acc) { this.acc = acc; }

    public VBox build() {
        VBox root = new VBox(16);
        root.getChildren().add(Theme.h1("Utility Bills"));

        StackPane detailHolder = new StackPane();
        detailHolder.setPadding(new Insets(10, 0, 0, 0));

        Map<String, Enums.TxnCategory> options = new LinkedHashMap<>();
        options.put("Electricity Bill", Enums.TxnCategory.UTILITY_ELECTRICITY);
        options.put("Water Bill", Enums.TxnCategory.UTILITY_WATER);
        options.put("Tax Bill", Enums.TxnCategory.UTILITY_TAX);
        options.put("Govt. Fees", Enums.TxnCategory.UTILITY_GOVT_FEE);
        options.put("Institutional Fees", Enums.TxnCategory.UTILITY_INSTITUTIONAL);

        FlowPane buttons = new FlowPane(10, 10);
        for (Map.Entry<String, Enums.TxnCategory> entry : options.entrySet()) {
            Button b = Theme.secondaryButton(entry.getKey());
            b.setOnAction(e -> detailHolder.getChildren().setAll(new BillPanel(acc, entry.getValue(), entry.getKey()).build()));
            buttons.getChildren().add(b);
        }

        root.getChildren().addAll(buttons, detailHolder);
        return root;
    }
}
