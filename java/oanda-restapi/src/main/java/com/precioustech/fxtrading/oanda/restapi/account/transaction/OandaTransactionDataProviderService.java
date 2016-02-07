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
package com.precioustech.fxtrading.oanda.restapi.account.transaction;

import static com.precioustech.fxtrading.oanda.restapi.OandaConstants.ACCOUNTS_RESOURCE;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.time;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.precioustech.fxtrading.TradingConstants;
import com.precioustech.fxtrading.account.transaction.Transaction;
import com.precioustech.fxtrading.account.transaction.TransactionDataProvider;
import com.precioustech.fxtrading.oanda.restapi.OandaConstants;
import com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys;
import com.precioustech.fxtrading.oanda.restapi.utils.OandaUtils;
import com.precioustech.fxtrading.utils.TradingUtils;

public class OandaTransactionDataProviderService implements TransactionDataProvider<Long, Long, String> {
	private static final Logger LOG = Logger.getLogger(OandaTransactionDataProviderService.class);

	private final String url;
	private final BasicHeader authHeader;

	public OandaTransactionDataProviderService(final String url, final String accessToken) {
		this.url = url;
		this.authHeader = OandaUtils.createAuthHeader(accessToken);
	}

	CloseableHttpClient getHttpClient() {
		return HttpClientBuilder.create().build();
	}

	String getSingleAccountTransactionUrl(Long transactionId, Long accountId) {
		return url + ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId + TradingConstants.FWD_SLASH
				+ "transactions" + TradingConstants.FWD_SLASH
				+ transactionId;
	}

	@Override
	public Transaction<Long, Long, String> getTransaction(Long transactionId, Long accountId) {
		CloseableHttpClient httpClient = getHttpClient();
		try {
			HttpUriRequest httpGet = new HttpGet(getSingleAccountTransactionUrl(transactionId, accountId));
			httpGet.setHeader(authHeader);
			httpGet.setHeader(OandaConstants.UNIX_DATETIME_HEADER);

			LOG.info(TradingUtils.executingRequestMsg(httpGet));
			HttpResponse httpResponse = httpClient.execute(httpGet);
			String strResp = TradingUtils.responseToString(httpResponse);
			if (strResp != StringUtils.EMPTY) {
				JSONObject transactionJson = (JSONObject) JSONValue.parse(strResp);
				final String instrument = transactionJson.get(OandaJsonKeys.instrument).toString();
				final String type = transactionJson.get(OandaJsonKeys.type).toString();
				final long tradeUnits = (Long) transactionJson.get(OandaJsonKeys.units);
				final long timestamp = Long.parseLong(transactionJson.get(time).toString());
				final double pnl = ((Number) transactionJson.get(OandaJsonKeys.pl)).doubleValue();
				final double interest = ((Number) transactionJson.get(OandaJsonKeys.interest)).doubleValue();
				final double price = ((Number) transactionJson.get(OandaJsonKeys.price)).doubleValue();
				final String side = transactionJson.get(OandaJsonKeys.side).toString();
				return new Transaction<Long, Long, String>(transactionId, OandaUtils.toOandaTransactionType(type),
						accountId, instrument, tradeUnits,
						OandaUtils.toTradingSignal(side),
						new DateTime(TradingUtils.toMillisFromNanos(timestamp)), price, interest, pnl);
			} else {
				TradingUtils.printErrorMsg(httpResponse);
			}
		} catch (Exception e) {
			LOG.error(e);
		}
		return null;
	}

}
