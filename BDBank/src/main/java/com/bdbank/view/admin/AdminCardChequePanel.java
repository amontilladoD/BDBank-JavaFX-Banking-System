package com.bdbank.view.admin;

import com.bdbank.exception.BankException;
import com.bdbank.model.Enums;
import com.bdbank.model.ServiceRequest;
import com.bdbank.service.RequestService;
import com.bdbank.util.AlertUtil;
import com.bdbank.util.Util;
import com.bdbank.view.Theme;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AdminCardChequePanel {
    public VBox build() {
        VBox root = new VBox(16);
        root.getChildren().add(Theme.h1("Card / Cheque Book Management"));

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(new Tab("Card Applications", buildTab(Enums.RequestType.CARD)),
                new Tab("Cheque Book Applications", buildTab(Enums.RequestType.CHEQUE)));
        root.getChildren().add(tabs);
        return root;
    }

    private VBox buildTab(Enums.RequestType type) {
        VBox box = new VBox(12);
        box.setPadding(new Insets(16));
        ListView<ServiceRequest> list = new ListView<>();
        Runnable refresh = () -> list.setItems(FXCollections.observableArrayList(RequestService.get().allOfType(type)));
        list.setCellFactory(v -> new ListCell<>() {
            @Override protected void updateItem(ServiceRequest r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? null : r.getAccountNumber() + " | " + r.summary() + " | Status: " + r.getStatus() +
                        " | " + Util.format(r.getSubmittedAt()));
            }
        });
        refresh.run();
        VBox.setVgrow(list, Priority.ALWAYS);
        TextField remarksField = new TextField(); remarksField.setPromptText("Remarks (optional)");
        Button approve = Theme.successButton("Approve");
        Button reject = Theme.dangerButton("Reject");
        approve.setOnAction(e -> {
            ServiceRequest sel = list.getSelectionModel().getSelectedItem();
            if (sel == null) { AlertUtil.error("No selection", "Please select an application."); return; }
            if (sel.getStatus() != Enums.RequestStatus.PENDING) { AlertUtil.error("Already processed", "Not pending."); return; }
            try { RequestService.get().approve(sel, remarksField.getText()); refresh.run(); }
            catch (BankException ex) { AlertUtil.error("Error", ex.getMessage()); }
        });
        reject.setOnAction(e -> {
            ServiceRequest sel = list.getSelectionModel().getSelectedItem();
            if (sel == null) { AlertUtil.error("No selection", "Please select an application."); return; }
            RequestService.get().reject(sel, remarksField.getText());
            refresh.run();
        });
        box.getChildren().addAll(list, remarksField, new HBox(10, approve, reject));
        return box;
    }
}
