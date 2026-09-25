package com.bdbank.view.user;

import com.bdbank.model.*;
import com.bdbank.model.Schemes.LoanScheme;
import com.bdbank.service.RequestService;
import com.bdbank.service.SchemeService;
import com.bdbank.util.Util;
import com.bdbank.view.Theme;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;

public class LoanPanel {
    private final Account acc;
    public LoanPanel(Account acc) { this.acc = acc; }

    public VBox build() {
        VBox root = new VBox(16);
        root.getChildren().add(Theme.h1("Loan"));

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(
                new Tab("Eligibility Check", buildEligibilityTab()),
                new Tab("Loan Application", buildApplyTab()),
                new Tab("Loan Rates", buildRatesTab()),
                new Tab("My Loan", buildMyLoanTab())
        );
        root.getChildren().add(tabs);
        return root;
    }

    private VBox buildEligibilityTab() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(16));
        box.setMaxWidth(460);
        ComboBox<LoanScheme> schemeBox = new ComboBox<>(FXCollections.observableArrayList(SchemeService.get().loanSchemes()));
        schemeBox.setPromptText("Choose Loan Scheme"); schemeBox.setMaxWidth(Double.MAX_VALUE);
        TextField amountField = new TextField(); amountField.setPromptText("Desired Loan Amount (Tk)");
        Label result = new Label();
        result.setWrapText(true);
        Button checkBtn = Theme.primaryButton("Check Eligibility"); checkBtn.setMaxWidth(Double.MAX_VALUE);
        checkBtn.setOnAction(e -> {
            try {
                LoanScheme s = schemeBox.getValue();
                if (s == null) throw new IllegalArgumentException("Please choose a scheme.");
                double amount = Double.parseDouble(amountField.getText());
                StringBuilder reasons = new StringBuilder();
                boolean eligible = true;
                if (acc.getBalance() < s.minEligibleBalance) { eligible = false; reasons.append("- Requires minimum balance of Tk ").append(s.minEligibleBalance).append("\n"); }
                if (amount > s.maxAmount) { eligible = false; reasons.append("- Requested amount exceeds max of Tk ").append(s.maxAmount).append("\n"); }
                if (acc.getStatus() != Enums.AccountStatus.ACTIVE) { eligible = false; reasons.append("- Account must be active\n"); }
                if (eligible) {
                    result.setStyle("-fx-text-fill:" + Theme.SUCCESS + ";");
                    result.setText("You are ELIGIBLE for this loan under " + s.name + " at " + s.annualRatePercent + "% p.a.");
                } else {
                    result.setStyle("-fx-text-fill:" + Theme.DANGER + ";");
                    result.setText("NOT eligible:\n" + reasons);
                }
            } catch (Exception ex) { result.setStyle("-fx-text-fill:" + Theme.DANGER + ";"); result.setText(ex.getMessage()); }
        });
        box.getChildren().addAll(schemeBox, amountField, checkBtn, result);
        return box;
    }

    private VBox buildApplyTab() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(16));
        box.setMaxWidth(460);
        ComboBox<LoanScheme> schemeBox = new ComboBox<>(FXCollections.observableArrayList(SchemeService.get().loanSchemes()));
        schemeBox.setPromptText("Choose Loan Scheme"); schemeBox.setMaxWidth(Double.MAX_VALUE);
        TextField principalField = new TextField(); principalField.setPromptText("Loan Amount (Tk)");
        TextField tenureField = new TextField(); tenureField.setPromptText("Tenure (months)");
        Label status = new Label();
        status.setWrapText(true);
        Button submit = Theme.primaryButton("Submit Loan Application"); submit.setMaxWidth(Double.MAX_VALUE);
        submit.setOnAction(e -> {
            try {
                LoanScheme s = schemeBox.getValue();
                if (s == null) throw new IllegalArgumentException("Please choose a scheme.");
                double principal = Double.parseDouble(principalField.getText());
                int tenure = Integer.parseInt(tenureField.getText());
                if (principal > s.maxAmount) throw new IllegalArgumentException("Max loan amount is Tk " + s.maxAmount);
                if (tenure > s.maxTenureMonths) throw new IllegalArgumentException("Max tenure is " + s.maxTenureMonths + " months");
                double monthlyRate = (s.annualRatePercent / 100.0) / 12.0;
                double emi = (principal * monthlyRate * Math.pow(1 + monthlyRate, tenure)) / (Math.pow(1 + monthlyRate, tenure) - 1);
                var req = new RequestTypes.LoanApplication(Util.nextId("LOAN"), acc.getAccountNumber());
                req.put("scheme", s.name); req.put("principal", String.valueOf(principal)); req.put("tenure", String.valueOf(tenure));
                req.put("rate", String.valueOf(s.annualRatePercent)); req.put("emi", String.valueOf(Util.round2(emi)));
                RequestService.get().submit(req);
                status.setStyle("-fx-text-fill:" + Theme.SUCCESS + ";");
                status.setText("Loan application submitted. Estimated EMI: Tk " + Util.round2(emi) + "/month");
            } catch (Exception ex) { status.setStyle("-fx-text-fill:" + Theme.DANGER + ";"); status.setText(ex.getMessage()); }
        });
        box.getChildren().addAll(schemeBox, principalField, tenureField, submit, status);
        return box;
    }

    private VBox buildRatesTab() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(16));
        for (LoanScheme s : SchemeService.get().loanSchemes()) {
            VBox c = new VBox(new Label(s.toString()));
            Theme.card(c);
            box.getChildren().add(c);
        }
        return box;
    }

    private VBox buildMyLoanTab() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(16));
        List<ServiceRequest> mine = RequestService.get().forAccount(acc.getAccountNumber(), Enums.RequestType.LOAN);
        if (mine.isEmpty()) box.getChildren().add(Theme.muted("You have not taken any loans yet."));
        for (ServiceRequest r : mine) {
            VBox card = new VBox(4);
            Theme.card(card);
            Label sum = new Label(r.summary());
            sum.setStyle("-fx-font-weight: bold;");
            card.getChildren().addAll(sum,
                    new Label("Status: " + r.getStatus() + (r.getRemarks() != null ? " (" + r.getRemarks() + ")" : "")),
                    new Label("Interest rate: " + r.get("rate") + "% p.a. | EMI: Tk " + r.get("emi")),
                    Theme.muted("Applied: " + Util.format(r.getSubmittedAt())));
            box.getChildren().add(card);
        }
        return box;
    }
}
