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

import static org.apache.commons.lang3.StringUtils.center;
import static org.apache.commons.lang3.StringUtils.repeat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.DateTime;

import com.precioustech.fxtrading.TradingConstants;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.historic.CandleStick;
import com.precioustech.fxtrading.marketdata.historic.CandleStickGranularity;
import com.precioustech.fxtrading.marketdata.historic.HistoricMarketDataProvider;

public class HistoricMarketDataOnDemandDemo {

	private static final String datefmtLabel = "dd/mm/yyyy hh24:mi:ss";
	private static final int priceColLen = 10;
	private static final int timeColLen = 25;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	private static void usage(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: HistoricMarketDataOnDemandDemo <url> <accesstoken>");
			System.exit(1);
		}
	}

	public static void main(String[] args) throws Exception {
		usage(args);
		final String url = args[0];
		final String accessToken = args[1];
		HistoricMarketDataProvider<String> historicMarketDataProvider = new OandaHistoricMarketDataProvider(url,
				accessToken);
		Scanner scanner = new Scanner(System.in);
		scanner.useDelimiter(System.getProperty("line.separator"));
		System.out.print("Instrument" + TradingConstants.COLON);
		String ccyPair = scanner.next();
		TradeableInstrument<String> instrument = new TradeableInstrument<>(ccyPair.toUpperCase());

		System.out.print("Granularity" + ArrayUtils.toString(CandleStickGranularity.values()) + TradingConstants.COLON);
		CandleStickGranularity granularity = CandleStickGranularity.valueOf(scanner.next().toUpperCase());
		System.out.print("Time Range Candles(t) or Last N Candles(n)?:");
		String choice = scanner.next();

		List<CandleStick<String>> candles = null;
		if ("t".equalsIgnoreCase(choice)) {
			System.out.print("Start Time(" + datefmtLabel + ")" + TradingConstants.COLON);
			String startStr = scanner.next();
			Date startDt = sdf.parse(startStr);
			System.out.print("  End Time(" + datefmtLabel + ")" + TradingConstants.COLON);
			String endStr = scanner.next();
			Date endDt = sdf.parse(endStr);
			candles = historicMarketDataProvider.getCandleSticks(instrument, granularity,
					new DateTime(startDt.getTime()), new DateTime(endDt.getTime()));
		} else {
			System.out.print("Last how many candles?" + TradingConstants.COLON);
			int n = scanner.nextInt();
			candles = historicMarketDataProvider.getCandleSticks(instrument, granularity, n);
		}
		System.out.println(center("Time", timeColLen) + center("Open", priceColLen) + center("Close", priceColLen)
				+ center("High", priceColLen) + center("Low", priceColLen));
		System.out.println(repeat("=", timeColLen + priceColLen * 4));
		for (CandleStick<String> candle : candles) {
			System.out.println(center(sdf.format(candle.getEventDate().toDate()), timeColLen)
					+ formatPrice(candle.getOpenPrice()) + formatPrice(candle.getClosePrice())
					+ formatPrice(candle.getHighPrice()) + formatPrice(candle.getLowPrice()));
		}
		scanner.close();
	}

	private static String formatPrice(double price) {
		return center(String.format("%3.5f", price), priceColLen);
	}

}
