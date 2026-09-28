package com.bdbank.service;

import com.bdbank.exception.BankException;
import com.bdbank.model.*;
import com.bdbank.util.ExecutorServiceManager;
import com.bdbank.util.Util;

import java.util.concurrent.TimeUnit;

public class AdminBankingService {
    private static final AdminBankingService INSTANCE = new AdminBankingService();
    public static AdminBankingService get() { return INSTANCE; }

    private volatile boolean interestJobStarted = false;

    public synchronized Account openAccount(Enums.AccountType type, String name, String nid, String phone,
                                             String email, String password, double openingDeposit,
                                             String studentId, String gender, String employer) throws BankException {
        if (Util.isBlank(name) || Util.isBlank(nid)) throw new BankException("Name and NID are mandatory.");
        if (!Util.isValidPhone(phone)) throw new BankException("Phone must be an 11-digit '01...' number.");
        if (!Util.isValidEmail(email)) throw new BankException("A valid email is required.");
        if (password == null || password.length() < 6) throw new BankException("Password must be at least 6 characters.");

        switch (type) {
            case STUDENT -> { if (Util.isBlank(studentId)) throw new BankException("Student account requires a valid Student ID."); }
            case WOMAN -> {
                if (Util.isBlank(gender) || !gender.equalsIgnoreCase("Female"))
                    throw new BankException("Woman account can only be opened for a female applicant.");
            }
            case WORKER -> { if (Util.isBlank(employer)) throw new BankException("Worker account requires employer/company name."); }
            case SAVINGS_PLUS -> {
                if (openingDeposit < 10000) throw new BankException("Savings+ requires a minimum opening deposit of Tk 10,000.");
            }
            default -> { /* Normal: no extra criteria */ }
        }

        String accNo = Util.nextAccountNumber();

        Account acc = new Account(accNo, name, nid, phone, email, Util.hash(password), type, 0.0, Enums.AccountStatus.ACTIVE);
        AuthService.get().addAccount(acc);
        if (openingDeposit > 0) {
            AccountService.get().creditAccount(acc, Enums.TxnCategory.DEPOSIT, openingDeposit, "Initial deposit at account opening");
        }
        FileManager.get().appendLog("account_opening_log.txt",
                Util.now() + " | Opened " + type + " account " + accNo + " for " + name + " with deposit Tk " + openingDeposit);
        return acc;
    }

    public synchronized void approveExistingAccount(Account acc) {
        acc.setStatus(Enums.AccountStatus.ACTIVE);
        AuthService.get().persistAccounts();
        NotificationService.get().push(acc.getAccountNumber(), "Your account has been approved and activated. Welcome to BD Bank!");
    }

    public synchronized void depositCash(Account acc, double amount) throws BankException {
        if (amount <= 0) throw new BankException("Deposit amount must be greater than zero.");
        if (acc.getStatus() != Enums.AccountStatus.ACTIVE) throw new BankException("Only active accounts can receive deposits.");
        AccountService.get().creditAccount(acc, Enums.TxnCategory.DEPOSIT, amount, "Cash deposit at branch counter");
        FileManager.get().appendLog("account_opening_log.txt",
                Util.now() + " | Cash deposit of Tk " + amount + " into " + acc.getAccountNumber());
    }

    public synchronized void blockAccount(Account acc) {
        acc.setStatus(Enums.AccountStatus.BLOCKED);
        AuthService.get().persistAccounts();
    }

    public synchronized void startInterestAccrualJob() {
        if (interestJobStarted) return;
        interestJobStarted = true;
        ExecutorServiceManager.get().scheduler().scheduleAtFixedRate(() -> {
            try {
                BankConfig cfg = ConfigService.get().config();
                for (Account acc : AuthService.get().allAccounts()) {
                    if (acc.getStatus() != Enums.AccountStatus.ACTIVE || acc.getBalance() <= 0) continue;
                    double annualRate = cfg.interestRates.getOrDefault(acc.getAccountType(), acc.getAccountType().defaultRate);
                    // accelerated demo accrual: a small slice of the annual rate applied periodically
                    double interest = Util.round2(acc.getBalance() * (annualRate / 100.0) / 24.0);
                    if (interest > 0) {
                        AccountService.get().creditAccount(acc, Enums.TxnCategory.INTEREST_CREDIT, interest,
                                "Interest credited (" + annualRate + "% p.a.)");
                    }
                }
            } catch (Exception e) {
                System.err.println("[AdminBankingService] interest job failed: " + e.getMessage());
            }
        }, 60, 90, TimeUnit.SECONDS);
    }
}
