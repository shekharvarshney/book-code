package com.precioustech.fxtrading.account;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.precioustech.fxtrading.BaseTradingConfig;
import com.precioustech.fxtrading.helper.ProviderHelper;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.CurrentPriceInfoProvider;
import com.precioustech.fxtrading.oanda.restapi.account.OandaAccountDataProviderService;
import com.precioustech.fxtrading.oanda.restapi.helper.OandaProviderHelper;
import com.precioustech.fxtrading.oanda.restapi.marketdata.OandaCurrentPriceInfoProvider;

public class AccountInfoServiceDemo {

	private static final Logger LOG = Logger.getLogger(AccountInfoServiceDemo.class);

	private static void usage(String[] args) {
		if (args.length != 3) {
			LOG.error("Usage: AccountInfoServiceDemo <url> <username> <accesstoken>");
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		usage(args);
		String url = args[0];
		String userName = args[1];
		String accessToken = args[2];

		// initialise the dependencies
		AccountDataProvider<Long> accountDataProvider = new OandaAccountDataProviderService(url, userName, accessToken);
		CurrentPriceInfoProvider<String> currentPriceInfoProvider = new OandaCurrentPriceInfoProvider(url, accessToken);
		BaseTradingConfig tradingConfig = new BaseTradingConfig();
		tradingConfig.setMinReserveRatio(0.05);
		tradingConfig.setMinAmountRequired(100.00);
		ProviderHelper<String> providerHelper = new OandaProviderHelper();

		AccountInfoService<Long, String> accountInfoService = new AccountInfoService<Long, String>(accountDataProvider,
				currentPriceInfoProvider, tradingConfig, providerHelper);

		Collection<Account<Long>> accounts = accountInfoService.getAllAccounts();
		LOG.info(String.format("Found %d accounts to trade for user %s", accounts.size(), userName));
		LOG.info("+++++++++++++++++++++++++++++++ Dumping Account Info +++++++++++++++++++++++++++++");
		for (Account<Long> account : accounts) {
			LOG.info(account);
		}
		LOG.info("++++++++++++++++++++++ Finished Dumping Account Info +++++++++++++++++++++++++++++");
		Account<Long> sampleAccount = accounts.iterator().next();
		final int units = 5000;
		TradeableInstrument<String> gbpusd = new TradeableInstrument<String>("GBP_USD");
		TradeableInstrument<String> eurgbp = new TradeableInstrument<String>("EUR_GBP");
		double gbpusdMarginReqd = accountInfoService.calculateMarginForTrade(sampleAccount, gbpusd, units);
		double eurgbpMarginReqd = accountInfoService.calculateMarginForTrade(sampleAccount, eurgbp, units);
		LOG.info(String.format("Marging requirement for trading pair %d units of %s is %5.2f %s ", units, gbpusd
				.getInstrument(), gbpusdMarginReqd, sampleAccount.getCurrency()));
		LOG.info(String.format("Marging requirement for trading pair %d units of %s is %5.2f %s ", units, eurgbp
				.getInstrument(), eurgbpMarginReqd, sampleAccount.getCurrency()));
	}

}
