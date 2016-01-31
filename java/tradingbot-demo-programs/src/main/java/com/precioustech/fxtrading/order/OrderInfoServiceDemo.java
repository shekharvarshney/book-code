package com.precioustech.fxtrading.order;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.precioustech.fxtrading.account.AccountDataProvider;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.oanda.restapi.account.OandaAccountDataProviderService;
import com.precioustech.fxtrading.oanda.restapi.order.OandaOrderManagementProvider;

public class OrderInfoServiceDemo {

	private static final Logger LOG = Logger.getLogger(OrderInfoServiceDemo.class);

	private static void usage(String[] args) {
		if (args.length != 3) {
			LOG.error("Usage: OrderExecutionServiceDemo <url> <username> <accesstoken>");
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

		OrderInfoService<Long, String, Long> orderInfoService = new OrderInfoService<Long, String, Long>(
				orderManagementProvider);

		TradeableInstrument<String> gbpusd = new TradeableInstrument<String>("GBP_USD");

		orderInfoService.allPendingOrders();
		Collection<Order<String, Long>> pendingOrdersGbpUsd = orderInfoService.pendingOrdersForInstrument(gbpusd);

		LOG.info(String.format("+++++++++++++++++++ Dumping all pending orders for %s +++", gbpusd.getInstrument()));
		for (Order<String, Long> order : pendingOrdersGbpUsd) {
			LOG.info(String.format("units=%d, takeprofit=%2.5f,stoploss=%2.5f,limitprice=%2.5f,side=%s", order
					.getUnits(), order.getTakeProfit(), order.getStopLoss(), order.getPrice(), order.getSide()));
		}

		int usdPosCt = orderInfoService.findNetPositionCountForCurrency("USD");
		int gbpPosCt = orderInfoService.findNetPositionCountForCurrency("GBP");
		LOG.info("Net Position count for USD = " + usdPosCt);
		LOG.info("Net Position count for GBP = " + gbpPosCt);
		Collection<Order<String, Long>> pendingOrders = orderInfoService.allPendingOrders();
		LOG.info("+++++++++++++++++++ Dumping all pending orders ++++++++");
		for (Order<String, Long> order : pendingOrders) {
			LOG.info(String.format("instrument=%s,units=%d, takeprofit=%2.5f,stoploss=%2.5f,limitprice=%2.5f,side=%s",
					order.getInstrument().getInstrument(), order.getUnits(), order.getTakeProfit(),
					order.getStopLoss(), order.getPrice(), order.getSide()));
		}
	}
}
