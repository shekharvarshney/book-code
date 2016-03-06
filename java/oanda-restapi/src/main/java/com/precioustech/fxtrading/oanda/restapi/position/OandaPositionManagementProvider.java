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
package com.precioustech.fxtrading.oanda.restapi.position;

import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.avgPrice;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.instrument;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.positions;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.side;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.units;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.collect.Lists;
import com.precioustech.fxtrading.TradingConstants;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.oanda.restapi.OandaConstants;
import com.precioustech.fxtrading.oanda.restapi.utils.OandaUtils;
import com.precioustech.fxtrading.position.Position;
import com.precioustech.fxtrading.position.PositionManagementProvider;
import com.precioustech.fxtrading.utils.TradingUtils;

public class OandaPositionManagementProvider implements PositionManagementProvider<String, Long> {

	private static final Logger LOG = Logger.getLogger(OandaPositionManagementProvider.class);

	private final String url;
	private final BasicHeader authHeader;
	private static final String positionsResource = "/positions";

	public OandaPositionManagementProvider(String url, String accessToken) {
		this.url = url;
		this.authHeader = OandaUtils.createAuthHeader(accessToken);
	}

	CloseableHttpClient getHttpClient() {
		return HttpClientBuilder.create().build();
	}

	private Position<String> parsePositionInfo(JSONObject accPosition) {
		Position<String> positionInfo = new Position<String>(new TradeableInstrument<String>((String) accPosition
				.get(instrument)), (Long) accPosition.get(units), OandaUtils.toTradingSignal((String) accPosition
				.get(side)), (Double) accPosition.get(avgPrice));
		return positionInfo;
	}

	String getPositionsForAccountUrl(Long accountId) {
		return this.url + OandaConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId + positionsResource;
	}

	@Override
	public Collection<Position<String>> getPositionsForAccount(Long accountId) {
		Collection<Position<String>> allPositions = Lists.newArrayList();
		CloseableHttpClient httpClient = getHttpClient();
		try {
			String strResp = getResponseAsString(getPositionsForAccountUrl(accountId), httpClient);
			if (strResp != StringUtils.EMPTY) {
				Object obj = JSONValue.parse(strResp);
				JSONObject jsonResp = (JSONObject) obj;
				JSONArray accPositions = (JSONArray) jsonResp.get(positions);
				for (Object o : accPositions) {
					JSONObject accPosition = (JSONObject) o;
					Position<String> positionInfo = parsePositionInfo(accPosition);
					allPositions.add(positionInfo);
				}
			}
		} catch (Exception ex) {
			LOG.error("error encountered whilst fetching positions for account:" + accountId, ex);
		} finally {
			TradingUtils.closeSilently(httpClient);
		}
		return allPositions;
	}

	private String getResponseAsString(String reqUrl, CloseableHttpClient httpClient) throws Exception {
		HttpUriRequest httpGet = new HttpGet(reqUrl);
		httpGet.setHeader(this.authHeader);
		LOG.info(TradingUtils.executingRequestMsg(httpGet));
		HttpResponse resp = httpClient.execute(httpGet);
		String strResp = TradingUtils.responseToString(resp);
		return strResp;
	}

	String getPositionForInstrumentUrl(Long accountId, TradeableInstrument<String> instrument) {
		return this.url + OandaConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId + positionsResource
				+ TradingConstants.FWD_SLASH + instrument.getInstrument();
	}

	@Override
	public boolean closePosition(Long accountId, TradeableInstrument<String> instrument) {
		CloseableHttpClient httpClient = getHttpClient();
		try {
			HttpDelete httpDelete = new HttpDelete(getPositionForInstrumentUrl(accountId, instrument));
			httpDelete.setHeader(authHeader);
			LOG.info(TradingUtils.executingRequestMsg(httpDelete));
			HttpResponse resp = httpClient.execute(httpDelete);
			int httpCode = resp.getStatusLine().getStatusCode();
			if (httpCode == HttpStatus.SC_OK) {
				LOG.info(String.format("Position successfully closed for instrument %s and account %d", instrument
						.getInstrument(), accountId));
				return true;
			} else {
				LOG.warn(String.format(
						"Position for instrument %s and account %d not closed. Encountered error code=%d", instrument
								.getInstrument(), accountId, httpCode));
			}
		} catch (Exception ex) {
			LOG.error(String.format("error encountered whilst closing position for instrument %s and account %d",
					instrument.getInstrument(), accountId), ex);
		} finally {
			TradingUtils.closeSilently(httpClient);
		}
		return false;
	}

	@Override
	public Position<String> getPositionForInstrument(Long accountId, TradeableInstrument<String> instrument) {
		CloseableHttpClient httpClient = getHttpClient();
		try {
			String strResp = getResponseAsString(getPositionForInstrumentUrl(accountId, instrument), httpClient);
			if (strResp != StringUtils.EMPTY) {
				return parsePositionInfo((JSONObject) JSONValue.parse(strResp));
			}
		} catch (Exception ex) {
			LOG.error(String.format("error encountered whilst fetching position for instrument %s and account %d",
					instrument.getInstrument(), accountId), ex);
		} finally {
			TradingUtils.closeSilently(httpClient);
		}
		return null;
	}
}
