package com.bdbank.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class ServiceRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String accountNumber;
    private final Enums.RequestType requestType;
    private Enums.RequestStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime processedAt;
    private String remarks;
    private final Map<String, String> fields = new LinkedHashMap<>();

    protected ServiceRequest(String id, String accountNumber, Enums.RequestType requestType) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.requestType = requestType;
        this.status = Enums.RequestStatus.PENDING;
        this.submittedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public Enums.RequestType getRequestType() { return requestType; }
    public Enums.RequestStatus getStatus() { return status; }
    public void setStatus(Enums.RequestStatus s) { this.status = s; this.processedAt = LocalDateTime.now(); }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String r) { this.remarks = r; }

    public void hydrate(LocalDateTime submittedAt, LocalDateTime processedAt, Enums.RequestStatus status, String remarks) {
        this.submittedAt = submittedAt;
        this.processedAt = processedAt;
        this.status = status;
        this.remarks = remarks;
    }

    public void put(String key, String value) { fields.put(key, value); }
    public String get(String key) { return fields.getOrDefault(key, ""); }
    public double getDouble(String key) { try { return Double.parseDouble(get(key)); } catch (Exception e) { return 0.0; } }
    public int getInt(String key) { try { return Integer.parseInt(get(key)); } catch (Exception e) { return 0; } }
    public Map<String, String> getFields() { return fields; }

    public abstract String summary();
}
