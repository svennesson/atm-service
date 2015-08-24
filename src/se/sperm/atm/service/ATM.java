package se.sperm.atm.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.sperm.atm.exception.ATMException;
import se.sperm.atm.exception.ATMSecurityException;
import se.sperm.atm.model.ATMCard;

public final class ATM {

    private Map<String, Bank> bankList;

    public ATM(List<Bank> banks) {
	bankList = new HashMap<String, Bank>();
	
	if (!banks.isEmpty()) {
	    for (Bank bank : banks) {
		this.bankList.put(bank.getBankId(), bank);
	    }
	} else {
	    throw new IllegalArgumentException("At least one bank"
			+ " have to be connected to the atm");
	}
    }

    public ATMSession verifyPin(int pin, ATMCard card) {
	if (card.verifyPin(pin)) {
	    return new ATMSessionImpl(card, getBank(card));
	}
	
	throw new ATMSecurityException("Wrong pincode");
    }

    private Bank getBank(ATMCard card) {
	if (bankList.get(card.getBankId()) == null) {
		throw new ATMException("ATM not connected to that bank");
	}
	return bankList.get(card.getBankId());
    }

}
