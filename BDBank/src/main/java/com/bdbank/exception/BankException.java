package com.bdbank.exception;

/**
 * Single custom checked exception for all business-rule / validation failures
 * (insufficient balance, invalid credentials, account not found, invalid input, etc).
 * Keeping one exception type with a clear message keeps the try/catch blocks in
 * the controllers simple and consistent while still being a deliberate, named
 * exception type rather than relying on generic RuntimeExceptions.
 */
public class BankException extends Exception {
    public BankException(String message) {
        super(message);
    }
    public BankException(String message, Throwable cause) {
        super(message, cause);
    }
}
