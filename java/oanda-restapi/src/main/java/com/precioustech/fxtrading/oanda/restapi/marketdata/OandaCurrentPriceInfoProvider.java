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
package com.precioustech.fxtrading.oanda.restapi.marketdata;

import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.ask;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.bid;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.time;

import java.util.Collection;
import java.util.Map;

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

import com.google.common.collect.Maps;
import com.precioustech.fxtrading.TradingConstants;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.CurrentPriceInfoProvider;
import com.precioustech.fxtrading.marketdata.Price;
import com.precioustech.fxtrading.oanda.restapi.OandaConstants;
import com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys;
import com.precioustech.fxtrading.oanda.restapi.utils.OandaUtils;
import com.precioustech.fxtrading.utils.TradingUtils;

public class OandaCurrentPriceInfoProvider implements CurrentPriceInfoProvider<String> {

	private static final Logger LOG = Logger.getLogger(OandaCurrentPriceInfoProvider.class);

	private final String url;
	private final BasicHeader authHeader;

	public OandaCurrentPriceInfoProvider(String url, String accessToken) {
		this.url = url;
		this.authHeader = OandaUtils.createAuthHeader(accessToken);
	}

	CloseableHttpClient getHttpClient() {
		return HttpClientBuilder.create().build();
	}

	@Override
	public Map<TradeableInstrument<String>, Price<String>> getCurrentPricesForInstruments(
			Collection<TradeableInstrument<String>> instruments) {
		StringBuilder instrumentCsv = new StringBuilder();
		boolean firstTime = true;
		for (TradeableInstrument<String> instrument : instruments) {
			if (firstTime) {
				firstTime = false;
			} else {
				instrumentCsv.append(TradingConstants.ENCODED_COMMA);
			}
			instrumentCsv.append(instrument.getInstrument());
		}

		Map<TradeableInstrument<String>, Price<String>> pricesMap = Maps.newHashMap();
		CloseableHttpClient httpClient = getHttpClient();
		try {
			HttpUriRequest httpGet = new HttpGet(this.url + OandaConstants.PRICES_RESOURCE + "?instruments="
					+ instrumentCsv.toString());
			httpGet.setHeader(this.authHeader);
			httpGet.setHeader(OandaConstants.UNIX_DATETIME_HEADER);
			LOG.info(TradingUtils.executingRequestMsg(httpGet));
			HttpResponse resp = httpClient.execute(httpGet);
			String strResp = TradingUtils.responseToString(resp);
			if (strResp != StringUtils.EMPTY) {
				Object obj = JSONValue.parse(strResp);
				JSONObject jsonResp = (JSONObject) obj;
				JSONArray prices = (JSONArray) jsonResp.get(OandaJsonKeys.prices);
				for (Object price : prices) {
					JSONObject trade = (JSONObject) price;
					Long priceTime = Long.parseLong(trade.get(time).toString());
					TradeableInstrument<String> instrument = new TradeableInstrument<String>((String) trade
							.get(OandaJsonKeys.instrument));
					Price<String> pi = new Price<String>(instrument, ((Number) trade.get(bid)).doubleValue(),
							((Number) trade.get(ask)).doubleValue(), new DateTime(TradingUtils
									.toMillisFromNanos(priceTime)));
					pricesMap.put(instrument, pi);
				}
			} else {
				TradingUtils.printErrorMsg(resp);
			}
		} catch (Exception ex) {
			LOG.error(ex);
		} finally {
			TradingUtils.closeSilently(httpClient);
		}
		return pricesMap;
	}

}
