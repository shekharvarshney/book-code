package com.precioustech.fxtrading.oanda.restapi.account.transaction;

import java.util.List;

import org.apache.log4j.Logger;

import com.precioustech.fxtrading.account.transaction.Transaction;
import com.precioustech.fxtrading.account.transaction.TransactionDataProvider;

public class TransactionDataProviderDemo {

	private static final Logger LOG = Logger.getLogger(TransactionDataProviderDemo.class);

	private static void usage(String[] args) {
		if (args.length != 4) {
			LOG.error("Usage: TransactionDataProviderDemo <url> <accesstoken> <minTransactionId> <accountId>");
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		usage(args);
		final String url = args[0];
		final String accessToken = args[1];
		final Long transactionId = Long.parseLong(args[2]);
		final Long accountId = Long.parseLong(args[3]);
		TransactionDataProvider<Long, Long, String> transactionDataProvider = new OandaTransactionDataProviderService(
				url, accessToken);

		List<Transaction<Long, Long, String>> transactions = transactionDataProvider
				.getTransactionsGreaterThanId(transactionId, accountId);
		LOG.info(String.format("++++ Found %d historic transactions from id %d for acc id = %d", transactions.size(),
				transactionId, accountId));
		for (Transaction<Long, Long, String> transaction : transactions) {

			if (transaction != null) {
				LOG.info(transaction);
			}
		}
	}

}
