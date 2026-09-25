package com.bdbank.service;

import com.bdbank.model.BankConfig;
import com.bdbank.model.Enums;
import com.bdbank.util.ExecutorServiceManager;
import com.bdbank.util.JsonUtil;
import com.bdbank.util.Util;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.beans.property.SimpleObjectProperty;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Owns the bank-wide config (single row in SQLite's `bank_config` table, id fixed at 1) AND the
 * background thread that refreshes the USD rate periodically - a clean, visible demonstration of
 * a ScheduledExecutorService running independently of the UI thread. The rate itself now comes
 * from ExchangeRateApiClient, which calls a real JSON API (with an offline fallback), tying this
 * feature directly to the JSON-parsing / API-handling lab topic.
 */
public class ConfigService {
    private static final ConfigService INSTANCE = new ConfigService();
    public static ConfigService get() { return INSTANCE; }

    private final BankConfig config;
    private final SimpleObjectProperty<BankConfig> configProperty;
    private volatile boolean tickerStarted = false;

    private ConfigService() {
        config = loadOrSeed();
        configProperty = new SimpleObjectProperty<>(config);
    }

    private BankConfig loadOrSeed() {
        var rows = Db.get().query("SELECT * FROM bank_config WHERE id = 1", this::mapConfig);
        if (!rows.isEmpty()) return rows.get(0);
        BankConfig fresh = new BankConfig();
        insertConfig(fresh);
        return fresh;
    }

    private BankConfig mapConfig(ResultSet rs) throws SQLException {
        BankConfig c = new BankConfig();
        c.totalBDT = rs.getDouble("total_bdt");
        c.totalForeignReserveUSD = rs.getDouble("total_forex_usd");
        c.totalGoldAndValuablesBDT = rs.getDouble("total_gold_bdt");
        c.maxLoanCapacity = rs.getDouble("max_loan_capacity");
        c.totalLoanDisbursed = rs.getDouble("total_loan_disbursed");
        c.dollarBuyRate = rs.getDouble("dollar_buy");
        c.dollarSellRate = rs.getDouble("dollar_sell");
        c.dollarRateUpdatedAt = LocalDateTime.parse(rs.getString("dollar_updated_at"));
        c.lockerSmallFee = rs.getDouble("locker_small_fee");
        c.lockerMediumFee = rs.getDouble("locker_medium_fee");
        c.lockerLargeFee = rs.getDouble("locker_large_fee");
        c.lockerAvailableSmall = rs.getInt("locker_small_avail");
        c.lockerAvailableMedium = rs.getInt("locker_medium_avail");
        c.lockerAvailableLarge = rs.getInt("locker_large_avail");

        // Interest rates were stored as JSON ({"NORMAL":2.0,"STUDENT":4.0,...}); parse them back
        // into the per-account-type map with Jackson.
        try {
            JsonNode json = JsonUtil.MAPPER.readTree(rs.getString("interest_rates_json"));
            for (Enums.AccountType type : Enums.AccountType.values()) {
                if (json.has(type.name())) c.interestRates.put(type, json.get(type.name()).asDouble());
            }
        } catch (Exception e) {
            throw new SQLException("Malformed interest_rates_json: " + e.getMessage(), e);
        }
        return c;
    }

    private void insertConfig(BankConfig c) {
        Db.get().update("INSERT OR REPLACE INTO bank_config (id, total_bdt, total_forex_usd, total_gold_bdt, " +
                "max_loan_capacity, total_loan_disbursed, dollar_buy, dollar_sell, dollar_updated_at, " +
                "locker_small_fee, locker_medium_fee, locker_large_fee, locker_small_avail, locker_medium_avail, " +
                "locker_large_avail, interest_rates_json) VALUES (1,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                c.totalBDT, c.totalForeignReserveUSD, c.totalGoldAndValuablesBDT, c.maxLoanCapacity, c.totalLoanDisbursed,
                c.dollarBuyRate, c.dollarSellRate, c.dollarRateUpdatedAt.toString(),
                c.lockerSmallFee, c.lockerMediumFee, c.lockerLargeFee,
                c.lockerAvailableSmall, c.lockerAvailableMedium, c.lockerAvailableLarge,
                interestRatesToJson(c));
    }

    private String interestRatesToJson(BankConfig c) {
        ObjectNode json = JsonUtil.MAPPER.createObjectNode();
        c.interestRates.forEach((type, rate) -> json.put(type.name(), rate));
        return json.toString();
    }

    public BankConfig config() { return config; }
    public SimpleObjectProperty<BankConfig> configProperty() { return configProperty; }

    /** Writes every field of the (possibly just-edited) config back to its single SQLite row. */
    public void save() { insertConfig(config); }

    /** Starts the recurring dollar-rate refresh job exactly once for the whole app lifetime.
     *  Each tick calls ExchangeRateApiClient (a real HTTPS + JSON-parsing round trip) on this
     *  background scheduler thread - never on the JavaFX Application Thread - then persists and
     *  notifies. */
    public synchronized void startDollarRateTicker() {
        if (tickerStarted) return;
        tickerStarted = true;
        ExecutorServiceManager.get().scheduler().scheduleAtFixedRate(() -> {
            try {
                double liveRate = ExchangeRateApiClient.fetchUsdToBdtRate();
                config.dollarBuyRate = Util.round2(liveRate);
                config.dollarSellRate = Util.round2(liveRate + 1.30); // bank's spread/margin
                config.dollarRateUpdatedAt = LocalDateTime.now();
                save();
                NotificationService.get().push("ALL", "USD rate updated: Buy Tk " + config.dollarBuyRate + " / Sell Tk " + config.dollarSellRate);
            } catch (Exception e) {
                System.err.println("[ConfigService] dollar rate tick failed: " + e.getMessage());
            }
        }, 15, 60, TimeUnit.SECONDS);
    }
}
