package com.bdbank.service;

import com.bdbank.exception.BankException;
import com.bdbank.model.*;
import com.bdbank.util.Util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/** Transactions are stored in SQLite's `transactions` table and are append-only: once written,
 *  a transaction is never edited, so persistTxn() below is a single INSERT rather than a rewrite. */
public class AccountService {
    private static final AccountService INSTANCE = new AccountService();
    public static AccountService get() { return INSTANCE; }

    private final List<Transaction> transactions;

    private AccountService() {
        transactions = new CopyOnWriteArrayList<>(
                Db.get().query("SELECT * FROM transactions ORDER BY timestamp DESC", this::mapTransaction));
    }

    private Transaction mapTransaction(ResultSet rs) throws SQLException {
        Transaction t = new Transaction(rs.getString("id"), rs.getString("account_number"),
                Enums.TxnType.valueOf(rs.getString("type")), Enums.TxnCategory.valueOf(rs.getString("category")),
                rs.getDouble("amount"), rs.getDouble("charge"), rs.getDouble("balance_after"), rs.getString("description"));
        t.hydrateTimestamp(LocalDateTime.parse(rs.getString("timestamp")));
        return t;
    }

    public enum TransferChannel { NPSB, BEFTN, CARD, BKASH, NAGAD, ROCKET }

    /** Fee schedule exactly as specified by the bank's rules. */
    public double calculateTransferCharge(TransferChannel channel, double amount) {
        switch (channel) {
            case NPSB:
                if (amount <= 100000) return 10;
                // "above 20tk/lacs" -> 20 Tk per lac (100,000) portion above the base slab
                return Math.ceil(amount / 100000.0) * 20.0;
            case BEFTN: return 0.0;
            case CARD: return Util.round2(amount * 0.015);
            case BKASH:
            case NAGAD:
            case ROCKET:
                return 0.0;
            default: return 0.0;
        }
    }

    public synchronized Transaction transfer(Account from, String toAccountOrNumber, TransferChannel channel, double amount) throws BankException {
        if (amount <= 0) throw new BankException("Transfer amount must be greater than zero.");
        if (toAccountOrNumber != null && toAccountOrNumber.trim().equalsIgnoreCase(from.getAccountNumber())) {
            throw new BankException("You cannot transfer to your own account.");
        }
        double charge = calculateTransferCharge(channel, amount);
        double total = Util.round2(amount + charge);
        from.debit(total);
        AuthService.get().persistAccounts();

        Enums.TxnCategory category = switch (channel) {
            case NPSB -> Enums.TxnCategory.TRANSFER_NPSB;
            case BEFTN -> Enums.TxnCategory.TRANSFER_BEFTN;
            case CARD -> Enums.TxnCategory.TRANSFER_CARD;
            case BKASH -> Enums.TxnCategory.TRANSFER_BKASH;
            case NAGAD -> Enums.TxnCategory.TRANSFER_NAGAD;
            case ROCKET -> Enums.TxnCategory.TRANSFER_ROCKET;
        };
        String note = "Transfer to " + toAccountOrNumber + " via " + channel +
                (channel == TransferChannel.BEFTN ? " (settles next working day)" : " (instant)");
        Transaction t = new Transaction(Util.nextId("TXN"), from.getAccountNumber(), Enums.TxnType.DEBIT, category,
                amount, charge, from.getBalance(), note);
        transactions.add(t);
        persistTxn(t);
        NotificationService.get().push(from.getAccountNumber(),
                "Tk " + amount + " sent via " + channel + " (charge Tk " + charge + "). New balance: Tk " + from.getBalance());

        // This is the actual "deposit" step: if the recipient account number belongs to another
        // BD Bank customer, the money is credited straight into their account here - the same way
        // a real intra-bank transfer would. If the number doesn't match anyone in this bank, it's
        // treated as going out to an external bank/card/MFS network, so there's nothing local to credit.
        Optional<Account> recipient = AuthService.get().allAccounts().stream()
                .filter(a -> a.getAccountNumber().equalsIgnoreCase(toAccountOrNumber == null ? "" : toAccountOrNumber.trim()))
                .findFirst();
        if (recipient.isPresent()) {
            Account to = recipient.get();
            to.credit(amount);
            AuthService.get().persistAccounts();
            Transaction creditTxn = new Transaction(Util.nextId("TXN"), to.getAccountNumber(), Enums.TxnType.CREDIT,
                    category, amount, 0, to.getBalance(), "Received from " + from.getAccountNumber() + " via " + channel);
            transactions.add(creditTxn);
            persistTxn(creditTxn);
            NotificationService.get().push(to.getAccountNumber(), "Tk " + amount + " received from " +
                    from.getAccountNumber() + " via " + channel + ". New balance: Tk " + to.getBalance());
        }
        return t;
    }

