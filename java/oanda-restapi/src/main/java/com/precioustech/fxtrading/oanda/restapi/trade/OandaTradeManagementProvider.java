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
package com.precioustech.fxtrading.oanda.restapi.trade;

import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.id;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.instrument;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.price;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.side;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.stopLoss;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.takeProfit;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.time;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.trades;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.units;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.collect.Lists;
import com.precioustech.fxtrading.TradingConstants;
import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.oanda.restapi.OandaConstants;
import com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys;
import com.precioustech.fxtrading.oanda.restapi.utils.OandaUtils;
import com.precioustech.fxtrading.trade.Trade;
import com.precioustech.fxtrading.trade.TradeManagementProvider;
import com.precioustech.fxtrading.utils.TradingUtils;

public class OandaTradeManagementProvider implements TradeManagementProvider<Long, String, Long> {

	private static final Logger LOG = Logger.getLogger(OandaTradeManagementProvider.class);
	private static final String tradesResource = "/trades";

	private final String url;
	private final BasicHeader authHeader;

	public OandaTradeManagementProvider(String url, String accessToken) {
		this.url = url;
		this.authHeader = OandaUtils.createAuthHeader(accessToken);
	}

	CloseableHttpClient getHttpClient() {
		return HttpClientBuilder.create().build();
	}

	String getTradesInfoUrl(Long accountId) {
		return this.url + OandaConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId + tradesResource;
	}

	private Trade<Long, String, Long> parseTrade(JSONObject trade, Long accountId) {
		final Long tradeTime = Long.parseLong(trade.get(time).toString());
		final Long tradeId = (Long) trade.get(id);
		final Long tradeUnits = (Long) trade.get(units);
		final TradingSignal tradeSignal = OandaUtils.toTradingSignal((String) trade.get(side));
		final TradeableInstrument<String> tradeInstrument = new TradeableInstrument<String>((String) trade
				.get(instrument));
		final double tradeTakeProfit = ((Number) trade.get(takeProfit)).doubleValue();
		final double tradeExecutionPrice = ((Number) trade.get(price)).doubleValue();
		final double tradeStopLoss = ((Number) trade.get(stopLoss)).doubleValue();

		return new Trade<Long, String, Long>(tradeId, tradeUnits, tradeSignal, tradeInstrument, new DateTime(
				TradingUtils.toMillisFromNanos(tradeTime)), tradeTakeProfit, tradeExecutionPrice, tradeStopLoss,
				accountId);

	}

	@Override
	public Collection<Trade<Long, String, Long>> getTradesForAccount(Long accountId) {
		Collection<Trade<Long, String, Long>> allTrades = Lists.newArrayList();
		CloseableHttpClient httpClient = getHttpClient();
		try {
			HttpUriRequest httpGet = new HttpGet(getTradesInfoUrl(accountId));
			httpGet.setHeader(this.authHeader);
			httpGet.setHeader(OandaConstants.UNIX_DATETIME_HEADER);
			LOG.info(TradingUtils.executingRequestMsg(httpGet));
			HttpResponse resp = httpClient.execute(httpGet);
			String strResp = TradingUtils.responseToString(resp);
			if (strResp != StringUtils.EMPTY) {
				Object obj = JSONValue.parse(strResp);
				JSONObject jsonResp = (JSONObject) obj;
				JSONArray accountTrades = (JSONArray) jsonResp.get(trades);
				for (Object accountTrade : accountTrades) {
					JSONObject trade = (JSONObject) accountTrade;
					Trade<Long, String, Long> tradeInfo = parseTrade(trade, accountId);
					allTrades.add(tradeInfo);
				}
			} else {
				TradingUtils.printErrorMsg(resp);
			}
		} catch (Exception ex) {
			LOG.error("error encountered whilst fetching trades for account:" + accountId, ex);
		} finally {
			TradingUtils.closeSilently(httpClient);
		}
		return allTrades;
	}

	String getTradeForAccountUrl(Long tradeId, Long accountId) {
		return this.url + OandaConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId + tradesResource
				+ TradingConstants.FWD_SLASH + tradeId;
	}

