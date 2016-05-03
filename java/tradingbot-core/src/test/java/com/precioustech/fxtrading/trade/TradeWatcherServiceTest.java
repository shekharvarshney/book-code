package com.precioustech.fxtrading.trade;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.precioustech.fxtrading.BaseTradingConfig;
import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.instrument.InstrumentService;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.CurrentPriceInfoProvider;
import com.precioustech.fxtrading.marketdata.Price;

@SuppressWarnings("unchecked")
public class TradeWatcherServiceTest {

	private final double pipNonJpy = 0.0001;
	private final double pipJpy = 0.01;
	private final double profitTgt = 15.0;
	private final TradeableInstrument<String> eurusd = new TradeableInstrument<>("EUR_USD");
	private final TradeableInstrument<String> gbpusd = new TradeableInstrument<>("GBP_USD");
	private final TradeableInstrument<String> gbpnzd = new TradeableInstrument<>("GBP_NZD");
	private final TradeableInstrument<String> audjpy = new TradeableInstrument<>("AUD_JPY");
	private final TradeableInstrument<String> cadchf = new TradeableInstrument<>("CAD_CHF");
	@Test
	public void tradeWatcher() {
		TradeInfoService<Long, String, Long> tradeInfoService = mock(TradeInfoService.class);
		CurrentPriceInfoProvider<String> currPriceInfoProvider = mock(CurrentPriceInfoProvider.class);
		InstrumentService<String> instrumentService = mock(InstrumentService.class);
		TradeManagementProvider<Long, String, Long> tradeMgmtProvider = mock(TradeManagementProvider.class);
		BaseTradingConfig config = mock(BaseTradingConfig.class);
		TradeWatcherService<Long, String, Long> tradeWatcherService = new TradeWatcherService<>(tradeInfoService,
				currPriceInfoProvider, instrumentService, tradeMgmtProvider, config, Collections.emptySet());

		Set<TradeableInstrument<String>> instruments = Sets.newHashSet(eurusd, gbpusd, gbpnzd, audjpy, cadchf);

		List<Trade<Long, String, Long>> trades = createTrades();
		Map<TradeableInstrument<String>, Price<String>> prices = createPrices();
		when(currPriceInfoProvider.getCurrentPricesForInstruments(eq(instruments))).thenReturn(prices);
		when(tradeInfoService.getAllTrades()).thenReturn(trades);
		when(instrumentService.getPipForInstrument(eurusd)).thenReturn(pipNonJpy);
		when(instrumentService.getPipForInstrument(gbpusd)).thenReturn(pipNonJpy);
		when(instrumentService.getPipForInstrument(gbpnzd)).thenReturn(pipNonJpy);
		when(instrumentService.getPipForInstrument(audjpy)).thenReturn(pipJpy);
		when(instrumentService.getPipForInstrument(cadchf)).thenReturn(pipNonJpy);
		when(config.getMinProfitTarget()).thenReturn(profitTgt);
		tradeWatcherService.watch();
		verify(tradeInfoService, times(4)).refreshTradesForAccount(null);
		verify(tradeMgmtProvider, times(1)).modifyTrade(null, null, 1.1259, 0.0);
		verify(tradeMgmtProvider, times(1)).modifyTrade(null, null, 83.29, 0.0);
		verify(tradeMgmtProvider, times(1)).modifyTrade(null, null, 1.4407, 0.0);
		verify(tradeMgmtProvider, times(1)).modifyTrade(null, null, 0.7742, 0.0);
	}

