package com.bdbank.service;

import com.bdbank.exception.BankException;
import com.bdbank.model.*;
import com.bdbank.util.JsonUtil;
import com.bdbank.util.Util;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class RequestService {
    private static final RequestService INSTANCE = new RequestService();
    public static RequestService get() { return INSTANCE; }

    private final List<ServiceRequest> requests;

    private RequestService() {
        requests = new CopyOnWriteArrayList<>(Db.get().query("SELECT * FROM requests", this::mapRequest));
    }

    private ServiceRequest mapRequest(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String accountNumber = rs.getString("account_number");
        Enums.RequestType type = Enums.RequestType.valueOf(rs.getString("request_type"));

        ServiceRequest req = switch (type) {
            case LOAN -> new RequestTypes.LoanApplication(id, accountNumber);
            case DPS -> new RequestTypes.DPSApplication(id, accountNumber);
            case FDR -> new RequestTypes.FDRApplication(id, accountNumber);
            case CARD -> new RequestTypes.CardRequest(id, accountNumber);
            case CHEQUE -> new RequestTypes.ChequeRequest(id, accountNumber);
            case LOCKER -> new RequestTypes.LockerRequest(id, accountNumber);
            case DOLLAR_ENDORSEMENT -> new RequestTypes.DollarEndorsementRequest(id, accountNumber);
        };

        LocalDateTime submittedAt = LocalDateTime.parse(rs.getString("submitted_at"));
        String processedAtStr = rs.getString("processed_at");
        LocalDateTime processedAt = processedAtStr == null ? null : LocalDateTime.parse(processedAtStr);
        Enums.RequestStatus status = Enums.RequestStatus.valueOf(rs.getString("status"));
        req.hydrate(submittedAt, processedAt, status, rs.getString("remarks"));

        try {
            JsonNode json = JsonUtil.MAPPER.readTree(rs.getString("fields_json"));
            json.fields().forEachRemaining(entry -> req.put(entry.getKey(), entry.getValue().asText()));
        } catch (Exception e) {
            throw new SQLException("Malformed fields_json for request " + id + ": " + e.getMessage(), e);
        }

        return req;
    }

    private String fieldsToJson(ServiceRequest req) {
        ObjectNode json = JsonUtil.MAPPER.createObjectNode();
        for (Map.Entry<String, String> e : req.getFields().entrySet()) json.put(e.getKey(), e.getValue());
        return json.toString();
    }

    public void submit(ServiceRequest request) {
        requests.add(request);
        Db.get().update("INSERT INTO requests (id, account_number, request_type, status, submitted_at, processed_at, remarks, fields_json) " +
                        "VALUES (?,?,?,?,?,?,?,?)",
                request.getId(), request.getAccountNumber(), request.getRequestType().name(), request.getStatus().name(),
                request.getSubmittedAt().toString(), null, request.getRemarks(), fieldsToJson(request));
        NotificationService.get().push(request.getAccountNumber(),
                "Your " + request.getRequestType() + " request has been submitted and is pending review.");
    }

    public List<ServiceRequest> forAccount(String accountNumber, Enums.RequestType type) {
        return requests.stream().filter(r -> r.getAccountNumber().equals(accountNumber) && r.getRequestType() == type)
                .sorted((a, b) -> b.getSubmittedAt().compareTo(a.getSubmittedAt())).collect(Collectors.toList());
    }

    public List<ServiceRequest> pendingOfType(Enums.RequestType type) {
        return requests.stream().filter(r -> r.getRequestType() == type && r.getStatus() == Enums.RequestStatus.PENDING)
                .sorted((a, b) -> a.getSubmittedAt().compareTo(b.getSubmittedAt())).collect(Collectors.toList());
    }

    public List<ServiceRequest> allOfType(Enums.RequestType type) {
        return requests.stream().filter(r -> r.getRequestType() == type)
                .sorted((a, b) -> b.getSubmittedAt().compareTo(a.getSubmittedAt())).collect(Collectors.toList());
    }

    public synchronized void approve(ServiceRequest req, String remarks) throws BankException {
        req.setStatus(Enums.RequestStatus.APPROVED);
        req.setRemarks(remarks);

        Account acc = AuthService.get().allAccounts().stream()
                .filter(a -> a.getAccountNumber().equals(req.getAccountNumber())).findFirst()
                .orElseThrow(() -> new BankException("Linked account not found."));

        switch (req.getRequestType()) {
            case LOAN -> {
                double principal = req.getDouble("principal");
                AccountService.get().creditAccount(acc, Enums.TxnCategory.LOAN_DISBURSEMENT, principal,
                        "Loan disbursed: " + req.get("scheme"));
                BankConfig cfg = ConfigService.get().config();
                cfg.totalLoanDisbursed += principal;
                ConfigService.get().save();
            }
            case DPS -> NotificationService.get().push(acc.getAccountNumber(), "Your DPS application has been approved and is now active.");
            case FDR -> {
                double principal = req.getDouble("principal");
                AccountService.get().debitAccountFor(acc, Enums.TxnCategory.FDR_OPEN, principal, "FDR opened: " + req.get("scheme"));
            }
            case CARD -> {
                double fee = req.getDouble("fee");
                AccountService.get().debitAccountFor(acc, Enums.TxnCategory.CARD_FEE, fee, req.get("cardType") + " card issuance fee");
            }
            case CHEQUE -> {
                double fee = req.getDouble("fee");
                AccountService.get().debitAccountFor(acc, Enums.TxnCategory.CHEQUE_FEE, fee, req.get("pages") + "-page cheque book fee");
            }
            case LOCKER -> {
                double fee = req.getDouble("fee");
                AccountService.get().debitAccountFor(acc, Enums.TxnCategory.LOCKER_FEE, fee, req.get("size") + " locker annual fee");
            }
            case DOLLAR_ENDORSEMENT -> {
                double usd = req.getDouble("amountUsd");
                double rate = ConfigService.get().config().dollarSellRate;
                double bdtCost = Util.round2(usd * rate);
                AccountService.get().debitAccountFor(acc, Enums.TxnCategory.DOLLAR_ENDORSEMENT, bdtCost,
                        "Dollar endorsement $" + usd + " @ " + rate);
            }
        }
        persistStatus(req);
        NotificationService.get().push(req.getAccountNumber(), "Your " + req.getRequestType() + " request was APPROVED. " +
                (remarks == null || remarks.isBlank() ? "" : "Note: " + remarks));
    }

    public synchronized void reject(ServiceRequest req, String remarks) {
        req.setStatus(Enums.RequestStatus.REJECTED);
        req.setRemarks(remarks);
        persistStatus(req);
        NotificationService.get().push(req.getAccountNumber(), "Your " + req.getRequestType() + " request was REJECTED. " +
                (remarks == null || remarks.isBlank() ? "" : "Reason: " + remarks));
    }

    private void persistStatus(ServiceRequest req) {
        Db.get().update("UPDATE requests SET status = ?, processed_at = ?, remarks = ? WHERE id = ?",
                req.getStatus().name(),
                req.getProcessedAt() == null ? null : req.getProcessedAt().toString(),
                req.getRemarks(), req.getId());
    }
}
