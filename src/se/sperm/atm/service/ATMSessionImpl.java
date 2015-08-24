package se.sperm.atm.service;

import se.sperm.atm.exception.ATMException;
import se.sperm.atm.model.ATMCard;
import se.sperm.atm.model.ATMReceipt;
import se.sperm.atm.model.BankReceipt;

class ATMSessionImpl extends AbstractATMSession {

    private boolean onlyOneMethodCanBeCalled = true;

    public ATMSessionImpl(ATMCard atmCard, Bank bank) {
	super(atmCard, bank);
    }

    @Override
    public long withdrawAmount(int amount) {
	if ((amount > 100) && (amount < 10000) && ((amount % 100) == 0)) {
	    if (bank.getBalance(atmCard.getAccountHolderId()) >= amount) {
		if (onlyOneMethodCanBeCalled) {
		    onlyOneMethodCanBeCalled = false;
		    return bank.withdrawMoney(atmCard.getAccountHolderId(), amount);
		} else {
		    throw new ATMException("Can only be called one time per session");
		}
	    } else {
		throw new ATMException("Not enough money on account");
	    }
	}
	
	throw new ATMException("Wrong amount input");
    }

    @Override
    public ATMReceipt requestReceipt(long transactionId) {
	BankReceipt bankReceipt;
	bankReceipt = bank.requestReceipt(transactionId);
	
	return new ATMReceipt(transactionId, bankReceipt.getAmount());
    }

    @Override
    public long checkBalance() {
	if (onlyOneMethodCanBeCalled) {
	    onlyOneMethodCanBeCalled = false;
	    return bank.getBalance(atmCard.getAccountHolderId());
	}

	throw new ATMException("Can only be called one time per session");
    }

}
