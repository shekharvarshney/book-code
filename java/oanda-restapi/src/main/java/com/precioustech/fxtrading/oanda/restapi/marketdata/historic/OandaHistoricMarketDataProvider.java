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

import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.candles;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.closeMid;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.highMid;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.lowMid;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.openMid;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.time;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.collect.Lists;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.historic.CandleStick;
import com.precioustech.fxtrading.marketdata.historic.CandleStickGranularity;
import com.precioustech.fxtrading.marketdata.historic.HistoricMarketDataProvider;
import com.precioustech.fxtrading.oanda.restapi.OandaConstants;
import com.precioustech.fxtrading.oanda.restapi.utils.OandaUtils;
import com.precioustech.fxtrading.utils.TradingUtils;

public class OandaHistoricMarketDataProvider implements HistoricMarketDataProvider<String> {

	private final String url;
	private final BasicHeader authHeader;
	private static final String tzLondon = "Europe%2FLondon";
	private static final Logger LOG = Logger.getLogger(OandaHistoricMarketDataProvider.class);

	public OandaHistoricMarketDataProvider(String url, String accessToken) {
		this.url = url;
		this.authHeader = OandaUtils.createAuthHeader(accessToken);
	}

	String getFromToUrl(TradeableInstrument<String> instrument, CandleStickGranularity granularity, DateTime from,
			DateTime to) {
		return String
				.format("%s%s?instrument=%s&candleFormat=midpoint&granularity=%s&dailyAlignment=0&alignmentTimezone=%s&start=%d&end=%d",
						this.url, OandaConstants.CANDLES_RESOURCE, instrument.getInstrument(), granularity.name(),
						tzLondon, TradingUtils.toUnixTime(from), TradingUtils.toUnixTime(to));
	}

	String getCountUrl(TradeableInstrument<String> instrument, CandleStickGranularity granularity, int count) {
		return String
				.format("%s%s?instrument=%s&candleFormat=midpoint&granularity=%s&dailyAlignment=0&alignmentTimezone=%s&count=%d",
						this.url, OandaConstants.CANDLES_RESOURCE, instrument.getInstrument(), granularity.name(),
						tzLondon, count);
	}

	@Override
	public List<CandleStick<String>> getCandleSticks(TradeableInstrument<String> instrument,
			CandleStickGranularity granularity, DateTime from, DateTime to) {
		return getCandleSticks(instrument, getFromToUrl(instrument, granularity, from, to), granularity);
	}

	@Override
	public List<CandleStick<String>> getCandleSticks(TradeableInstrument<String> instrument,
			CandleStickGranularity granularity, int count) {

		return getCandleSticks(instrument, getCountUrl(instrument, granularity, count), granularity);
	}

	CloseableHttpClient getHttpClient() {
		return HttpClientBuilder.create().build();
	}

	private List<CandleStick<String>> getCandleSticks(TradeableInstrument<String> instrument, String url,
			CandleStickGranularity granularity) {
		List<CandleStick<String>> allCandleSticks = Lists.newArrayList();
		CloseableHttpClient httpClient = getHttpClient();
		try {
			HttpUriRequest httpGet = new HttpGet(url);
			httpGet.setHeader(authHeader);
			httpGet.setHeader(OandaConstants.UNIX_DATETIME_HEADER);
			LOG.info(TradingUtils.executingRequestMsg(httpGet));
			HttpResponse resp = httpClient.execute(httpGet);
			String strResp = TradingUtils.responseToString(resp);
			if (strResp != StringUtils.EMPTY) {
				Object obj = JSONValue.parse(strResp);
				JSONObject jsonResp = (JSONObject) obj;
				JSONArray candlsticks = (JSONArray) jsonResp.get(candles);

				for (Object o : candlsticks) {
					JSONObject candlestick = (JSONObject) o;

					final double openPrice = ((Number) candlestick.get(openMid)).doubleValue();
					final double highPrice = ((Number) candlestick.get(highMid)).doubleValue();
					final double lowPrice = ((Number) candlestick.get(lowMid)).doubleValue();
					final double closePrice = ((Number) candlestick.get(closeMid)).doubleValue();
					final long timestamp = Long.parseLong(candlestick.get(time).toString());

					CandleStick<String> candle = new CandleStick<String>(openPrice, highPrice, lowPrice, closePrice,
							new DateTime(TradingUtils.toMillisFromNanos(timestamp)), instrument, granularity);
					allCandleSticks.add(candle);
				}
			} else {
				TradingUtils.printErrorMsg(resp);
			}
		} catch (Exception e) {
			LOG.error(e);
		} finally {
			TradingUtils.closeSilently(httpClient);
		}
		return allCandleSticks;
	}

}