    public synchronized Transaction rechargeMobile(Account acc, String operator, String number, double amount) throws BankException {
        if (amount <= 0) throw new BankException("Recharge amount must be greater than zero.");
        acc.debit(amount);
        AuthService.get().persistAccounts();
        Transaction t = new Transaction(Util.nextId("TXN"), acc.getAccountNumber(), Enums.TxnType.DEBIT,
                Enums.TxnCategory.MOBILE_RECHARGE, amount, 0, acc.getBalance(),
                "Mobile recharge " + operator + " " + number);
        transactions.add(t);
        persistTxn(t);
        NotificationService.get().push(acc.getAccountNumber(), "Recharged Tk " + amount + " to " + number + " (" + operator + ").");
        return t;
    }

    public synchronized Transaction payBill(Account acc, Enums.TxnCategory category, String billerRef, double amount) throws BankException {
        if (amount <= 0) throw new BankException("Amount must be greater than zero.");
        acc.debit(amount);
        AuthService.get().persistAccounts();
        Transaction t = new Transaction(Util.nextId("TXN"), acc.getAccountNumber(), Enums.TxnType.DEBIT,
                category, amount, 0, acc.getBalance(), category + " payment, ref: " + billerRef);
        transactions.add(t);
        persistTxn(t);
        NotificationService.get().push(acc.getAccountNumber(), category + " bill of Tk " + amount + " paid successfully.");
        return t;
    }

    public synchronized void creditAccount(Account acc, Enums.TxnCategory category, double amount, String description) {
        acc.credit(amount);
        AuthService.get().persistAccounts();
        Transaction t = new Transaction(Util.nextId("TXN"), acc.getAccountNumber(), Enums.TxnType.CREDIT,
                category, amount, 0, acc.getBalance(), description);
        transactions.add(t);
        persistTxn(t);
        NotificationService.get().push(acc.getAccountNumber(), description + " Tk " + amount + " credited. New balance: Tk " + acc.getBalance());
    }

    public synchronized void debitAccountFor(Account acc, Enums.TxnCategory category, double amount, String description) throws BankException {
        acc.debit(amount);
        AuthService.get().persistAccounts();
        Transaction t = new Transaction(Util.nextId("TXN"), acc.getAccountNumber(), Enums.TxnType.DEBIT,
                category, amount, 0, acc.getBalance(), description);
        transactions.add(t);
        persistTxn(t);
    }

    public List<Transaction> transactionsFor(String accountNumber) {
        return transactions.stream().filter(t -> t.getAccountNumber().equals(accountNumber))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp())).collect(Collectors.toList());
    }

    public List<Transaction> monthlyStatement(String accountNumber, int year, int month) {
        return transactionsFor(accountNumber).stream()
                .filter(t -> t.getTimestamp().getYear() == year && t.getTimestamp().getMonthValue() == month)
                .collect(Collectors.toList());
    }

    public List<Transaction> allTransactions() {
        return transactions.stream().sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp())).collect(Collectors.toList());
    }

    public List<Transaction> transactionsOn(java.time.LocalDate date) {
        return transactions.stream().filter(t -> t.getTimestamp().toLocalDate().equals(date))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp())).collect(Collectors.toList());
    }

    /** Inserts a single transaction row into SQLite - called right after each transactions.add(...). */
    private void persistTxn(Transaction t) {
        Db.get().update("INSERT OR REPLACE INTO transactions " +
                "(id, account_number, type, category, amount, charge, balance_after, description, timestamp) " +
                "VALUES (?,?,?,?,?,?,?,?,?)",
                t.getId(), t.getAccountNumber(), t.getType().name(), t.getCategory().name(),
                t.getAmount(), t.getCharge(), t.getBalanceAfter(), t.getDescription(), t.getTimestamp().toString());
    }
}
