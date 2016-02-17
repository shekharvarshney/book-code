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
package com.precioustech.fxtrading.oanda.restapi.marketdata.historic;

import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.historic.CandleStick;
import com.precioustech.fxtrading.marketdata.historic.CandleStickGranularity;
import com.precioustech.fxtrading.marketdata.historic.HistoricMarketDataProvider;

public class HistoricMarketDataProviderDemo {

	private static final Logger LOG = Logger.getLogger(HistoricMarketDataProviderDemo.class);

	private static void usage(String[] args) {
		if (args.length != 2) {
			LOG.error("Usage: HistoricMarketDataProviderDemo <url> <accesstoken>");
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		usage(args);
		final String url = args[0];
		final String accessToken = args[1];
		HistoricMarketDataProvider<String> historicMarketDataProvider = new OandaHistoricMarketDataProvider(url,
				accessToken);
		TradeableInstrument<String> usdchf = new TradeableInstrument<String>("USD_CHF");
		List<CandleStick<String>> candlesUsdChf = historicMarketDataProvider.getCandleSticks(usdchf,
				CandleStickGranularity.D, 15);
		LOG.info(String.format("++++++++++++++++++ Last %d Candle Sticks with Daily Granularity for %s ++++++++++ ",
				candlesUsdChf.size(), usdchf.getInstrument()));

		for (CandleStick<String> candle : candlesUsdChf) {
			LOG.info(candle);
		}
		TradeableInstrument<String> gbpaud = new TradeableInstrument<String>("GBP_AUD");
		DateTime from = new DateTime(1420070400000L);// 01 Jan 2015
		DateTime to = new DateTime(1451606400000L);// 01 Jan 2016
		List<CandleStick<String>> candlesGbpAud = historicMarketDataProvider.getCandleSticks(gbpaud,
				CandleStickGranularity.M, from, to);

		LOG.info(String.format("+++++++++++Candle Sticks From %s To %s with Monthly Granularity for %s ++++++++++ ",
				from, to, gbpaud.getInstrument()));
		for (CandleStick<String> candle : candlesGbpAud) {
			LOG.info(candle);
		}

	}

}
