package com.banking.exception;

public class UnauthorizedException extends BankingException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
