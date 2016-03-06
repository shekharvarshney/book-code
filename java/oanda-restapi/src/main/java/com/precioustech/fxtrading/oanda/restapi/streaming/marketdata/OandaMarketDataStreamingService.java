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
package com.precioustech.fxtrading.oanda.restapi.streaming.marketdata;

import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.ask;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.bid;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.disconnect;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.heartbeat;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.tick;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.time;

import java.io.BufferedReader;
import java.util.Collection;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.precioustech.fxtrading.TradingConstants;
import com.precioustech.fxtrading.heartbeats.HeartBeatCallback;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.MarketEventCallback;
import com.precioustech.fxtrading.oanda.restapi.OandaConstants;
import com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys;
import com.precioustech.fxtrading.oanda.restapi.streaming.OandaStreamingService;
import com.precioustech.fxtrading.streaming.marketdata.MarketDataStreamingService;
import com.precioustech.fxtrading.utils.TradingUtils;

public class OandaMarketDataStreamingService extends OandaStreamingService implements MarketDataStreamingService {

	private static final Logger LOG = Logger.getLogger(OandaMarketDataStreamingService.class);
	private final String url;
	private final MarketEventCallback<String> marketEventCallback;

	public OandaMarketDataStreamingService(String url, String accessToken, long accountId,
			Collection<TradeableInstrument<String>> instruments, MarketEventCallback<String> marketEventCallback,
			HeartBeatCallback<DateTime> heartBeatCallback, String heartbeatSourceId) {
		super(accessToken, heartBeatCallback, heartbeatSourceId);
		this.url = url + OandaConstants.PRICES_RESOURCE + "?accountId=" + accountId + "&instruments="
				+ instrumentsAsCsv(instruments);
		this.marketEventCallback = marketEventCallback;
	}

	private String instrumentsAsCsv(Collection<TradeableInstrument<String>> instruments) {
		StringBuilder csvLst = new StringBuilder();
		boolean firstTime = true;
		for (TradeableInstrument<String> instrument : instruments) {
			if (firstTime) {
				firstTime = false;
			} else {
				csvLst.append(TradingConstants.ENCODED_COMMA);
			}
			csvLst.append(instrument.getInstrument());
		}
		return csvLst.toString();
	}

	@Override
	protected String getStreamingUrl() {
		return this.url;
	}

	@Override
	public void stopMarketDataStreaming() {
		this.serviceUp = false;
		if (streamThread != null && streamThread.isAlive()) {
			streamThread.interrupt();
		}
	}

	@Override
	public void startMarketDataStreaming() {
		stopMarketDataStreaming();
		this.streamThread = new Thread(new Runnable() {

			@Override
			public void run() {
				CloseableHttpClient httpClient = getHttpClient();
				try {
					BufferedReader br = setUpStreamIfPossible(httpClient);
					if (br != null) {
						String line;
						while ((line = br.readLine()) != null && serviceUp) {
							Object obj = JSONValue.parse(line);
							JSONObject instrumentTick = (JSONObject) obj;
							// unwrap if necessary
							if (instrumentTick.containsKey(tick)) {
								instrumentTick = (JSONObject) instrumentTick.get(tick);
							}

							if (instrumentTick.containsKey(OandaJsonKeys.instrument)) {
								final String instrument = instrumentTick.get(OandaJsonKeys.instrument).toString();
								final String timeAsString = instrumentTick.get(time).toString();
								final long eventTime = Long.parseLong(timeAsString);
								final double bidPrice = ((Number) instrumentTick.get(bid)).doubleValue();
								final double askPrice = ((Number) instrumentTick.get(ask)).doubleValue();
								marketEventCallback.onMarketEvent(new TradeableInstrument<String>(instrument),
										bidPrice, askPrice, new DateTime(TradingUtils.toMillisFromNanos(eventTime)));
							} else if (instrumentTick.containsKey(heartbeat)) {
								handleHeartBeat(instrumentTick);
							} else if (instrumentTick.containsKey(disconnect)) {
								handleDisconnect(line);
							}
						}
						br.close();
						// stream.close();
					}
				} catch (Exception e) {
					LOG.error("error encountered inside market data streaming thread", e);
				} finally {
					serviceUp = false;
					TradingUtils.closeSilently(httpClient);
				}

			}
		}, "OandMarketDataStreamingThread");
		this.streamThread.start();

	}

	@Override
	protected void startStreaming() {
		startMarketDataStreaming();

	}

	@Override
	protected void stopStreaming() {
		stopMarketDataStreaming();

	}

}
