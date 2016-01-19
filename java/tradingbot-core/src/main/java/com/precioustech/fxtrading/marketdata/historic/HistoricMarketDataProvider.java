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

import org.joda.time.DateTime;

import com.precioustech.fxtrading.instrument.TradeableInstrument;

/**
 * A provider of candle stick data for a given instrument. The candle sticks
 * must be in chronological order in order to easily construct time series
 * information.
 * 
 * @author Shekhar Varshney
 *
 * @param <T>
 *            The type of instrumentId in class TradeableInstrument
 * @see TradeableInstrument
 */
public interface HistoricMarketDataProvider<T> {

	/**
	 * Construct candle sticks for a given from and to period.
	 * 
	 * @param instrument
	 *            , for which the candle stick information is requested
	 * @param granularity
	 *            , the time interval between 2 candle sticks
	 * @param from
	 *            , the start of first candle stick
	 * @param to
	 *            , the end of last candle stick
	 * @return List<CandleStick<T>> chronologically ordered.
	 */
	List<CandleStick<T>> getCandleSticks(TradeableInstrument<T> instrument, CandleStickGranularity granularity,
			DateTime from, DateTime to);

	/**
	 * Construct last "count" candle sticks. This could be translated to an
	 * invocation of the overloaded method above which requires "from" and "to"
	 * date, if appropriate. The "to" date = now() and "from" date = now() -
	 * granularity*count
	 * 
	 * @param instrument
	 *            , for which the candle stick information is requested
	 * @param granularity
	 *            , the time interval between 2 candle sticks
	 * @param count
	 *            ,
	 * @return List<CandleStick<T>> chronologically ordered.
	 */
	List<CandleStick<T>> getCandleSticks(TradeableInstrument<T> instrument, CandleStickGranularity granularity,
			int count);
}
