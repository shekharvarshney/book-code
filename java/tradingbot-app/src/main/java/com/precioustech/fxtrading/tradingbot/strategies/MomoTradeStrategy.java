/*
 *  Copyright 2016 Shekhar Varshney
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.precioustech.fxtrading.tradingbot.strategies;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import javax.annotation.Resource;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;
import com.precioustech.fxtrading.TradingDecision;
import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.instrument.InstrumentService;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.CurrentPriceInfoProvider;
import com.precioustech.fxtrading.marketdata.Price;
import com.precioustech.fxtrading.marketdata.historic.CandleStickGranularity;
import com.precioustech.fxtrading.marketdata.historic.MovingAverageCalculationService;
import com.precioustech.fxtrading.trade.strategies.TradingStrategy;
import com.precioustech.fxtrading.utils.TradingUtils;

@TradingStrategy
public class MomoTradeStrategy<T> {
	@Autowired
	private MovingAverageCalculationService<T> movingAverageCalculationService;
	@Resource(name = "orderQueue")
	BlockingQueue<TradingDecision<T>> orderQueue;
	private final Collection<TradeableInstrument<T>> instruments;
	@Autowired
	private CurrentPriceInfoProvider<T> currentPriceInfoProvider;
	@Autowired
	InstrumentService<T> instrumentService;
	private Set<TradeableInstrument<T>> longCandidates = Sets.newHashSet();
	private Set<TradeableInstrument<T>> shortCandidates = Sets.newHashSet();
	private static double PIPS_DIST_EMA = 10.0;// TODO: in config??
	private static final Logger LOG = Logger.getLogger(MomoTradeStrategy.class);

	public MomoTradeStrategy(Collection<TradeableInstrument<T>> instruments) {
		this.instruments = instruments;
	}

	// called by the scheduler
	public synchronized void analyseAndTrade() {
		Map<TradeableInstrument<T>, Price<T>> priceMap = this.currentPriceInfoProvider
				.getCurrentPricesForInstruments(instruments);
		priceMap.entrySet().stream().forEach(entry -> doMomoAnalysis(entry));
	}

	private static final CandleStickGranularity MIN5 = CandleStickGranularity.M5;

	private void doMomoAnalysis(Entry<TradeableInstrument<T>, Price<T>> entry) {
		final TradeableInstrument<T> instrument = entry.getKey();
		final Price<T> price = entry.getValue();
		Pair<Double, Double> macdEmaAsPair = movingAverageCalculationService.macdAndEmaAsPair(instrument, MIN5,
				MovingAverageCalculationService.SLOW_MACD_LINE, MovingAverageCalculationService.FAST_MACD_LINE, 20);
		double ema20 = macdEmaAsPair.getRight();
		double macd = macdEmaAsPair.getLeft();
		double avgPrice = (price.getBidPrice() + price.getAskPrice()) / 2;
		TradingSignal signal = null;
		Double pipForInstrument = null;
		Double limitPrice = null;
		if (avgPrice < ema20 && macd < 0) {
			if (shortCandidates.contains(instrument)) {
				shortCandidates.remove(instrument);
				pipForInstrument = instrumentService.getPipForInstrument(instrument);
				signal = TradingSignal.SHORT;
				limitPrice = ema20 - pipForInstrument * PIPS_DIST_EMA;
			} else {
				longCandidates.add(instrument);
			}
		} else if (avgPrice > ema20 && macd > 0) {
			if (longCandidates.contains(instrument)) {
				longCandidates.remove(instrument);
				signal = TradingSignal.LONG;
				pipForInstrument = instrumentService.getPipForInstrument(instrument);
				limitPrice = ema20 + pipForInstrument * PIPS_DIST_EMA;
			} else {
				shortCandidates.add(instrument);
			}
		}
		if (signal != null) {
			limitPrice = TradingUtils.round(limitPrice, TradingUtils.decimalPlaces(pipForInstrument));
			orderQueue.offer(new TradingDecision<T>(entry.getKey(), signal, 0.0, 0.0, limitPrice,
					TradingDecision.SRCDECISION.MOMO));
			LOG.info(String.format(
					"MOMO Trade requested. ema20=%2.5f, macd=%2.5f, price=%2.5f, limit=%2.5f, instrument=%s, signal=%s",
					ema20, macd, avgPrice, limitPrice, instrument.getInstrument(), signal.name()));
		}
	}

}
