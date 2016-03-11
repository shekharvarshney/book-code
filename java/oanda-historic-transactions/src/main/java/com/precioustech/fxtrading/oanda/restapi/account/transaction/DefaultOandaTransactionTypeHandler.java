package com.precioustech.fxtrading.oanda.restapi.account.transaction;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.precioustech.fxtrading.account.transaction.Transaction;
import com.precioustech.fxtrading.oanda.restapi.account.transaction.dao.OandaAccountDao;
import com.precioustech.fxtrading.oanda.restapi.account.transaction.entities.OandaAccount;
import com.precioustech.fxtrading.oanda.restapi.account.transaction.entities.OandaTransaction;

//@Component("defaultHandler")
public class DefaultOandaTransactionTypeHandler implements IOandaTransactionTypeHandler {

	@Autowired
	protected OandaAccountDao accountDao;

	@Override
	@Transactional
	public OandaTransaction handle(Transaction<Long, Long, String> transaction) {
		OandaTransaction oandaTransaction = new OandaTransaction();
		OandaAccount account = fetchAccountPojo(transaction.getAccountId());
		oandaTransaction.setAccount(account);
		oandaTransaction.setInterest(transaction.getInterest());
		oandaTransaction.setLinkedTransactionId(transaction.getLinkedTransactionId());
		oandaTransaction.setPnl(transaction.getPnl());
		oandaTransaction.setPrice(transaction.getPrice() == null ? 0.0 : transaction.getPrice());
		oandaTransaction.setTransactionId(transaction.getTransactionId());
		oandaTransaction.setTransactionTime(transaction.getTransactionTime());
		oandaTransaction.setTransactionType(transaction.getTransactionType().name());
		oandaTransaction.setUnits(transaction.getUnits());
		oandaTransaction.setInstrument(transaction.getInstrument().getInstrument());
		return oandaTransaction;
	}

	private OandaAccount fetchAccountPojo(Long accountId) {
		Collection<OandaAccount> allAccounts = this.accountDao.allAccounts();
		for (OandaAccount account : allAccounts) {
			if (accountId.equals(account.getAccountId())) {
				return account;
			}
		}
		return null;
	}

}
