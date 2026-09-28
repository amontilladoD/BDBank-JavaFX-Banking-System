package com.bdbank.model;

public class RequestTypes {

    public static class LoanApplication extends ServiceRequest {
        public LoanApplication(String id, String accountNumber) { super(id, accountNumber, Enums.RequestType.LOAN); }
        @Override public String summary() {
            return "Loan: " + get("scheme") + " - " + get("principal") + " Tk / " + get("tenure") + " months";
        }
    }

    public static class DPSApplication extends ServiceRequest {
        public DPSApplication(String id, String accountNumber) { super(id, accountNumber, Enums.RequestType.DPS); }
        @Override public String summary() {
            return "DPS: " + get("scheme") + " - " + get("monthly") + " Tk/month for " + get("tenure") + " months";
        }
    }

    public static class FDRApplication extends ServiceRequest {
        public FDRApplication(String id, String accountNumber) { super(id, accountNumber, Enums.RequestType.FDR); }
        @Override public String summary() {
            return "FDR: " + get("scheme") + " - " + get("principal") + " Tk for " + get("tenure") + " months";
        }
    }

    public static class CardRequest extends ServiceRequest {
        public CardRequest(String id, String accountNumber) { super(id, accountNumber, Enums.RequestType.CARD); }
        @Override public String summary() { return "Card: " + get("cardType") + " (fee " + get("fee") + " Tk)"; }
    }

    public static class ChequeRequest extends ServiceRequest {
        public ChequeRequest(String id, String accountNumber) { super(id, accountNumber, Enums.RequestType.CHEQUE); }
        @Override public String summary() { return "Cheque book: " + get("pages") + " pages (fee " + get("fee") + " Tk)"; }
    }

    public static class LockerRequest extends ServiceRequest {
        public LockerRequest(String id, String accountNumber) { super(id, accountNumber, Enums.RequestType.LOCKER); }
        @Override public String summary() { return "Locker: " + get("size") + " (fee " + get("fee") + " Tk/yr)"; }
    }

    public static class DollarEndorsementRequest extends ServiceRequest {
        public DollarEndorsementRequest(String id, String accountNumber) { super(id, accountNumber, Enums.RequestType.DOLLAR_ENDORSEMENT); }
        @Override public String summary() { return "Dollar Endorsement: $" + get("amountUsd") + " (" + get("purpose") + ")"; }
    }
}
