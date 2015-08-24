package se.sperm.atm.service;

import se.sperm.atm.model.ATMReceipt;

public interface ATMSession {

    long withdrawAmount(int amount);
    ATMReceipt requestReceipt(long transactionId);
    long checkBalance();

}
