package com.precioustech.fxtrading.order;

import org.apache.log4j.Logger;

import com.precioustech.fxtrading.BaseTradingConfig;
import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.account.AccountDataProvider;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.historic.HistoricMarketDataProvider;
import com.precioustech.fxtrading.marketdata.historic.MovingAverageCalculationService;
import com.precioustech.fxtrading.oanda.restapi.account.OandaAccountDataProviderService;
import com.precioustech.fxtrading.oanda.restapi.marketdata.historic.OandaHistoricMarketDataProvider;
import com.precioustech.fxtrading.oanda.restapi.order.OandaOrderManagementProvider;
import com.precioustech.fxtrading.oanda.restapi.trade.OandaTradeManagementProvider;
import com.precioustech.fxtrading.trade.TradeInfoService;
import com.precioustech.fxtrading.trade.TradeManagementProvider;

public class PreValidationServiceDemo {

	private static final Logger LOG = Logger.getLogger(PreValidationServiceDemo.class);

	private static void usage(String[] args) {
		if (args.length != 3) {
			LOG.error("Usage: PreValidationServiceDemo <url> <username> <accesstoken>");
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		usage(args);
		String url = args[0];
		String userName = args[1];
		String accessToken = args[2];

		AccountDataProvider<Long> accountDataProvider = new OandaAccountDataProviderService(url, userName, accessToken);

		OrderManagementProvider<Long, String, Long> orderManagementProvider = new OandaOrderManagementProvider(url,
				accessToken, accountDataProvider);

		TradeManagementProvider<Long, String, Long> tradeManagementProvider = new OandaTradeManagementProvider(url,
				accessToken);

		BaseTradingConfig tradingConfig = new BaseTradingConfig();
		tradingConfig.setMinReserveRatio(0.05);
		tradingConfig.setMinAmountRequired(100.00);
		tradingConfig.setMaxAllowedQuantity(10);
		tradingConfig.setMaxAllowedNetContracts(3);



		TradeInfoService<Long, String, Long> tradeInfoService = new TradeInfoService<Long, String, Long>(
				tradeManagementProvider, accountDataProvider);

		tradeInfoService.init();

		HistoricMarketDataProvider<String> historicMarketDataProvider = new OandaHistoricMarketDataProvider(url,
				accessToken);

		MovingAverageCalculationService<String> movingAverageCalculationService = new MovingAverageCalculationService<String>(
				historicMarketDataProvider);

		OrderInfoService<Long, String, Long> orderInfoService = new OrderInfoService<Long, String, Long>(
				orderManagementProvider);

		PreOrderValidationService<Long, String, Long> preOrderValidationService = new PreOrderValidationService<Long, String, Long>(
				tradeInfoService, movingAverageCalculationService, tradingConfig, orderInfoService);

		TradeableInstrument<String> eurusd = new TradeableInstrument<String>("EUR_USD");
		TradeableInstrument<String> usdjpy = new TradeableInstrument<String>("USD_JPY");
		boolean isEurUsdTraded = preOrderValidationService.checkInstrumentNotAlreadyTraded(eurusd);
		boolean isUsdJpyTraded = preOrderValidationService.checkInstrumentNotAlreadyTraded(usdjpy);
		LOG.info(eurusd.getInstrument() + " trade present? " + !isEurUsdTraded);
		LOG.info(usdjpy.getInstrument() + " trade present? " + !isUsdJpyTraded);

		TradeableInstrument<String> usdzar = new TradeableInstrument<String>("USD_ZAR");

		boolean isUsdZarTradeInSafeZone = preOrderValidationService.isInSafeZone(TradingSignal.LONG, 17.9, usdzar);
		LOG.info(usdzar.getInstrument() + " in safe zone? " + isUsdZarTradeInSafeZone);
		boolean isEurUsdTradeInSafeZone = preOrderValidationService.isInSafeZone(TradingSignal.LONG, 1.2, eurusd);
		LOG.info(eurusd.getInstrument() + " in safe zone? " + isEurUsdTradeInSafeZone);

		TradeableInstrument<String> nzdchf = new TradeableInstrument<String>("NZD_CHF");

		preOrderValidationService.checkLimitsForCcy(nzdchf, TradingSignal.LONG);
	}

}
