package com.bdbank.view.user;

import com.bdbank.model.*;
import com.bdbank.service.RequestService;
import com.bdbank.util.Util;
import com.bdbank.view.Theme;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class CardChequePanel {
    private final Account acc;
    public CardChequePanel(Account acc) { this.acc = acc; }

    private static final double VISA_FEE = 500, MASTER_FEE = 500, AMEX_FEE = 1200;
    private static final double CHEQUE_10_FEE = 100, CHEQUE_20_FEE = 180;

    public VBox build() {
        VBox root = new VBox(16);
        root.getChildren().add(Theme.h1("Card / Cheque Book Request"));

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(new Tab("Card Management", buildCardTab()), new Tab("Cheque Book Management", buildChequeTab()),
                new Tab("My Requests", buildMyRequestsTab()));
        root.getChildren().add(tabs);
        return root;
    }

    private VBox buildCardTab() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(16));
        box.setMaxWidth(420);
        ComboBox<String> cardBox = new ComboBox<>(FXCollections.observableArrayList("Visa (Tk 500)", "MasterCard (Tk 500)", "American Express (Tk 1200)"));
        cardBox.setPromptText("Choose Card"); cardBox.setMaxWidth(Double.MAX_VALUE);
        Label status = new Label(); status.setWrapText(true);
        Button submit = Theme.primaryButton("Request Card"); submit.setMaxWidth(Double.MAX_VALUE);
        submit.setOnAction(e -> {
            try {
                String choice = cardBox.getValue();
                if (choice == null) throw new IllegalArgumentException("Please choose a card type.");
                String cardType = choice.startsWith("Visa") ? "VISA" : choice.startsWith("Master") ? "MASTER" : "AMEX";
                double fee = cardType.equals("VISA") ? VISA_FEE : cardType.equals("MASTER") ? MASTER_FEE : AMEX_FEE;
                var req = new RequestTypes.CardRequest(Util.nextId("CARD"), acc.getAccountNumber());
                req.put("cardType", cardType); req.put("fee", String.valueOf(fee));
                RequestService.get().submit(req);
                status.setStyle("-fx-text-fill:" + Theme.SUCCESS + ";");
                status.setText("Card request submitted for admin approval.");
            } catch (Exception ex) { status.setStyle("-fx-text-fill:" + Theme.DANGER + ";"); status.setText(ex.getMessage()); }
        });
        box.getChildren().addAll(cardBox, submit, status);
        return box;
    }

    private VBox buildChequeTab() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(16));
        box.setMaxWidth(420);
        ComboBox<String> pagesBox = new ComboBox<>(FXCollections.observableArrayList("10 pages (Tk 100)", "20 pages (Tk 180)"));
        pagesBox.setPromptText("Choose Cheque Book"); pagesBox.setMaxWidth(Double.MAX_VALUE);
        Label status = new Label(); status.setWrapText(true);
        Button submit = Theme.primaryButton("Request Cheque Book"); submit.setMaxWidth(Double.MAX_VALUE);
        submit.setOnAction(e -> {
            try {
                String choice = pagesBox.getValue();
                if (choice == null) throw new IllegalArgumentException("Please choose a cheque book size.");
                boolean is10 = choice.startsWith("10");
                var req = new RequestTypes.ChequeRequest(Util.nextId("CHQ"), acc.getAccountNumber());
                req.put("pages", is10 ? "10" : "20"); req.put("fee", String.valueOf(is10 ? CHEQUE_10_FEE : CHEQUE_20_FEE));
                RequestService.get().submit(req);
                status.setStyle("-fx-text-fill:" + Theme.SUCCESS + ";");
                status.setText("Cheque book request submitted for admin approval.");
            } catch (Exception ex) { status.setStyle("-fx-text-fill:" + Theme.DANGER + ";"); status.setText(ex.getMessage()); }
        });
        box.getChildren().addAll(pagesBox, submit, status);
        return box;
    }

    private VBox buildMyRequestsTab() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(16));
        var cardReqs = RequestService.get().forAccount(acc.getAccountNumber(), Enums.RequestType.CARD);
        var chequeReqs = RequestService.get().forAccount(acc.getAccountNumber(), Enums.RequestType.CHEQUE);
        if (cardReqs.isEmpty() && chequeReqs.isEmpty()) box.getChildren().add(Theme.muted("No card/cheque requests yet."));
        for (var r : cardReqs) box.getChildren().add(reqCard(r));
        for (var r : chequeReqs) box.getChildren().add(reqCard(r));
        return box;
    }

    private VBox reqCard(ServiceRequest r) {
        VBox c = new VBox(4);
        Theme.card(c);
        c.getChildren().addAll(new Label(r.summary()), new Label("Status: " + r.getStatus()));
        return c;
    }
}
