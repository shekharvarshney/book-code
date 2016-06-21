package com.precioustech.fxtrading.trade;

import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.precioustech.fxtrading.BaseTradingConfig;
import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.instrument.InstrumentService;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.CurrentPriceInfoProvider;
import com.precioustech.fxtrading.marketdata.Price;
import com.precioustech.fxtrading.remotecontrol.ToggleServices;
import com.precioustech.fxtrading.utils.TradingUtils;

public class TradeWatcherService<M, N, K> implements ToggleServices {

	private final TradeInfoService<M, N, K> tradeInfoService;
	private final CurrentPriceInfoProvider<N> currentPriceInfoProvider;
	private final InstrumentService<N> instrumentService;
	private final BaseTradingConfig tradingConfig;
	private final TradeManagementProvider<M, N, K> tradeManagementProvider;
	private final Collection<M> ignoreMe;
	private volatile boolean shouldWatch = true;
	public TradeWatcherService(TradeInfoService<M, N, K> tradeInfoService,
			CurrentPriceInfoProvider<N> currentPriceInfoProvider, InstrumentService<N> instrumentService,
			TradeManagementProvider<M, N, K> tradeManagementProvider, BaseTradingConfig tradingConfig,
			Collection<M> ignoreMe) {
		this.tradeInfoService = tradeInfoService;
		this.currentPriceInfoProvider = currentPriceInfoProvider;
		this.instrumentService = instrumentService;
		this.tradingConfig = tradingConfig;
		this.tradeManagementProvider = tradeManagementProvider;
		this.ignoreMe = ignoreMe;
	}

	@Subscribe
	@AllowConcurrentEvents
	@Override
	public synchronized void toggleService(Boolean shouldWatch) {
		this.shouldWatch = shouldWatch;
	}

	// called by scheduler
	public void watch() {
		if (!shouldWatch) {
			return;
		}
		Collection<Trade<M, N, K>> allTrades = this.tradeInfoService.getAllTrades();
		final Map<TradeableInstrument<N>, Collection<Trade<M, N, K>>> instrumentTradeMap = convertToMapView(allTrades);
		final Map<TradeableInstrument<N>, Price<N>> currentInstrumentPricesMap = this.currentPriceInfoProvider
				.getCurrentPricesForInstruments(instrumentTradeMap.keySet());

		instrumentTradeMap.forEach((instrument, trades) -> {
			final Price<N> spotPrice = currentInstrumentPricesMap.get(instrument);
			trades.parallelStream().filter(trade -> eligibleTrades(instrument, spotPrice).test(trade))
					.forEach(trade -> {
				double stopLossPrice = trade.getStopLoss();
				double profitTarget = tradingConfig.getMinProfitTarget();
				final double pipForInstrument = instrumentService.getPipForInstrument(instrument);
				if (stopLossPrice == 0.0) {
					setTrailingStop(trade, spotPrice, profitTarget, pipForInstrument);
				} else {
					double delta = 0.0;
					if (trade.getSide() == TradingSignal.LONG) {
						delta = (spotPrice.getBidPrice() - stopLossPrice) / pipForInstrument;
					} else {
						delta = (stopLossPrice - spotPrice.getAskPrice()) / pipForInstrument;
					}
					if (delta > profitTarget) {
						setTrailingStop(trade, spotPrice, profitTarget, pipForInstrument);
					}
				}

			});
		});
	}

	private void setTrailingStop(Trade<M, N, K> trade, final Price<N> spotPrice, double profitTarget,
			double pipForInstrument) {
		double stopLossPrice = trade.getSide() == TradingSignal.LONG
				? (spotPrice.getBidPrice() - profitTarget * pipForInstrument)
				: (spotPrice.getAskPrice() + profitTarget * pipForInstrument);
		stopLossPrice = TradingUtils.round(stopLossPrice, TradingUtils.decimalPlaces(pipForInstrument));
		tradeManagementProvider.modifyTrade(trade.getAccountId(), trade.getTradeId(), stopLossPrice, 0.0);
		tradeInfoService.refreshTradesForAccount(trade.getAccountId());
	}

	private Predicate<Trade<M, N, K>> eligibleTrades(final TradeableInstrument<N> instrument, final Price<N> price) {
		return new Predicate<Trade<M, N, K>>() {

			@Override
			public boolean test(Trade<M, N, K> trade) {
				if (ignoreMe.contains(trade.getTradeId())) {
					return false;
				}
				double pipsWon = calculatePipsWon(trade, price, instrument);
				if (pipsWon > tradingConfig.getMinProfitTarget()) {
					return true;
				} else {
					return false;
				}
			}
		};
	}

	private double calculatePipsWon(final Trade<M, N, K> trade, final Price<N> price,
			TradeableInstrument<N> instrument) {
		double pipsWon = 0.0;
		final double pipForInstrument = instrumentService.getPipForInstrument(instrument);
		if (trade.getSide() == TradingSignal.LONG && price.getBidPrice() > trade.getExecutionPrice()) {
			pipsWon = (price.getBidPrice() - trade.getExecutionPrice()) / pipForInstrument;
		} else if (trade.getSide() == TradingSignal.SHORT && price.getAskPrice() < trade.getExecutionPrice()) {
			pipsWon = (trade.getExecutionPrice() - price.getAskPrice()) / pipForInstrument;
		}
		return pipsWon;
	}

	Map<TradeableInstrument<N>, Collection<Trade<M, N, K>>> convertToMapView(
			Collection<Trade<M, N, K>> allTrades) {
		final Map<TradeableInstrument<N>, Collection<Trade<M, N, K>>> instrumentTradeMap = Maps.newHashMap();

		allTrades.stream().forEach(trade -> {
			Collection<Trade<M, N, K>> tradesCollection = null;
			TradeableInstrument<N> instrument = trade.getInstrument();
			if (instrumentTradeMap.containsKey(instrument)) {
				tradesCollection = instrumentTradeMap.get(instrument);
			} else {
				tradesCollection = Lists.newArrayList();
				instrumentTradeMap.put(instrument, tradesCollection);
			}
			tradesCollection.add(trade);
		});

		return instrumentTradeMap;
	}
}
