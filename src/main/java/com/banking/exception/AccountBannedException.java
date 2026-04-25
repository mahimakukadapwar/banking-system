package com.banking.exception;

public class AccountBannedException extends BankingException {
    public AccountBannedException(String message) {
        super(message);
    }
}
