package com.banking.exception;

public class InvalidPasswordException extends BankingException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}
