package com.banking.util;

import java.util.Random;

public class AccountNumberGenerator {

    private static final String ACCOUNT_PREFIX = "ACC";
    private static final int ACCOUNT_NUMBER_LENGTH = 16;

    public static String generateAccountNumber() {
        Random random = new Random();
        StringBuilder accountNumber = new StringBuilder(ACCOUNT_PREFIX);

        for (int i = 0; i < ACCOUNT_NUMBER_LENGTH - ACCOUNT_PREFIX.length(); i++) {
            accountNumber.append(random.nextInt(10));
        }

        return accountNumber.toString();
    }

    public static boolean isValidAccountNumber(String accountNumber) {
        return accountNumber != null &&
                accountNumber.startsWith(ACCOUNT_PREFIX) &&
                accountNumber.length() == ACCOUNT_NUMBER_LENGTH &&
                accountNumber.substring(3).matches("\\d+");
    }
}
