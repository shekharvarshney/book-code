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
import org.joda.time.DateTime;

import com.precioustech.fxtrading.instrument.TradeableInstrument;

public class MovingAverageCalculationService<T> {

	private final HistoricMarketDataProvider<T> historicMarketDataProvider;

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
}
