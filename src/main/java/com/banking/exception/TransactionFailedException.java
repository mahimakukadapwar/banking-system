package com.banking.exception;

public class TransactionFailedException extends BankingException {
    public TransactionFailedException(String message) {
        super(message);
    }
}
