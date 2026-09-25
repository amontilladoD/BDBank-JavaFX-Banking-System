package com.bdbank.service;

import com.bdbank.exception.BankException;
import com.bdbank.model.*;
import com.bdbank.util.Util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Accounts and admins live in the SQLite `accounts` / `admins` tables (see Db.java for schema).
 * Just like before, everything is also kept in an in-memory CopyOnWriteArrayList cache so the
 * rest of the app can keep holding a live, mutable Account reference (e.g. the logged-in user's
 * session account) that updates instantly across every screen - persistAccounts() is what pushes
 * that in-memory state back down into SQLite.
 */
public class AuthService {
    private static final AuthService INSTANCE = new AuthService();
    public static AuthService get() { return INSTANCE; }

    private final List<Account> accounts;
    private final List<AdminUser> admins;

    private AuthService() {
        accounts = new CopyOnWriteArrayList<>(Db.get().query("SELECT * FROM accounts", this::mapAccount));

        admins = new CopyOnWriteArrayList<>(Db.get().query("SELECT * FROM admins", this::mapAdmin));
        if (admins.isEmpty()) {
            // seed a default admin so the app is usable on first run
            admins.add(new AdminUser("admin", "Bank Administrator", Util.hash("admin123")));
            persistAdmins();
        }
    }

    private Account mapAccount(ResultSet rs) throws SQLException {
        Account acc = new Account(rs.getString("account_number"), rs.getString("holder_name"), rs.getString("nid"),
                rs.getString("phone"), rs.getString("email"), rs.getString("password_hash"),
                Enums.AccountType.valueOf(rs.getString("account_type")), rs.getDouble("balance"),
                Enums.AccountStatus.valueOf(rs.getString("status")));
        acc.hydrateCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
        return acc;
    }

    private AdminUser mapAdmin(ResultSet rs) throws SQLException {
        return new AdminUser(rs.getString("admin_id"), rs.getString("name"), rs.getString("password_hash"));
    }

    public synchronized Account authenticateUser(String accountNumber, String password) throws BankException {
        Account acc = accounts.stream().filter(a -> a.getAccountNumber().equalsIgnoreCase(accountNumber)).findFirst()
                .orElseThrow(() -> new BankException("No account found with number: " + accountNumber));
        if (acc.getStatus() == Enums.AccountStatus.PENDING) {
            throw new BankException("Your account is still pending admin approval.");
        }
        if (acc.getStatus() == Enums.AccountStatus.BLOCKED) {
            throw new BankException("Your account has been blocked. Contact support.");
        }
        if (!Util.matches(password, acc.getPasswordHash())) {
            throw new BankException("Incorrect password.");
        }
        return acc;
    }

    public synchronized AdminUser authenticateAdmin(String adminId, String password) throws BankException {
        AdminUser admin = admins.stream().filter(a -> a.getAdminId().equalsIgnoreCase(adminId)).findFirst()
                .orElseThrow(() -> new BankException("No admin found with id: " + adminId));
        if (!Util.matches(password, admin.getPasswordHash())) {
            throw new BankException("Incorrect password.");
        }
        return admin;
    }

    /** Self-registration by a customer results in a PENDING account until admin approves account opening. */
    public synchronized Account registerPending(String name, String nid, String phone, String email,
                                                 String password, Enums.AccountType type) throws BankException {
        if (Util.isBlank(name) || Util.isBlank(nid)) throw new BankException("Name and NID are required.");
        if (!Util.isValidPhone(phone)) throw new BankException("Phone number must be 11 digits starting with 01.");
        if (!Util.isValidEmail(email)) throw new BankException("Please enter a valid email address.");
        if (password == null || password.length() < 6) throw new BankException("Password must be at least 6 characters.");
        boolean phoneExists = accounts.stream().anyMatch(a -> a.getPhone().equals(phone));
        if (phoneExists) throw new BankException("An account already exists with this phone number.");

        String accNo = Util.nextAccountNumber();
        Account acc = new Account(accNo, name, nid, phone, email, Util.hash(password), type, 0.0, Enums.AccountStatus.PENDING);
        accounts.add(acc);
        persistAccounts();
        return acc;
    }

    public List<Account> allAccounts() { return accounts; }

    /** Upserts every in-memory account into SQLite. Called after any balance/status change. */
    public synchronized void persistAccounts() {
        for (Account a : accounts) {
            Db.get().update("INSERT OR REPLACE INTO accounts " +
                    "(account_number, holder_name, nid, phone, email, password_hash, account_type, balance, status, created_at) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?)",
                    a.getAccountNumber(), a.getAccountHolderName(), a.getNid(), a.getPhone(), a.getEmail(),
                    a.getPasswordHash(), a.getAccountType().name(), a.getBalance(), a.getStatus().name(),
                    a.getCreatedAt().toString());
        }
    }

    public synchronized void persistAdmins() {
        for (AdminUser a : admins) {
            Db.get().update("INSERT OR REPLACE INTO admins (admin_id, name, password_hash) VALUES (?,?,?)",
                    a.getAdminId(), a.getName(), a.getPasswordHash());
        }
    }

    public void addAccount(Account acc) { accounts.add(acc); persistAccounts(); }
}
