package se.sperm.atm.model;

import java.util.Date;

public final class BankReceipt {

    private final String bankId;
    private final long transactionId;
    private final int amount;
    private final Date date;

    public BankReceipt(String bankId, long transactionId, int amount, Date date) {
	this.bankId = bankId;
	this.transactionId = transactionId;
	this.amount = amount;
	this.date = date;
    }

    public String getBankId() {
	return bankId;
    }

    public long getTransactionId() {
	return transactionId;
    }

    public int getAmount() {
	return amount;
    }

    public Date getDate() {
	return date;
    }

}
