package com.precioustech.fxtrading.oanda.restapi.account.transaction;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import com.precioustech.fxtrading.account.Account;
import com.precioustech.fxtrading.account.AccountDataProvider;
import com.precioustech.fxtrading.account.transaction.Transaction;
import com.precioustech.fxtrading.account.transaction.TransactionDataProvider;
import com.precioustech.fxtrading.events.Event;
import com.precioustech.fxtrading.oanda.restapi.account.transaction.dao.OandaTransactionDao;
import com.precioustech.fxtrading.oanda.restapi.account.transaction.entities.OandaAccount;
import com.precioustech.fxtrading.oanda.restapi.account.transaction.entities.OandaTransaction;

public class OandaTransactionService {

	private static final Logger LOG = Logger.getLogger(OandaTransactionService.class);

	@Autowired
	private OandaTransactionDao transactionDao;

	@Autowired
	private AccountDataProvider<Long> accountDataProvider;

	@Autowired
	private TransactionDataProvider<Long, Long, String> transactionDataProvider;

	@Resource(name = "transactionTypeHandlerMap")
	private Map<Event, IOandaTransactionTypeHandler> transactionTypeHandlerMap;

	@Autowired
	@Qualifier(value = "defaultTransactionTypeHandler")
	private IOandaTransactionTypeHandler defaultTransactionTypeHandler;

	@Transactional
	public void persistNewTransactions() {
		Collection<Account<Long>> allAccounts = this.accountDataProvider.getLatestAccountInfo();
		for (Account<Long> account : allAccounts) {
			OandaAccount oandaAcc = new OandaAccount(account.getAccountId(), account.getCurrency());
			Long maxTransactionId = this.transactionDao.transactionMaxIdForAccount(oandaAcc);
			List<Transaction<Long, Long, String>> newTransactions = this.transactionDataProvider
					.getTransactionsGreaterThanId(maxTransactionId, account.getAccountId());
			LOG.info(String.format("Found %d new transactions for account %d", newTransactions.size(),
					account.getAccountId()));

			for (Transaction<Long, Long, String> transaction : newTransactions) {
				Event evt = transaction.getTransactionType();
				IOandaTransactionTypeHandler typeHandler = this.transactionTypeHandlerMap.get(evt);
				if (typeHandler == null) {
					typeHandler = this.defaultTransactionTypeHandler;
				}
				LOG.info(transaction.getTransactionType() + "-" + transaction.getPrice());
				OandaTransaction oandaTransaction = typeHandler.handle(transaction);
				this.transactionDao.saveTransaction(oandaTransaction);
			}
			try {
				Thread.sleep(70000L);// Imposed by OANDA for 500 transactions
										// retrieval
			} catch (InterruptedException e) {
				LOG.error(e);
			}

		}
	}

	public Map<OandaAccount, Long> transactionsByAccount() {
		return this.transactionDao.transactionsCountByAccount();
	}

	public OandaTransaction transactionById(Long transactionId) {
		return this.transactionDao.findByTransactionId(transactionId);
	}

	public static void main(String args[]) {
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("oanda-transactions-app.xml");
		OandaTransactionService service = appContext.getBean(OandaTransactionService.class);
		service.persistNewTransactions();
		appContext.close();
	}
}
