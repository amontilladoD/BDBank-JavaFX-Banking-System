package com.bdbank.view.user;

import com.bdbank.model.*;
import com.bdbank.model.Schemes.*;
import com.bdbank.service.RequestService;
import com.bdbank.service.SchemeService;
import com.bdbank.util.Util;
import com.bdbank.view.Theme;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;

/** Drives DPS, FDR and Locker requests generically: Application tab, Rates tab, My Requests tab. */
public class RequestHubPanel {
    private final Account acc;
    private final Enums.RequestType type;

    public RequestHubPanel(Account acc, Enums.RequestType type) { this.acc = acc; this.type = type; }

    public VBox build() {
        VBox root = new VBox(16);
        String title = switch (type) { case DPS -> "DPS"; case FDR -> "FDR"; case LOCKER -> "Locker Request"; default -> type.toString(); };
        root.getChildren().add(Theme.h1(title));

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        Tab applyTab = new Tab(type == Enums.RequestType.LOCKER ? "Application" : type + " Application", buildApplyTab());
        Tab ratesTab = new Tab(type == Enums.RequestType.LOCKER ? "Rates" : type + " Rates", buildRatesTab());
        Tab myTab = new Tab(type == Enums.RequestType.LOCKER ? "My Lockers" : "My " + type, buildMyTab());
        tabs.getTabs().addAll(applyTab, ratesTab, myTab);
        root.getChildren().add(tabs);
        return root;
    }

