package com.precioustech.fxtrading.order;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.precioustech.fxtrading.BaseTradingConfig;
import com.precioustech.fxtrading.TradingDecision;
import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.account.AccountDataProvider;
import com.precioustech.fxtrading.account.AccountInfoService;
import com.precioustech.fxtrading.account.AccountInfoServiceDemo;
import com.precioustech.fxtrading.helper.ProviderHelper;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.CurrentPriceInfoProvider;
import com.precioustech.fxtrading.marketdata.historic.HistoricMarketDataProvider;
import com.precioustech.fxtrading.marketdata.historic.MovingAverageCalculationService;
import com.precioustech.fxtrading.oanda.restapi.account.OandaAccountDataProviderService;
import com.precioustech.fxtrading.oanda.restapi.helper.OandaProviderHelper;
import com.precioustech.fxtrading.oanda.restapi.marketdata.OandaCurrentPriceInfoProvider;
import com.precioustech.fxtrading.oanda.restapi.marketdata.historic.OandaHistoricMarketDataProvider;
import com.precioustech.fxtrading.oanda.restapi.order.OandaOrderManagementProvider;
import com.precioustech.fxtrading.oanda.restapi.trade.OandaTradeManagementProvider;
import com.precioustech.fxtrading.trade.TradeInfoService;
import com.precioustech.fxtrading.trade.TradeManagementProvider;

public class OrderExecutionServiceDemo {

	private static final Logger LOG = Logger.getLogger(AccountInfoServiceDemo.class);

	private static void usage(String[] args) {
		if (args.length != 3) {
			LOG.error("Usage: OrderExecutionServiceDemo <url> <username> <accesstoken>");
			System.exit(1);
		}
	}

	public static void main(String[] args) throws Exception {

		usage(args);
		String url = args[0];
		String userName = args[1];
		String accessToken = args[2];

		BlockingQueue<TradingDecision<String>> orderQueue = new LinkedBlockingQueue<TradingDecision<String>>();

		AccountDataProvider<Long> accountDataProvider = new OandaAccountDataProviderService(url, userName, accessToken);
		CurrentPriceInfoProvider<String> currentPriceInfoProvider = new OandaCurrentPriceInfoProvider(url, accessToken);
		BaseTradingConfig tradingConfig = new BaseTradingConfig();
		tradingConfig.setMinReserveRatio(0.05);
		tradingConfig.setMinAmountRequired(100.00);
		tradingConfig.setMaxAllowedQuantity(10);
		ProviderHelper<String> providerHelper = new OandaProviderHelper();
		AccountInfoService<Long, String> accountInfoService = new AccountInfoService<Long, String>(accountDataProvider,
				currentPriceInfoProvider, tradingConfig, providerHelper);

		OrderManagementProvider<Long, String, Long> orderManagementProvider = new OandaOrderManagementProvider(url,
				accessToken, accountDataProvider);

		TradeManagementProvider<Long, String, Long> tradeManagementProvider = new OandaTradeManagementProvider(url,
				accessToken);

		OrderInfoService<Long, String, Long> orderInfoService = new OrderInfoService<Long, String, Long>(
				orderManagementProvider);

		TradeInfoService<Long, String, Long> tradeInfoService = new TradeInfoService<Long, String, Long>(
				tradeManagementProvider, accountDataProvider);

		HistoricMarketDataProvider<String> historicMarketDataProvider = new OandaHistoricMarketDataProvider(url,
				accessToken);

		MovingAverageCalculationService<String> movingAverageCalculationService = new MovingAverageCalculationService<String>(
				historicMarketDataProvider);

		PreOrderValidationService<Long, String, Long> preOrderValidationService = new PreOrderValidationService<Long, String, Long>(
				tradeInfoService, movingAverageCalculationService, tradingConfig, orderInfoService);

		OrderExecutionService<Long, String, Long> orderExecService = new OrderExecutionService<Long, String, Long>(
				orderQueue, accountInfoService, orderManagementProvider, tradingConfig, preOrderValidationService,
				currentPriceInfoProvider);
		orderExecService.init();

		TradingDecision<String> decision = new TradingDecision<String>(new TradeableInstrument<String>("GBP_USD"),
				TradingSignal.LONG, 1.44, 1.35, 1.4);
		orderQueue.offer(decision);
		Thread.sleep(10000);// enough time to place an order
		orderExecService.shutDown();

	}
}
