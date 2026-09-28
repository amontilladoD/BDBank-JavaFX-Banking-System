package com.bdbank.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

public class BankConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    public double totalBDT = 500_000_000;
    public double totalForeignReserveUSD = 2_000_000;
    public double totalGoldAndValuablesBDT = 150_000_000;
    public double maxLoanCapacity = 100_000_000;
    public double totalLoanDisbursed = 0;

    public double dollarBuyRate = 118.50;
    public double dollarSellRate = 119.80;
    public LocalDateTime dollarRateUpdatedAt = LocalDateTime.now();

    public Map<Enums.AccountType, Double> interestRates = new EnumMap<>(Enums.AccountType.class);

    public double lockerSmallFee = 1500;
    public double lockerMediumFee = 2500;
    public double lockerLargeFee = 4000;
    public int lockerAvailableSmall = 10;
    public int lockerAvailableMedium = 8;
    public int lockerAvailableLarge = 5;

    public BankConfig() {
        for (Enums.AccountType t : Enums.AccountType.values()) interestRates.put(t, t.defaultRate);
    }
}