	@Override
	public Trade<Long, String, Long> getTradeForAccount(Long tradeId, Long accountId) {
		CloseableHttpClient httpClient = getHttpClient();
		try {
			HttpUriRequest httpGet = new HttpGet(getTradeForAccountUrl(tradeId, accountId));
			httpGet.setHeader(this.authHeader);
			httpGet.setHeader(OandaConstants.UNIX_DATETIME_HEADER);
			LOG.info(TradingUtils.executingRequestMsg(httpGet));
			HttpResponse resp = httpClient.execute(httpGet);
			String strResp = TradingUtils.responseToString(resp);
			if (strResp != StringUtils.EMPTY) {
				JSONObject trade = (JSONObject) JSONValue.parse(strResp);
				return parseTrade(trade, accountId);
			} else {
				TradingUtils.printErrorMsg(resp);
			}
		} catch (Exception ex) {
			LOG.error(String.format("error encountered whilst fetching trade %d for account %d", tradeId, accountId),
					ex);
		} finally {
			TradingUtils.closeSilently(httpClient);
		}
		return null;
	}

	HttpPatch createPatchCommand(Long accountId, Long tradeId, double stopLoss, double takeProfit) throws Exception {
		HttpPatch httpPatch = new HttpPatch(getTradeForAccountUrl(tradeId, accountId));
		httpPatch.setHeader(this.authHeader);
		List<NameValuePair> params = Lists.newArrayList();
		params.add(new BasicNameValuePair(OandaJsonKeys.takeProfit, String.valueOf(takeProfit)));
		params.add(new BasicNameValuePair(OandaJsonKeys.stopLoss, String.valueOf(stopLoss)));
		httpPatch.setEntity(new UrlEncodedFormEntity(params));
		return httpPatch;
	}

	@Override
	public boolean modifyTrade(Long accountId, Long tradeId, double stopLoss, double takeProfit) {
		CloseableHttpClient httpClient = getHttpClient();
		try {
			HttpPatch httpPatch = createPatchCommand(accountId, tradeId, stopLoss, takeProfit);
			LOG.info(TradingUtils.executingRequestMsg(httpPatch));
			HttpResponse resp = httpClient.execute(httpPatch);
			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				if (resp.getEntity() != null) {
					LOG.info("Trade Modified->" + TradingUtils.responseToString(resp));
				} else {
					LOG.warn(String.format("trade %d could not be modified with stop loss %3.5f", tradeId, stopLoss));
				}

				return true;
			} else {
				LOG.warn(String.format(
						"trade %d could not be modified with stop loss %3.5f and take profit %3.5f. http code=%d",
						tradeId, stopLoss, takeProfit, resp.getStatusLine().getStatusCode()));
			}
		} catch (Exception e) {
			LOG.error(
					String.format("error while modifying trade %d to stop loss %3.5f, take profit %3.5f for account %d",
							tradeId, stopLoss, takeProfit, accountId),
					e);
		} finally {
			TradingUtils.closeSilently(httpClient);
		}
		return false;
	}

	@Override
	public boolean closeTrade(Long tradeId, Long accountId) {
		CloseableHttpClient httpClient = getHttpClient();
		try {
			HttpDelete httpDelete = new HttpDelete(getTradeForAccountUrl(tradeId, accountId));
			httpDelete.setHeader(authHeader);
			LOG.info(TradingUtils.executingRequestMsg(httpDelete));
			HttpResponse resp = httpClient.execute(httpDelete);
			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				LOG.info(String.format("Trade %d successfully closed for account %d", tradeId, accountId));
				return true;
			} else {
				LOG.warn(String.format("Trade %d could not be closed. Recd error code %d", tradeId, resp
						.getStatusLine().getStatusCode()));
			}
		} catch (Exception e) {
			LOG.warn("error deleting trade id:" + tradeId, e);
		} finally {
			TradingUtils.closeSilently(httpClient);
		}
		return false;
	}

}
