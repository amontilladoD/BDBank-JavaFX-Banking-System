package com.bdbank.model;

import java.io.Serializable;

public class Schemes {

    public static class LoanScheme implements Serializable {
        private static final long serialVersionUID = 1L;
        public int id = -1;
        public String name;
        public double annualRatePercent;
        public double maxAmount;
        public int maxTenureMonths;
        public double minEligibleBalance;

        public LoanScheme(String name, double annualRatePercent, double maxAmount, int maxTenureMonths, double minEligibleBalance) {
            this.name = name; this.annualRatePercent = annualRatePercent; this.maxAmount = maxAmount;
            this.maxTenureMonths = maxTenureMonths; this.minEligibleBalance = minEligibleBalance;
        }
        @Override public String toString() {
            return name + " | " + annualRatePercent + "% p.a. | up to " + maxAmount + " Tk | up to " + maxTenureMonths + " months";
        }
    }

    public static class DPSScheme implements Serializable {
        private static final long serialVersionUID = 1L;
        public int id = -1;
        public String name;
        public double annualRatePercent;
        public int tenureMonths;
        public double minMonthlyInstallment;

        public DPSScheme(String name, double annualRatePercent, int tenureMonths, double minMonthlyInstallment) {
            this.name = name; this.annualRatePercent = annualRatePercent; this.tenureMonths = tenureMonths;
            this.minMonthlyInstallment = minMonthlyInstallment;
        }
        @Override public String toString() {
            return name + " | " + annualRatePercent + "% p.a. | " + tenureMonths + " months | min " + minMonthlyInstallment + " Tk/month";
        }
    }

    public static class FDRScheme implements Serializable {
        private static final long serialVersionUID = 1L;
        public int id = -1;
        public String name;
        public double annualRatePercent;
        public int tenureMonths;
        public double minAmount;

        public FDRScheme(String name, double annualRatePercent, int tenureMonths, double minAmount) {
            this.name = name; this.annualRatePercent = annualRatePercent; this.tenureMonths = tenureMonths;
            this.minAmount = minAmount;
        }
        @Override public String toString() {
            return name + " | " + annualRatePercent + "% p.a. | " + tenureMonths + " months | min " + minAmount + " Tk";
        }
    }
}