    private VBox buildApplyTab() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(16));
        box.setMaxWidth(460);
        Label status = new Label();
        status.setWrapText(true);

        if (type == Enums.RequestType.DPS) {
            ComboBox<DPSScheme> schemeBox = new ComboBox<>(FXCollections.observableArrayList(SchemeService.get().dpsSchemes()));
            schemeBox.setPromptText("Choose DPS Scheme"); schemeBox.setMaxWidth(Double.MAX_VALUE);
            TextField monthlyField = new TextField(); monthlyField.setPromptText("Monthly Installment (Tk)");
            Button submit = Theme.primaryButton("Apply for DPS"); submit.setMaxWidth(Double.MAX_VALUE);
            submit.setOnAction(e -> {
                try {
                    DPSScheme s = schemeBox.getValue();
                    if (s == null) throw new IllegalArgumentException("Please choose a scheme.");
                    double monthly = Double.parseDouble(monthlyField.getText());
                    if (monthly < s.minMonthlyInstallment) throw new IllegalArgumentException("Minimum monthly installment is Tk " + s.minMonthlyInstallment);
                    var req = new RequestTypes.DPSApplication(Util.nextId("DPS"), acc.getAccountNumber());
                    req.put("scheme", s.name); req.put("monthly", String.valueOf(monthly)); req.put("tenure", String.valueOf(s.tenureMonths));
                    req.put("rate", String.valueOf(s.annualRatePercent));
                    double maturity = monthly * s.tenureMonths * (1 + (s.annualRatePercent / 100.0) * (s.tenureMonths / 12.0) / 2.0);
                    req.put("maturity", String.valueOf(Util.round2(maturity)));
                    RequestService.get().submit(req);
                    status.setStyle("-fx-text-fill:" + Theme.SUCCESS + ";");
                    status.setText("DPS application submitted for admin review.");
                } catch (Exception ex) { status.setStyle("-fx-text-fill:" + Theme.DANGER + ";"); status.setText(ex.getMessage()); }
            });
            box.getChildren().addAll(schemeBox, monthlyField, submit, status);

        } else if (type == Enums.RequestType.FDR) {
            ComboBox<FDRScheme> schemeBox = new ComboBox<>(FXCollections.observableArrayList(SchemeService.get().fdrSchemes()));
            schemeBox.setPromptText("Choose FDR Scheme"); schemeBox.setMaxWidth(Double.MAX_VALUE);
            TextField principalField = new TextField(); principalField.setPromptText("Principal Amount (Tk)");
            Button submit = Theme.primaryButton("Apply for FDR"); submit.setMaxWidth(Double.MAX_VALUE);
            submit.setOnAction(e -> {
                try {
                    FDRScheme s = schemeBox.getValue();
                    if (s == null) throw new IllegalArgumentException("Please choose a scheme.");
                    double principal = Double.parseDouble(principalField.getText());
                    if (principal < s.minAmount) throw new IllegalArgumentException("Minimum FDR amount is Tk " + s.minAmount);
                    if (acc.getBalance() < principal) throw new IllegalArgumentException("Insufficient balance to open this FDR.");
                    var req = new RequestTypes.FDRApplication(Util.nextId("FDR"), acc.getAccountNumber());
                    req.put("scheme", s.name); req.put("principal", String.valueOf(principal)); req.put("tenure", String.valueOf(s.tenureMonths));
                    req.put("rate", String.valueOf(s.annualRatePercent));
                    double maturity = principal * (1 + (s.annualRatePercent / 100.0) * (s.tenureMonths / 12.0));
                    req.put("maturity", String.valueOf(Util.round2(maturity)));
                    RequestService.get().submit(req);
                    status.setStyle("-fx-text-fill:" + Theme.SUCCESS + ";");
                    status.setText("FDR application submitted for admin review.");
                } catch (Exception ex) { status.setStyle("-fx-text-fill:" + Theme.DANGER + ";"); status.setText(ex.getMessage()); }
            });
            box.getChildren().addAll(schemeBox, principalField, submit, status);

        } else if (type == Enums.RequestType.LOCKER) {
            ComboBox<String> sizeBox = new ComboBox<>(FXCollections.observableArrayList("Small", "Medium", "Large"));
            sizeBox.setPromptText("Locker Size"); sizeBox.setMaxWidth(Double.MAX_VALUE);
            Button submit = Theme.primaryButton("Apply for Locker"); submit.setMaxWidth(Double.MAX_VALUE);
            submit.setOnAction(e -> {
                try {
                    String size = sizeBox.getValue();
                    if (size == null) throw new IllegalArgumentException("Please choose a locker size.");
                    var cfg = com.bdbank.service.ConfigService.get().config();
                    double fee = switch (size) { case "Small" -> cfg.lockerSmallFee; case "Medium" -> cfg.lockerMediumFee; default -> cfg.lockerLargeFee; };
                    var req = new RequestTypes.LockerRequest(Util.nextId("LKR"), acc.getAccountNumber());
                    req.put("size", size); req.put("fee", String.valueOf(fee));
                    RequestService.get().submit(req);
                    status.setStyle("-fx-text-fill:" + Theme.SUCCESS + ";");
                    status.setText("Locker request submitted for admin review.");
                } catch (Exception ex) { status.setStyle("-fx-text-fill:" + Theme.DANGER + ";"); status.setText(ex.getMessage()); }
            });
            box.getChildren().addAll(sizeBox, submit, status);
        }
        return box;
    }

    private VBox buildRatesTab() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(16));
        if (type == Enums.RequestType.DPS) {
            for (DPSScheme s : SchemeService.get().dpsSchemes()) box.getChildren().add(rateCard(s.toString()));
        } else if (type == Enums.RequestType.FDR) {
            for (FDRScheme s : SchemeService.get().fdrSchemes()) box.getChildren().add(rateCard(s.toString()));
        } else if (type == Enums.RequestType.LOCKER) {
            var cfg = com.bdbank.service.ConfigService.get().config();
            box.getChildren().add(rateCard("Small locker: Tk " + cfg.lockerSmallFee + "/year - " + cfg.lockerAvailableSmall + " available"));
            box.getChildren().add(rateCard("Medium locker: Tk " + cfg.lockerMediumFee + "/year - " + cfg.lockerAvailableMedium + " available"));
            box.getChildren().add(rateCard("Large locker: Tk " + cfg.lockerLargeFee + "/year - " + cfg.lockerAvailableLarge + " available"));
        }
        return box;
    }

    private VBox buildMyTab() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(16));
        List<ServiceRequest> mine = RequestService.get().forAccount(acc.getAccountNumber(), type);
        if (mine.isEmpty()) box.getChildren().add(Theme.muted("You have no " + type + " records yet."));
        for (ServiceRequest r : mine) {
            VBox card = new VBox(4);
            Theme.card(card);
            Label sum = new Label(r.summary());
            sum.setStyle("-fx-font-weight: bold;");
            Label status = new Label("Status: " + r.getStatus() + (r.getRemarks() != null ? " (" + r.getRemarks() + ")" : ""));
            Label submitted = Theme.muted("Submitted: " + Util.format(r.getSubmittedAt()));
            card.getChildren().addAll(sum, status, submitted);
            if (r.get("maturity") != null && !r.get("maturity").isBlank())
                card.getChildren().add(new Label("Expected maturity amount: Tk " + r.get("maturity")));
            box.getChildren().add(card);
        }
        return box;
    }

    private VBox rateCard(String text) {
        VBox c = new VBox(new Label(text));
        Theme.card(c);
        return c;
    }
}
