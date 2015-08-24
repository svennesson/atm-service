package se.sperm.atm.service;

import se.sperm.atm.model.BankReceipt;

public interface Bank {

    String getBankId();
    long getBalance(String accountHolderId);
    long withdrawMoney(String accountHolderId, long amount);
    BankReceipt requestReceipt(long transactionId);
}