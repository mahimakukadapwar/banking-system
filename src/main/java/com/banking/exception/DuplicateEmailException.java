package com.banking.exception;

public class DuplicateEmailException extends BankingException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}
