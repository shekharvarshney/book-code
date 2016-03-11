package com.precioustech.fxtrading.oanda.restapi.account.transaction;

import com.precioustech.fxtrading.account.transaction.Transaction;
import com.precioustech.fxtrading.oanda.restapi.account.transaction.entities.OandaTransaction;

public interface IOandaTransactionTypeHandler {

	OandaTransaction handle(Transaction<Long, Long, String> transaction);
}
