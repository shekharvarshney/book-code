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
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.code;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.highMid;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.lowMid;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.openMid;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.time;

import java.util.Collections;
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
	private static final String tzGMT = "GMT";// "Europe%2FLondon";
	private static final Logger LOG = Logger.getLogger(OandaHistoricMarketDataProvider.class);
	static final int LIMIT_ERR_CODE = 36;
	static final int MAX_CANDLES_COUNT = 5000;// max candles allowed by OANDA
												// platform

	public OandaHistoricMarketDataProvider(String url, String accessToken) {
		this.url = url;
		this.authHeader = OandaUtils.createAuthHeader(accessToken);
	}

	String getFromToUrl(TradeableInstrument<String> instrument, CandleStickGranularity granularity, DateTime from,
			DateTime to) {
		return String
				.format("%s%s?instrument=%s&candleFormat=midpoint&granularity=%s&dailyAlignment=0&alignmentTimezone=%s&start=%d&end=%d",
						this.url, OandaConstants.CANDLES_RESOURCE, instrument.getInstrument(), granularity.name(),
						tzGMT, TradingUtils.toUnixTime(from), TradingUtils.toUnixTime(to));
	}

	String getFromCountUrl(TradeableInstrument<String> instrument, CandleStickGranularity granularity, DateTime from,
			int count) {
		return String.format(
				"%s%s?instrument=%s&candleFormat=midpoint&granularity=%s&dailyAlignment=0&alignmentTimezone=%s&start=%d&count=%d",
				this.url, OandaConstants.CANDLES_RESOURCE, instrument.getInstrument(), granularity.name(), tzGMT,
				TradingUtils.toUnixTime(from), count);
	}

	String getToCountUrl(TradeableInstrument<String> instrument, CandleStickGranularity granularity, DateTime to,
			int count) {
		return String.format(
				"%s%s?instrument=%s&candleFormat=midpoint&granularity=%s&dailyAlignment=0&alignmentTimezone=%s&end=%d&count=%d",
				this.url, OandaConstants.CANDLES_RESOURCE, instrument.getInstrument(), granularity.name(), tzGMT,
				TradingUtils.toUnixTime(to), count);
	}

	String getCountUrl(TradeableInstrument<String> instrument, CandleStickGranularity granularity, int count) {
		return String
				.format("%s%s?instrument=%s&candleFormat=midpoint&granularity=%s&dailyAlignment=0&alignmentTimezone=%s&count=%d",
						this.url, OandaConstants.CANDLES_RESOURCE, instrument.getInstrument(), granularity.name(),
						tzGMT, count);
	}


	@Override
	public List<CandleStick<String>> getCandleSticks(TradeableInstrument<String> instrument,
			CandleStickGranularity granularity, DateTime from, DateTime to) {
		try {
			return getCandleSticks(instrument, getFromToUrl(instrument, granularity, from, to), granularity);
		} catch (OandaLimitExceededException leex) {
			try {
				List<CandleStick<String>> allCandles = Lists.newArrayList();
				allCandles = getCandleSticks(instrument,
						getFromCountUrl(instrument, granularity, from, MAX_CANDLES_COUNT), granularity);
				DateTime lastDate = allCandles.get(allCandles.size() - 1).getEventDate();
				long batchesReqd = (to.getMillis() - lastDate.getMillis()) / (lastDate.getMillis() - from.getMillis())
						+ 1;

				DateTime start = lastDate.plusSeconds((int) granularity.getGranularityInSeconds());

				for (long batch = 1; batch <= batchesReqd; batch++) {
					List<CandleStick<String>> batchCandles = null;
					if (batch == batchesReqd && start.isBefore(to)) {
						batchCandles = getCandleSticks(instrument, getFromToUrl(instrument, granularity, start, to),
								granularity);
					} else {
						batchCandles = getCandleSticks(instrument,
								getFromCountUrl(instrument, granularity, start, MAX_CANDLES_COUNT), granularity);
					}
					if (batchCandles == null || batchCandles.isEmpty()) {
						break;
					}
					start = batchCandles.get(batchCandles.size() - 1).getEventDate()
							.plusSeconds((int) granularity.getGranularityInSeconds());
					allCandles.addAll(batchCandles);
				}

				return allCandles;
			} catch (OandaLimitExceededException e) {
				LOG.error("limit exceedeed error encountered again", e);
			}
			return Collections.emptyList();
		}
	}

	@Override
	public List<CandleStick<String>> getCandleSticks(TradeableInstrument<String> instrument,
			CandleStickGranularity granularity, int count) {
		try {
			if (count > MAX_CANDLES_COUNT) {
				List<List<CandleStick<String>>> batchesList = Lists.newArrayList();
				List<CandleStick<String>> last5KCandles = getCandleSticks(instrument,
						getCountUrl(instrument, granularity, MAX_CANDLES_COUNT), granularity);
				if (last5KCandles.size() < MAX_CANDLES_COUNT) {
					return last5KCandles;
				} else {
					batchesList.add(last5KCandles);
					int batchesReqd = (count - MAX_CANDLES_COUNT) / MAX_CANDLES_COUNT + 1;
					DateTime endDate = last5KCandles.get(0).getEventDate();
					for (int batch = 1; batch <= batchesReqd; batch++) {
						List<CandleStick<String>> batchCandles = null;
						int batchCt = MAX_CANDLES_COUNT;
						if (batch == batchesReqd) {
							batchCt = count % MAX_CANDLES_COUNT;

						}
						batchCandles = getCandleSticks(instrument,
								getToCountUrl(instrument, granularity, endDate, batchCt), granularity);
						batchesList.add(batchCandles);
						if (batchCandles.size() < MAX_CANDLES_COUNT) {
							break;
						} else {
							endDate = batchCandles.get(0).getEventDate();
						}
					}
					List<CandleStick<String>> allBatches = Lists.newArrayList();
					for (int i = batchesList.size() - 1; i >= 0; i--) {
						allBatches.addAll(batchesList.get(i));
					}
					return allBatches;
				}
			}

			return getCandleSticks(instrument, getCountUrl(instrument, granularity, count), granularity);
		} catch (OandaLimitExceededException leex) {
			LOG.error("unexpected limit exceedeed error encountered", leex);
			return Collections.emptyList();
		}
	}

	CloseableHttpClient getHttpClient() {
		return HttpClientBuilder.create().build();
	}

	private List<CandleStick<String>> getCandleSticks(TradeableInstrument<String> instrument, String url,
			CandleStickGranularity granularity) throws OandaLimitExceededException {
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
				String errResponse = TradingUtils.getResponse(resp);
				Object obj = JSONValue.parse(errResponse);
				JSONObject jsonResp = (JSONObject) obj;
				int errCode = Integer.parseInt(jsonResp.get(code).toString());
				if (errCode == LIMIT_ERR_CODE) {
					throw new OandaLimitExceededException();
				}
				System.err.println(errResponse);
			}
		} catch (OandaLimitExceededException leex) {
			throw leex;
		} catch (Exception e) {
			LOG.error(String.format(
					"exception encountered whilst retrieving candlesticks for instrument %s with granularity %s",
					instrument.getInstrument(), granularity.getLabel()), e);
		} finally {
			TradingUtils.closeSilently(httpClient);
		}
		return allCandleSticks;
	}

}
