package com.bdbank.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Db {
    private static final Db INSTANCE = new Db();
    public static Db get() { return INSTANCE; }

    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private final ReentrantLock lock = new ReentrantLock();
    private Connection connection;

    private Db() {
        try {
            Path dataDir = Paths.get("data");
            Files.createDirectories(dataDir);
            Path dbFile = dataDir.resolve("bdbank.db");

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.toAbsolutePath());
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA journal_mode=WAL;");
                st.execute("PRAGMA busy_timeout=5000;");
                st.execute("PRAGMA foreign_keys=ON;");
            }
            createSchema();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SQLite database: " + e.getMessage(), e);
        }
    }

    private void createSchema() throws SQLException {
        String[] ddl = {
            "CREATE TABLE IF NOT EXISTS admins (" +
                "admin_id TEXT PRIMARY KEY, name TEXT NOT NULL, password_hash TEXT NOT NULL)",

            "CREATE TABLE IF NOT EXISTS accounts (" +
                "account_number TEXT PRIMARY KEY, holder_name TEXT NOT NULL, nid TEXT, phone TEXT, " +
                "email TEXT, password_hash TEXT NOT NULL, account_type TEXT NOT NULL, balance REAL NOT NULL, " +
                "status TEXT NOT NULL, created_at TEXT NOT NULL)",

            "CREATE TABLE IF NOT EXISTS transactions (" +
                "id TEXT PRIMARY KEY, account_number TEXT NOT NULL, type TEXT NOT NULL, category TEXT NOT NULL, " +
                "amount REAL NOT NULL, charge REAL NOT NULL, balance_after REAL NOT NULL, description TEXT, " +
                "timestamp TEXT NOT NULL, " +
                "FOREIGN KEY(account_number) REFERENCES accounts(account_number))",

            "CREATE TABLE IF NOT EXISTS notifications (" +
                "id TEXT PRIMARY KEY, target_account TEXT NOT NULL, message TEXT NOT NULL, " +
                "timestamp TEXT NOT NULL, is_read INTEGER NOT NULL DEFAULT 0)",


            "CREATE TABLE IF NOT EXISTS requests (" +
                "id TEXT PRIMARY KEY, account_number TEXT NOT NULL, request_type TEXT NOT NULL, " +
                "status TEXT NOT NULL, submitted_at TEXT NOT NULL, processed_at TEXT, remarks TEXT, " +
                "fields_json TEXT NOT NULL DEFAULT '{}')",

            "CREATE TABLE IF NOT EXISTS loan_schemes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, annual_rate REAL NOT NULL, " +
                "max_amount REAL NOT NULL, max_tenure_months INTEGER NOT NULL, min_balance REAL NOT NULL)",

            "CREATE TABLE IF NOT EXISTS dps_schemes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, annual_rate REAL NOT NULL, " +
                "tenure_months INTEGER NOT NULL, min_monthly REAL NOT NULL)",

            "CREATE TABLE IF NOT EXISTS fdr_schemes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, annual_rate REAL NOT NULL, " +
                "tenure_months INTEGER NOT NULL, min_amount REAL NOT NULL)",

            "CREATE TABLE IF NOT EXISTS bank_config (" +
                "id INTEGER PRIMARY KEY CHECK (id = 1), total_bdt REAL NOT NULL, total_forex_usd REAL NOT NULL, " +
                "total_gold_bdt REAL NOT NULL, max_loan_capacity REAL NOT NULL, total_loan_disbursed REAL NOT NULL, " +
                "dollar_buy REAL NOT NULL, dollar_sell REAL NOT NULL, dollar_updated_at TEXT NOT NULL, " +
                "locker_small_fee REAL NOT NULL, locker_medium_fee REAL NOT NULL, locker_large_fee REAL NOT NULL, " +
                "locker_small_avail INTEGER NOT NULL, locker_medium_avail INTEGER NOT NULL, locker_large_avail INTEGER NOT NULL, " +
                "interest_rates_json TEXT NOT NULL)",

            "CREATE TABLE IF NOT EXISTS chat_messages (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, account_number TEXT NOT NULL, sender TEXT NOT NULL, " +
                "text TEXT NOT NULL, timestamp TEXT NOT NULL)"
        };
        try (Statement st = connection.createStatement()) {
            for (String sql : ddl) st.execute(sql);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) {
        lock.lock();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> out = new ArrayList<>();
                while (rs.next()) out.add(mapper.map(rs));
                return out;
            }
        } catch (SQLException e) {
            System.err.println("[Db] query failed: " + sql + " -> " + e.getMessage());
            return new ArrayList<>();
        } finally {
            lock.unlock();
        }
    }

    public int update(String sql, Object... params) {
        lock.lock();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[Db] update failed: " + sql + " -> " + e.getMessage());
            return -1;
        } finally {
            lock.unlock();
        }
    }

    public int insertAndGetId(String sql, Object... params) {
        lock.lock();
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            System.err.println("[Db] insertAndGetId failed: " + sql + " -> " + e.getMessage());
            return -1;
        } finally {
            lock.unlock();
        }
    }

    private void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object p = params[i];
            if (p == null) ps.setNull(i + 1, Types.VARCHAR);
            else if (p instanceof Integer v) ps.setInt(i + 1, v);
            else if (p instanceof Double v) ps.setDouble(i + 1, v);
            else if (p instanceof Long v) ps.setLong(i + 1, v);
            else ps.setString(i + 1, p.toString());
        }
    }
}
