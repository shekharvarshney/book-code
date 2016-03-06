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
package com.precioustech.fxtrading.oanda.restapi.account;

import static com.precioustech.fxtrading.oanda.restapi.OandaConstants.ACCOUNTS_RESOURCE;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.accountCurrency;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.accountId;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.balance;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.marginAvail;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.marginRate;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.marginUsed;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.openTrades;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.realizedPl;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.unrealizedPl;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
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
import com.precioustech.fxtrading.account.Account;
import com.precioustech.fxtrading.account.AccountDataProvider;
import com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys;
import com.precioustech.fxtrading.oanda.restapi.utils.OandaUtils;
import com.precioustech.fxtrading.utils.TradingUtils;

public class OandaAccountDataProviderService implements AccountDataProvider<Long> {

	private static final Logger LOG = Logger.getLogger(OandaAccountDataProviderService.class);

	private final String url;
	private final String userName;
	private final BasicHeader authHeader;

	public OandaAccountDataProviderService(final String url, final String userName, final String accessToken) {
		this.url = url;
		this.userName = userName;
		this.authHeader = OandaUtils.createAuthHeader(accessToken);
	}

	CloseableHttpClient getHttpClient() {
		return HttpClientBuilder.create().build();
	}

	String getSingleAccountUrl(Long accountId) {
		return url + ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId;
	}

	String getAllAccountsUrl() {
		return this.url + ACCOUNTS_RESOURCE + "?username=" + this.userName;
	}

	private Account<Long> getLatestAccountInfo(final Long accountId, CloseableHttpClient httpClient) {
		try {
			HttpUriRequest httpGet = new HttpGet(getSingleAccountUrl(accountId));
			httpGet.setHeader(authHeader);

			LOG.info(TradingUtils.executingRequestMsg(httpGet));
			HttpResponse httpResponse = httpClient.execute(httpGet);
			String strResp = TradingUtils.responseToString(httpResponse);
			if (strResp != StringUtils.EMPTY) {
				Object obj = JSONValue.parse(strResp);
				JSONObject accountJson = (JSONObject) obj;

				/*Parse JSON response for account information*/
				final double accountBalance = ((Number) accountJson.get(balance)).doubleValue();
				final double accountUnrealizedPnl = ((Number) accountJson.get(unrealizedPl)).doubleValue();
				final double accountRealizedPnl = ((Number) accountJson.get(realizedPl)).doubleValue();
				final double accountMarginUsed = ((Number) accountJson.get(marginUsed)).doubleValue();
				final double accountMarginAvailable = ((Number) accountJson.get(marginAvail)).doubleValue();
				final Long accountOpenTrades = (Long) accountJson.get(openTrades);
				final String accountBaseCurrency = (String) accountJson.get(accountCurrency);
				final Double accountLeverage = (Double) accountJson.get(marginRate);

				Account<Long> accountInfo = new Account<Long>(accountBalance, accountUnrealizedPnl, accountRealizedPnl,
						accountMarginUsed, accountMarginAvailable, accountOpenTrades, accountBaseCurrency, accountId,
						accountLeverage);

				return accountInfo;
			} else {
				TradingUtils.printErrorMsg(httpResponse);
			}
		} catch (Exception e) {
			LOG.error("Exception encountered whilst getting info for account:" + accountId, e);
		}
		return null;
	}

	@Override
	public Account<Long> getLatestAccountInfo(final Long accountId) {
		CloseableHttpClient httpClient = getHttpClient();
		try {
			return getLatestAccountInfo(accountId, httpClient);
		} finally {
			TradingUtils.closeSilently(httpClient);
		}
	}

	@Override
	public Collection<Account<Long>> getLatestAccountInfo() {
		CloseableHttpClient httpClient = getHttpClient();
		List<Account<Long>> accInfos = Lists.newArrayList();
		try {
			HttpUriRequest httpGet = new HttpGet(getAllAccountsUrl());
			httpGet.setHeader(this.authHeader);

			LOG.info(TradingUtils.executingRequestMsg(httpGet));
			HttpResponse resp = httpClient.execute(httpGet);
			String strResp = TradingUtils.responseToString(resp);
			if (strResp != StringUtils.EMPTY) {
				Object jsonObject = JSONValue.parse(strResp);
				JSONObject jsonResp = (JSONObject) jsonObject;
				JSONArray accounts = (JSONArray) jsonResp.get(OandaJsonKeys.accounts);
				/*
				 * We are doing a per account json request because not all information is returned in the array of results
				 */
				for (Object o : accounts) {
					JSONObject account = (JSONObject) o;
					Long accountIdentifier = (Long) account.get(accountId);
					Account<Long> accountInfo = getLatestAccountInfo(accountIdentifier, httpClient);
					accInfos.add(accountInfo);
				}
			} else {
				TradingUtils.printErrorMsg(resp);
			}

		} catch (Exception e) {
			LOG.error("Exception encountered while retrieving all accounts data", e);
		} finally {
			TradingUtils.closeSilently(httpClient);
		}
		return accInfos;
	}

}
