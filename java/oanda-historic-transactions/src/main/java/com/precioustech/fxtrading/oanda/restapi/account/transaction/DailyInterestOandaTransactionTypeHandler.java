package com.precioustech.fxtrading.oanda.restapi.account.transaction;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.account.transaction.Transaction;
import com.precioustech.fxtrading.oanda.restapi.account.transaction.entities.OandaTransaction;
import com.precioustech.fxtrading.trade.Trade;
import com.precioustech.fxtrading.trade.TradeInfoService;

//@Component("dailyInterestHandler")
public class DailyInterestOandaTransactionTypeHandler implements IOandaTransactionTypeHandler {

	@Autowired
	private TradeInfoService<Long, String, Long> tradeInfoService;

	@Autowired
	@Qualifier(value = "defaultTransactionTypeHandler")
	private IOandaTransactionTypeHandler defaultTransactionTypeHandler;

	@Override
	@Transactional
	public OandaTransaction handle(Transaction<Long, Long, String> transaction) {
		OandaTransaction oandaTransaction = defaultTransactionTypeHandler.handle(transaction);

		Collection<Trade<Long, String, Long>> instrumentTrades = this.tradeInfoService
				.getTradesForAccountAndInstrument(transaction.getAccountId(), transaction.getInstrument());
		if (!instrumentTrades.isEmpty()) {
			Trade<Long, String, Long> instrumentTrade = instrumentTrades.iterator().next();
			long units = instrumentTrade.getUnits();
			if (transaction.getSide() == TradingSignal.SHORT) {
				units *= -1;
			}
			oandaTransaction.setUnits(units);
		}
		oandaTransaction.setPrice(0.0);
		return oandaTransaction;
	}
}
