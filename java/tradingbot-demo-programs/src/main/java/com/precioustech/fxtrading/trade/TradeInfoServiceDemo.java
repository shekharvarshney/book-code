package com.precioustech.fxtrading.trade;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.precioustech.fxtrading.BaseTradingConfig;
import com.precioustech.fxtrading.account.AccountDataProvider;
import com.precioustech.fxtrading.account.AccountInfoService;
import com.precioustech.fxtrading.helper.ProviderHelper;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.CurrentPriceInfoProvider;
import com.precioustech.fxtrading.oanda.restapi.account.OandaAccountDataProviderService;
import com.precioustech.fxtrading.oanda.restapi.helper.OandaProviderHelper;
import com.precioustech.fxtrading.oanda.restapi.marketdata.OandaCurrentPriceInfoProvider;
import com.precioustech.fxtrading.oanda.restapi.trade.OandaTradeManagementProvider;

public class TradeInfoServiceDemo {

	private static final Logger LOG = Logger.getLogger(TradeInfoServiceDemo.class);

	private static void usage(String[] args) {
		if (args.length != 3) {
			LOG.error("Usage: TradeInfoServiceDemo <url> <username> <accesstoken>");
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		usage(args);
		String url = args[0];
		String userName = args[1];
		String accessToken = args[2];
		AccountDataProvider<Long> accountDataProvider = new OandaAccountDataProviderService(url, userName, accessToken);
		CurrentPriceInfoProvider<String> currentPriceInfoProvider = new OandaCurrentPriceInfoProvider(url, accessToken);
		BaseTradingConfig tradingConfig = new BaseTradingConfig();
		tradingConfig.setMinReserveRatio(0.05);
		tradingConfig.setMinAmountRequired(100.00);
		tradingConfig.setMaxAllowedQuantity(10);
		ProviderHelper<String> providerHelper = new OandaProviderHelper();
		AccountInfoService<Long, String> accountInfoService = new AccountInfoService<Long, String>(accountDataProvider,
				currentPriceInfoProvider, tradingConfig, providerHelper);
		TradeManagementProvider<Long, String, Long> tradeManagementProvider = new OandaTradeManagementProvider(url,
				accessToken);

		TradeInfoService<Long, String, Long> tradeInfoService = new TradeInfoService<Long, String, Long>(
				tradeManagementProvider, accountInfoService);

		tradeInfoService.init();
		Collection<Trade<Long, String, Long>> allTrades = tradeInfoService.getAllTrades();
		LOG.info("################ Dumping All Trades ################");
		for (Trade<Long, String, Long> trade : allTrades) {
			LOG.info(String.format("Units=%d,Side=%s,Instrument=%s,Price=%2.5f", trade.getUnits(), trade.getSide(),
					trade.getInstrument().getInstrument(), trade.getExecutionPrice()));
		}
		int chfTrades = tradeInfoService.findNetPositionCountForCurrency("CHF");
		int cadTrades = tradeInfoService.findNetPositionCountForCurrency("CAD");
		LOG.info("Net Position for CHF = " + chfTrades);
		LOG.info("Net Position for CAD = " + cadTrades);
		TradeableInstrument<String> cadchf = new TradeableInstrument<String>("CAD_CHF");
		TradeableInstrument<String> usdcad = new TradeableInstrument<String>("USD_CAD");
		boolean isCadChdTradeExists = tradeInfoService.isTradeExistsForInstrument(cadchf);
		boolean isUsdCadTradeExists = tradeInfoService.isTradeExistsForInstrument(usdcad);
		LOG.info(cadchf.getInstrument() + " exists?" + isCadChdTradeExists);
		LOG.info(usdcad.getInstrument() + " exists?" + isUsdCadTradeExists);

	}

}