	private Map<TradeableInstrument<String>, Price<String>> createPrices() {
		Map<TradeableInstrument<String>, Price<String>> pricesMap = Maps.newHashMap();

		Price<String> price1 = mock(Price.class);
		when(price1.getInstrument()).thenReturn(eurusd);
		when(price1.getBidPrice()).thenReturn(1.1274);
		when(price1.getAskPrice()).thenReturn(1.1275);

		Price<String> price2 = mock(Price.class);
		when(price2.getInstrument()).thenReturn(gbpusd);
		when(price2.getBidPrice()).thenReturn(1.4391);
		when(price2.getAskPrice()).thenReturn(1.4392);

		Price<String> price3 = mock(Price.class);
		when(price3.getInstrument()).thenReturn(gbpnzd);
		when(price3.getBidPrice()).thenReturn(2.1045);
		when(price3.getAskPrice()).thenReturn(2.1052);

		Price<String> price4 = mock(Price.class);
		when(price4.getInstrument()).thenReturn(audjpy);
		when(price4.getBidPrice()).thenReturn(83.44);
		when(price4.getAskPrice()).thenReturn(83.46);

		Price<String> price5 = mock(Price.class);
		when(price5.getInstrument()).thenReturn(cadchf);
		when(price5.getBidPrice()).thenReturn(0.7724);
		when(price5.getAskPrice()).thenReturn(0.7727);


		pricesMap.put(eurusd, price1);
		pricesMap.put(gbpusd, price2);
		pricesMap.put(gbpnzd, price3);
		pricesMap.put(audjpy, price4);
		pricesMap.put(cadchf, price5);
		return pricesMap;
	}

	private List<Trade<Long, String, Long>> createTrades() {
		List<Trade<Long, String, Long>> mockTrades = Lists.newArrayList();

		Trade<Long, String, Long> trade1 = mock(Trade.class);
		when(trade1.getInstrument()).thenReturn(eurusd);
		when(trade1.getSide()).thenReturn(TradingSignal.LONG);
		when(trade1.getExecutionPrice()).thenReturn(1.1255);
		when(trade1.getStopLoss()).thenReturn(0.0);

		Trade<Long, String, Long> trade2 = mock(Trade.class);
		when(trade2.getInstrument()).thenReturn(gbpusd);
		when(trade2.getSide()).thenReturn(TradingSignal.SHORT);
		when(trade2.getExecutionPrice()).thenReturn(1.4415);
		when(trade2.getStopLoss()).thenReturn(0.0);

		Trade<Long, String, Long> trade3 = mock(Trade.class);
		when(trade3.getInstrument()).thenReturn(gbpnzd);
		when(trade3.getSide()).thenReturn(TradingSignal.SHORT);
		when(trade3.getExecutionPrice()).thenReturn(2.1025);
		when(trade3.getStopLoss()).thenReturn(0.0);

		Trade<Long, String, Long> trade4 = mock(Trade.class);
		when(trade4.getInstrument()).thenReturn(audjpy);
		when(trade4.getSide()).thenReturn(TradingSignal.LONG);
		when(trade4.getExecutionPrice()).thenReturn(83.13);
		when(trade4.getStopLoss()).thenReturn(83.23);

		Trade<Long, String, Long> trade5 = mock(Trade.class);
		when(trade5.getInstrument()).thenReturn(cadchf);
		when(trade5.getSide()).thenReturn(TradingSignal.SHORT);
		when(trade5.getExecutionPrice()).thenReturn(0.7784);
		when(trade5.getStopLoss()).thenReturn(0.7754);

		mockTrades.add(trade1);
		mockTrades.add(trade2);
		mockTrades.add(trade3);
		mockTrades.add(trade4);
		mockTrades.add(trade5);

		return mockTrades;
	}

	@Test
	public void mapView() {
		int n1 = 5;
		int n2 = 3;

		final List<Trade<Long, String, Long>> trades = Lists.newArrayList();
		IntStream.range(0, n1).forEach(i -> {
			Trade<Long, String, Long> trade = mock(Trade.class);
			when(trade.getInstrument()).thenReturn(eurusd);
			trades.add(trade);
		});

		IntStream.range(0, n2).forEach(i -> {
			Trade<Long, String, Long> trade = mock(Trade.class);
			when(trade.getInstrument()).thenReturn(gbpusd);
			trades.add(trade);
		});

		TradeWatcherService<Long, String, Long> service = new TradeWatcherService<>(null, null, null, null, null, null);

		Map<TradeableInstrument<String>, Collection<Trade<Long, String, Long>>> mapView = service
				.convertToMapView(trades);

		assertEquals(2, mapView.keySet().size());
		assertEquals(n1, mapView.get(eurusd).size());
		assertEquals(n2, mapView.get(gbpusd).size());

	}
}
