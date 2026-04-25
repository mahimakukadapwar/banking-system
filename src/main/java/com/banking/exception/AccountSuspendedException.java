package com.banking.exception;

public class AccountSuspendedException extends BankingException {
    public AccountSuspendedException(String message) {
        super(message);
    }
}
