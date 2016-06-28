package com.precioustech.fxtrading.remotecontrol.handlers;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

import com.precioustech.fxtrading.account.Account;
import com.precioustech.fxtrading.account.AccountInfoService;
import com.precioustech.fxtrading.trade.TradeInfoService;

public class PingTradeDataGenerator<M, N, K> implements PingContentGenerator<String> {

	private final TradeInfoService<M, N, K> tradeInfoService;
	private final AccountInfoService<K, N> accountInfoService;
	private static final String SEP_CHR = "+";
	public PingTradeDataGenerator(TradeInfoService<M, N, K> tradeInfoService,
			AccountInfoService<K, N> accountInfoService) {
		this.tradeInfoService = tradeInfoService;
		this.accountInfoService = accountInfoService;
	}

	@Override
	public String generate(String[] args) {
		Collection<Account<K>> accounts = accountInfoService.getAllAccounts();
		StringBuilder content = new StringBuilder(generateSeparator());
		for (Account<K> account : accounts) {
			content.append(System.lineSeparator()).append(account.toString()).append(System.lineSeparator());
		}
		content.append(generateSeparator());
		// Collection<Trade<M, N, K>> allTrades =
		// this.tradeInfoService.getAllTrades();
		return content.toString();
	}

	private String generateSeparator() {
		return StringUtils.repeat(SEP_CHR, 105);
	}

}
