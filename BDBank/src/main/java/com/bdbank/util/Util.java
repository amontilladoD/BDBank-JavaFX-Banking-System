package com.bdbank.util;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

public class Util {

    public static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm:ss a");
    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    private static final AtomicLong SEQ = new AtomicLong(System.currentTimeMillis() % 100000);

    /** SHA-256 hash for password storage - never store plain text passwords. */
    public static String hash(String plainText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(plainText.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    public static boolean matches(String plainText, String hash) {
        return hash(plainText).equals(hash);
    }

    public static String now() { return LocalDateTime.now().format(DATE_TIME_FMT); }

    public static String format(LocalDateTime dt) { return dt == null ? "-" : dt.format(DATE_TIME_FMT); }

    public static String nextId(String prefix) {
        return prefix + "-" + System.currentTimeMillis() + "-" + SEQ.incrementAndGet();
    }

    public static String nextAccountNumber() {
        return "BDB" + (1000000000L + Math.abs((System.nanoTime() ^ SEQ.incrementAndGet()) % 8999999999L));
    }

    public static boolean isValidPhone(String s) { return s != null && s.matches("01[0-9]{9}"); }
    public static boolean isValidEmail(String s) { return s != null && s.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$"); }
    public static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    public static double round2(double v) { return Math.round(v * 100.0) / 100.0; }
}
