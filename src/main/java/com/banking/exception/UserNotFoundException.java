package com.banking.exception;

public class UserNotFoundException extends BankingException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
