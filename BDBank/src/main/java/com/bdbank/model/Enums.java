package com.bdbank.model;

public class Enums {

    /** The 5 account types the bank offers, each with its own annual interest rate (admin adjustable). */
    public enum AccountType {
        NORMAL("Normal", 2.0),
        STUDENT("Student", 4.0),
        WOMAN("Woman", 5.0),
        WORKER("Worker", 3.0),
        SAVINGS_PLUS("Savings+", 4.0);

        public final String label;
        public final double defaultRate;
        AccountType(String label, double defaultRate) { this.label = label; this.defaultRate = defaultRate; }
    }

    public enum AccountStatus { PENDING, ACTIVE, BLOCKED, CLOSED }

    public enum Role { USER, ADMIN }

    public enum TxnType { CREDIT, DEBIT }

    public enum TxnCategory {
        TRANSFER_NPSB, TRANSFER_BEFTN, TRANSFER_CARD, TRANSFER_BKASH, TRANSFER_NAGAD, TRANSFER_ROCKET,
        MOBILE_RECHARGE, UTILITY_ELECTRICITY, UTILITY_WATER, UTILITY_TAX, UTILITY_GOVT_FEE, UTILITY_INSTITUTIONAL,
        TOLL_SERVICE, LOAN_DISBURSEMENT, LOAN_INSTALLMENT, DPS_INSTALLMENT, DPS_ENCASHMENT, FDR_OPEN, FDR_ENCASHMENT,
        CARD_FEE, CHEQUE_FEE, LOCKER_FEE, DOLLAR_ENDORSEMENT, INTEREST_CREDIT, DEPOSIT, OTHER
    }

    public enum RequestType { LOAN, DPS, FDR, CARD, CHEQUE, LOCKER, DOLLAR_ENDORSEMENT }

    public enum RequestStatus { PENDING, APPROVED, REJECTED }

    public enum CardType { VISA, MASTER, AMEX }

    public enum ChequePages { TEN(10), TWENTY(20);
        public final int pages;
        ChequePages(int p) { this.pages = p; }
    }
}
