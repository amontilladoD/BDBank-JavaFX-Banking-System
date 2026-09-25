package com.bdbank.service;

import com.bdbank.model.Schemes.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Loan/DPS/FDR product schemes, each backed by its own SQLite table with a real AUTOINCREMENT
 *  primary key (id), so add/remove map directly onto INSERT/DELETE instead of rewriting a whole file. */
public class SchemeService {
    private static final SchemeService INSTANCE = new SchemeService();
    public static SchemeService get() { return INSTANCE; }

    private final List<LoanScheme> loanSchemes;
    private final List<DPSScheme> dpsSchemes;
    private final List<FDRScheme> fdrSchemes;

    private SchemeService() {
        loanSchemes = new CopyOnWriteArrayList<>(Db.get().query("SELECT * FROM loan_schemes", this::mapLoan));
        if (loanSchemes.isEmpty()) {
            addLoanScheme(new LoanScheme("Personal Loan", 9.5, 500000, 36, 5000));
            addLoanScheme(new LoanScheme("Home Loan", 7.5, 5000000, 240, 20000));
            addLoanScheme(new LoanScheme("Car Loan", 8.5, 2000000, 60, 15000));
        }
        dpsSchemes = new CopyOnWriteArrayList<>(Db.get().query("SELECT * FROM dps_schemes", this::mapDps));
        if (dpsSchemes.isEmpty()) {
            addDpsScheme(new DPSScheme("DPS 3 Years", 6.5, 36, 500));
            addDpsScheme(new DPSScheme("DPS 5 Years", 7.0, 60, 500));
        }
        fdrSchemes = new CopyOnWriteArrayList<>(Db.get().query("SELECT * FROM fdr_schemes", this::mapFdr));
        if (fdrSchemes.isEmpty()) {
            addFdrScheme(new FDRScheme("FDR 1 Year", 6.0, 12, 10000));
            addFdrScheme(new FDRScheme("FDR 3 Years", 7.0, 36, 10000));
        }
    }

    private LoanScheme mapLoan(ResultSet rs) throws SQLException {
        LoanScheme s = new LoanScheme(rs.getString("name"), rs.getDouble("annual_rate"), rs.getDouble("max_amount"),
                rs.getInt("max_tenure_months"), rs.getDouble("min_balance"));
        s.id = rs.getInt("id");
        return s;
    }

    private DPSScheme mapDps(ResultSet rs) throws SQLException {
        DPSScheme s = new DPSScheme(rs.getString("name"), rs.getDouble("annual_rate"), rs.getInt("tenure_months"), rs.getDouble("min_monthly"));
        s.id = rs.getInt("id");
        return s;
    }

    private FDRScheme mapFdr(ResultSet rs) throws SQLException {
        FDRScheme s = new FDRScheme(rs.getString("name"), rs.getDouble("annual_rate"), rs.getInt("tenure_months"), rs.getDouble("min_amount"));
        s.id = rs.getInt("id");
        return s;
    }

    public List<LoanScheme> loanSchemes() { return loanSchemes; }
    public List<DPSScheme> dpsSchemes() { return dpsSchemes; }
    public List<FDRScheme> fdrSchemes() { return fdrSchemes; }

    public void addLoanScheme(LoanScheme s) {
        int id = Db.get().insertAndGetId(
                "INSERT INTO loan_schemes (name, annual_rate, max_amount, max_tenure_months, min_balance) VALUES (?,?,?,?,?)",
                s.name, s.annualRatePercent, s.maxAmount, s.maxTenureMonths, s.minEligibleBalance);
        s.id = id;
        loanSchemes.add(s);
    }

    public void removeLoanScheme(LoanScheme s) {
        Db.get().update("DELETE FROM loan_schemes WHERE id = ?", s.id);
        loanSchemes.remove(s);
    }

    public void addDpsScheme(DPSScheme s) {
        int id = Db.get().insertAndGetId(
                "INSERT INTO dps_schemes (name, annual_rate, tenure_months, min_monthly) VALUES (?,?,?,?)",
                s.name, s.annualRatePercent, s.tenureMonths, s.minMonthlyInstallment);
        s.id = id;
        dpsSchemes.add(s);
    }

    public void removeDpsScheme(DPSScheme s) {
        Db.get().update("DELETE FROM dps_schemes WHERE id = ?", s.id);
        dpsSchemes.remove(s);
    }

    public void addFdrScheme(FDRScheme s) {
        int id = Db.get().insertAndGetId(
                "INSERT INTO fdr_schemes (name, annual_rate, tenure_months, min_amount) VALUES (?,?,?,?)",
                s.name, s.annualRatePercent, s.tenureMonths, s.minAmount);
        s.id = id;
        fdrSchemes.add(s);
    }

    public void removeFdrScheme(FDRScheme s) {
        Db.get().update("DELETE FROM fdr_schemes WHERE id = ?", s.id);
        fdrSchemes.remove(s);
    }
}
