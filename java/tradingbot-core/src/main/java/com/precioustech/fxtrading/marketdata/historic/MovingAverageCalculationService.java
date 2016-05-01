/*
 *  Copyright 2015 Shekhar Varshney
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
package com.precioustech.fxtrading.marketdata.historic;

import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;

import com.google.common.base.Preconditions;
import com.precioustech.fxtrading.instrument.TradeableInstrument;

public class MovingAverageCalculationService<T> {

	private final HistoricMarketDataProvider<T> historicMarketDataProvider;

	public static int SLOW_MACD_LINE = 26;
	public static int FAST_MACD_LINE = 12;

	public MovingAverageCalculationService(HistoricMarketDataProvider<T> historicMarketDataProvider) {
		this.historicMarketDataProvider = historicMarketDataProvider;
	}

	public double calculateSMA(TradeableInstrument<T> instrument, int count, CandleStickGranularity granularity) {
		List<CandleStick<T>> candles = this.historicMarketDataProvider.getCandleSticks(instrument, granularity, count);
		return calculateSMA(candles);
	}

	public double calculateSMA(TradeableInstrument<T> instrument, DateTime from, DateTime to,
			CandleStickGranularity granularity) {
		List<CandleStick<T>> candles = this.historicMarketDataProvider.getCandleSticks(instrument, granularity, from,
				to);
		return calculateSMA(candles);
	}

	/*
	 * Simple average calculation of close price of candle stick
	 */
	private double calculateSMA(List<CandleStick<T>> candles) {
		double sumsma = 0;
		for (CandleStick<T> candle : candles) {
			sumsma += candle.getClosePrice();
		}
		return sumsma / candles.size();
	}

	/*
	 * Optimization to get the two together in one call
	 */
	public ImmutablePair<Double, Double> calculateSMAandWMAasPair(TradeableInstrument<T> instrument, int count,
			CandleStickGranularity granularity) {
		List<CandleStick<T>> candles = this.historicMarketDataProvider.getCandleSticks(instrument, granularity, count);
		return new ImmutablePair<Double, Double>(calculateSMA(candles), calculateWMA(candles));
	}

	public ImmutablePair<Double, Double> calculateSMAandWMAasPair(TradeableInstrument<T> instrument, DateTime from,
			DateTime to, CandleStickGranularity granularity) {
		List<CandleStick<T>> candles = this.historicMarketDataProvider.getCandleSticks(instrument, granularity, from,
				to);
		return new ImmutablePair<Double, Double>(calculateSMA(candles), calculateWMA(candles));
	}

	/*
	 * If there are N candle sticks then Mth candle stick will have weight M/(N * (N+1)/2).
	 * Therefore the divisor D for each candle is (N * (N+1)/2)
	 */
	private double calculateWMA(List<CandleStick<T>> candles) {
		double divisor = (candles.size() * (candles.size() + 1)) / 2;
		int count = 0;
		double sumwma = 0;
		for (CandleStick<T> candle : candles) {
			count++;
			sumwma += (count * candle.getClosePrice()) / divisor;
		}
		return sumwma;
	}

	private double calculateEMA(List<CandleStick<T>> candles, int N) {
		double close = candles.get(N - 1).getClosePrice();
		if (N == 1) {
			return close;
		}
		double multiplier = 2.0 / (candles.size() + 1);
		double emaPrev = calculateEMA(candles, N - 1);
		return (close - emaPrev) * multiplier + emaPrev;
	}

	public double calculateEMA(TradeableInstrument<T> instrument, int count, CandleStickGranularity granularity) {
		List<CandleStick<T>> candles = this.historicMarketDataProvider.getCandleSticks(instrument, granularity, count);
		return calculateEMA(candles, candles.size());
	}

	public double calculateEMA(TradeableInstrument<T> instrument, DateTime from, DateTime to,
			CandleStickGranularity granularity) {
		List<CandleStick<T>> candles = this.historicMarketDataProvider.getCandleSticks(instrument, granularity, from,
				to);
		return calculateEMA(candles, candles.size());
	}

	public double calculateWMA(TradeableInstrument<T> instrument, int count, CandleStickGranularity granularity) {
		List<CandleStick<T>> candles = this.historicMarketDataProvider.getCandleSticks(instrument, granularity, count);
		return calculateWMA(candles);
	}

	public double calculateWMA(TradeableInstrument<T> instrument, DateTime from, DateTime to,
			CandleStickGranularity granularity) {
		List<CandleStick<T>> candles = this.historicMarketDataProvider.getCandleSticks(instrument, granularity, from,
				to);
		return calculateWMA(candles);
	}


	public double calculateStandardMACD(TradeableInstrument<T> instrument, CandleStickGranularity granularity) {
		return calculateMACD(instrument, granularity, SLOW_MACD_LINE, FAST_MACD_LINE);
	}

	public Pair<Double, Double> macdAndEmaAsPair(TradeableInstrument<T> instrument, CandleStickGranularity granularity,
			int slowEmaCount, int fastEmaCount, int otherEma) {
		int maxCandlesReqd = Math.max(slowEmaCount, otherEma);
		List<CandleStick<T>> candles = this.historicMarketDataProvider.getCandleSticks(instrument, granularity,
				maxCandlesReqd);
		Double macdVal = null;
		Double otherEmaVal = null;
		if (maxCandlesReqd == slowEmaCount) {
			macdVal = calculateMACD(slowEmaCount, fastEmaCount, candles);
			List<CandleStick<T>> candleSubList = candles.subList(maxCandlesReqd - otherEma, candles.size());
			otherEmaVal = calculateEMA(candleSubList, candleSubList.size());
		} else {
			otherEmaVal = calculateEMA(candles, candles.size());
			List<CandleStick<T>> candleSubList = candles.subList(maxCandlesReqd - slowEmaCount, candles.size());
			macdVal = calculateMACD(slowEmaCount, fastEmaCount, candleSubList);
		}
		return new ImmutablePair<Double, Double>(macdVal, otherEmaVal);
	}

	public double calculateMACD(TradeableInstrument<T> instrument, CandleStickGranularity granularity,
			int slowEmaCount, int fastEmaCount) {
		Preconditions.checkArgument(slowEmaCount > fastEmaCount);
		List<CandleStick<T>> candles = this.historicMarketDataProvider.getCandleSticks(instrument, granularity,
				slowEmaCount);
		if(slowEmaCount > candles.size()) {
			throw new IllegalStateException(
					String.format("Expected atleast %d candles but got %d", slowEmaCount, candles.size()));
		}
		return calculateMACD(slowEmaCount, fastEmaCount, candles);
	}

	private double calculateMACD(
			int slowEmaCount, int fastEmaCount, List<CandleStick<T>> candles) {
		List<CandleStick<T>> candleSubList = candles.subList(slowEmaCount - fastEmaCount, candles.size());
		double fastEma = calculateEMA(candleSubList, candleSubList.size());
		double slowEma = calculateEMA(candles, candles.size());
		return fastEma - slowEma;

	}
}
