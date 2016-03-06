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
package com.precioustech.fxtrading.oanda.restapi.streaming.events;

import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.heartbeat;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.transaction;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.type;

import java.io.BufferedReader;
import java.util.Collection;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.precioustech.fxtrading.TradingConstants;
import com.precioustech.fxtrading.account.Account;
import com.precioustech.fxtrading.account.AccountDataProvider;
import com.precioustech.fxtrading.events.EventCallback;
import com.precioustech.fxtrading.events.EventPayLoad;
import com.precioustech.fxtrading.heartbeats.HeartBeatCallback;
import com.precioustech.fxtrading.oanda.restapi.OandaConstants;
import com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys;
import com.precioustech.fxtrading.oanda.restapi.streaming.OandaStreamingService;
import com.precioustech.fxtrading.oanda.restapi.utils.OandaUtils;
import com.precioustech.fxtrading.streaming.events.EventsStreamingService;
import com.precioustech.fxtrading.utils.TradingUtils;

public class OandaEventsStreamingService extends OandaStreamingService implements EventsStreamingService {

	private static final Logger LOG = Logger.getLogger(OandaEventsStreamingService.class);
	private final String url;
	private final AccountDataProvider<Long> accountDataProvider;
	private final EventCallback<JSONObject> eventCallback;

	public OandaEventsStreamingService(final String url, final String accessToken,
			AccountDataProvider<Long> accountDataProvider, EventCallback<JSONObject> eventCallback,
			HeartBeatCallback<DateTime> heartBeatCallback, String heartBeatSourceId) {
		super(accessToken, heartBeatCallback, heartBeatSourceId);
		this.url = url;
		this.accountDataProvider = accountDataProvider;
		this.eventCallback = eventCallback;
	}

	@Override
	public void stopEventsStreaming() {
		this.serviceUp = false;
		if (streamThread != null && streamThread.isAlive()) {
			streamThread.interrupt();
		}
	}

	private String accountsAsCsvString(Collection<Account<Long>> accounts) {
		StringBuilder accountsAsCsv = new StringBuilder();
		boolean firstTime = true;
		for (Account<Long> account : accounts) {
			if (firstTime) {
				firstTime = false;
			} else {
				accountsAsCsv.append(TradingConstants.ENCODED_COMMA);
			}
			accountsAsCsv.append(account.getAccountId());
		}
		return accountsAsCsv.toString();
	}

	@Override
	protected String getStreamingUrl() {
		Collection<Account<Long>> accounts = accountDataProvider.getLatestAccountInfo();
		return this.url + OandaConstants.EVENTS_RESOURCE + "?accountIds=" + accountsAsCsvString(accounts);
	}

	@Override
	public void startEventsStreaming() {
		stopEventsStreaming();
		streamThread = new Thread(new Runnable() {

			@Override
			public void run() {
				CloseableHttpClient httpClient = getHttpClient();
				try {
					BufferedReader br = setUpStreamIfPossible(httpClient);
					if (br != null) {
						String line;
						while ((line = br.readLine()) != null && serviceUp) {
							Object obj = JSONValue.parse(line);
							JSONObject jsonPayLoad = (JSONObject) obj;
							if (jsonPayLoad.containsKey(heartbeat)) {
								handleHeartBeat(jsonPayLoad);
							} else if (jsonPayLoad.containsKey(transaction)) {
								JSONObject transactionObject = (JSONObject) jsonPayLoad.get(transaction);
								String transactionType = transactionObject.get(type).toString();
								/*convert here so that event bus can post to an appropriate handler, 
								 * event though this does not belong here*/
								EventPayLoad<JSONObject> payLoad = OandaUtils.toOandaEventPayLoad(transactionType,
										transactionObject);
								if (payLoad != null) {
									eventCallback.onEvent(payLoad);
								}
							} else if (jsonPayLoad.containsKey(OandaJsonKeys.disconnect)) {
								handleDisconnect(line);
							}
						}
						br.close();
					}

				} catch (Exception e) {
					LOG.error("error encountered inside event streaming thread", e);
				} finally {
					serviceUp = false;
					TradingUtils.closeSilently(httpClient);
				}

			}
		}, "OandEventStreamingThread");
		streamThread.start();
	}

	@Override
	protected void startStreaming() {
		this.startEventsStreaming();
	}

	@Override
	protected void stopStreaming() {
		this.stopEventsStreaming();
	}

}
