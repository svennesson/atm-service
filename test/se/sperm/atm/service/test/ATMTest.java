package se.sperm.atm.service.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.sperm.atm.exception.ATMException;
import se.sperm.atm.exception.ATMSecurityException;
import se.sperm.atm.model.ATMCard;
import se.sperm.atm.model.ATMReceipt;
import se.sperm.atm.model.BankReceipt;
import se.sperm.atm.service.ATM;
import se.sperm.atm.service.ATMSession;
import se.sperm.atm.service.Bank;

@RunWith(MockitoJUnitRunner.class)
public class ATMTest {

    private ATM atm;
    private ATMCard atmCard, atmCardNotConnected;
    private ATMSession atmSession;
    private BankReceipt bankReceipt;
    private ATMReceipt atmReceipt;

    private final String accountHolderId = "abc123";
    private final String bankId = "123-4567-34";
    private final int pin = 1234;
    private final long transactionId = 10001;
    private final long otherTransactionId = 20345;
    private final long originalAccountBalance = 7000;
    private final int withdrawAmount = 2000;
    private final int withdrawToLargeAmount = 8000;
    private final int wrongInputNotEvenHundred = 347;
    private final int wrongInputAmountUnderHundred = 99;
    private final int wrongInputAmountOverTenThousand = 10010;

    private List<Bank> bankList;

    @Mock
    private Bank bank;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
	bankReceipt = new BankReceipt(bankId, transactionId, withdrawAmount, new Date());
	bankList = new ArrayList<Bank>();
	bankList.add(bank);
	atmCard = new ATMCard(accountHolderId, bankId, pin);

	// Mock behavior
	when(bank.getBankId()).thenReturn(bankId);
	when(bank.getBalance(accountHolderId)).thenReturn(originalAccountBalance);
	when(bank.withdrawMoney(accountHolderId, withdrawAmount)).thenReturn(transactionId);
	when(bank.withdrawMoney(accountHolderId, withdrawToLargeAmount)).thenReturn(otherTransactionId);
	when(bank.requestReceipt(transactionId)).thenReturn(bankReceipt);
    }

    @After
    public void tearDown() throws Exception {
	reset(bank);
    }
    
    @Test
    public void shouldThrowExceptionWhenInputNotEvenHundreds() {
	thrown.expect(ATMException.class);
	thrown.expectMessage("Wrong amount input");
	
	long transactionID = 0;

	atm = new ATM(bankList);
	atmSession = atm.verifyPin(pin, atmCard);
	transactionID = atmSession.withdrawAmount(wrongInputNotEvenHundred);
	
	assertNotEquals(transactionID, transactionId);
	verify(bank).getBankId();
	verify(bank, times(0)).withdrawMoney(accountHolderId, wrongInputNotEvenHundred);
    }
    
    @Test
    public void shouldThrowExceptionWhenInputUnderHundred() {
	thrown.expect(ATMException.class);
	thrown.expectMessage("Wrong amount input");
	
	long transactionID = 0;

	atm = new ATM(bankList);
	atmSession = atm.verifyPin(pin, atmCard);
	transactionID = atmSession.withdrawAmount(wrongInputAmountUnderHundred);
	
	assertNotEquals(transactionID, transactionId);
	verify(bank).getBankId();
	verify(bank, times(0)).withdrawMoney(accountHolderId, wrongInputAmountUnderHundred);
    }
    
    @Test
    public void shouldThrowExceptionWhenInputOverTenThousand() {
	thrown.expect(ATMException.class);
	thrown.expectMessage("Wrong amount input");
	
	long transactionID = 0;

	atm = new ATM(bankList);
	atmSession = atm.verifyPin(pin, atmCard);
	transactionID = atmSession.withdrawAmount(wrongInputAmountOverTenThousand);
	
	assertNotEquals(transactionID, transactionId);
	verify(bank).getBankId();
	verify(bank, times(0)).withdrawMoney(accountHolderId, wrongInputAmountOverTenThousand);
    }

    @Test
    public void canCheckPinCode() {
	thrown.expect(ATMSecurityException.class);
	atm = new ATM(bankList);
	atmSession = atm.verifyPin(5678, atmCard);

	assertNull(atmSession);
	verify(bank).getBankId();
    }

    @Test
    public void canCheckATMCardBankConnection() {
	atm = new ATM(bankList);
	atmCardNotConnected = new ATMCard(accountHolderId, "987-6543-374", pin);

	thrown.expect(ATMException.class);
	thrown.expectMessage("ATM not connected to that bank");
	atmSession = atm.verifyPin(pin, atmCardNotConnected);

	assertNull(atmSession);
	verify(bank).getBankId();
    }

    @Test
    public void canNotCallMethodsTwice() {
	long transactionId1 = 0, transactionId2 = 0;

	atm = new ATM(bankList);
	atmSession = atm.verifyPin(pin, atmCard);

	try {
	    transactionId1 = atmSession.withdrawAmount(withdrawAmount);
	    transactionId2 = atmSession.withdrawAmount(withdrawAmount);
	    fail();
	} catch (ATMException e) {
	    assertNotEquals(transactionId1, transactionId2);
	}

	verify(bank).getBankId();
    }

    @Test
    public void canCheckWithdrawAmount() {
	long transactionID = 0;

	atm = new ATM(bankList);
	atmSession = atm.verifyPin(pin, atmCard);

	transactionID = atmSession.withdrawAmount(withdrawAmount);
	assertEquals(transactionId, transactionID);

	verify(bank).getBankId();
	verify(bank).getBalance(accountHolderId);
	verify(bank).withdrawMoney(accountHolderId, withdrawAmount);
    }

    @Test
    public void canCheckBalance() {
	long accountBalance = 0;

	atm = new ATM(bankList);
	atmSession = atm.verifyPin(pin, atmCard);

	accountBalance = atmSession.checkBalance();
	assertEquals(originalAccountBalance, accountBalance);

	verify(bank).getBankId();
	verify(bank).getBalance(accountHolderId);
    }

    @Test
    public void canRequestReceipt() {
	atm = new ATM(bankList);
	atmSession = atm.verifyPin(pin, atmCard);

	atmReceipt = atmSession.requestReceipt(transactionId);
	assertEquals(withdrawAmount, atmReceipt.getAmount());

	verify(bank).getBankId();
	verify(bank).requestReceipt(transactionId);
    }

    @Test
    public void canCheckBankList() {
	thrown.expect(IllegalArgumentException.class);
	bankList.clear();
	atm = new ATM(bankList);
	
	assertNull(atm);
	verify(bank, times(0)).getBankId();
    }

    @Test
    public void shouldThrowExceptionWhenTryingToWithdrawAmountHigherThanBalance() {
	thrown.expect(ATMException.class);
	thrown.expectMessage("Not enough money on account");
	
	long transactionID = 0;

	atm = new ATM(bankList);
	atmSession = atm.verifyPin(pin, atmCard);
	transactionID = atmSession.withdrawAmount(withdrawToLargeAmount);

	assertNotEquals(otherTransactionId, transactionID);
	verify(bank).getBankId();
	verify(bank).getBalance(accountHolderId);
	verify(bank, times(0)).withdrawMoney(accountHolderId, withdrawToLargeAmount);
    }

}
