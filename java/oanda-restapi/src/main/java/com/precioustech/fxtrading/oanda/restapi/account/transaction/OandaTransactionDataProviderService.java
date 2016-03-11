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
import static java.math.BigDecimal.ZERO;

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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.precioustech.fxtrading.TradingConstants;
import com.precioustech.fxtrading.account.transaction.Transaction;
import com.precioustech.fxtrading.account.transaction.TransactionDataProvider;
import com.precioustech.fxtrading.events.Event;
import com.precioustech.fxtrading.oanda.restapi.OandaConstants;
import com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys;
import com.precioustech.fxtrading.oanda.restapi.events.AccountEvents;
import com.precioustech.fxtrading.oanda.restapi.events.OrderEvents;
import com.precioustech.fxtrading.oanda.restapi.events.TradeEvents;
import com.precioustech.fxtrading.oanda.restapi.utils.OandaUtils;
import com.precioustech.fxtrading.utils.TradingUtils;

public class OandaTransactionDataProviderService implements TransactionDataProvider<Long, Long, String> {
	private static final Logger LOG = Logger.getLogger(OandaTransactionDataProviderService.class);

	private final String url;
	private final BasicHeader authHeader;
	private static final String TRANSACTIONS = "transactions";

	public OandaTransactionDataProviderService(final String url, final String accessToken) {
		this.url = url;
		this.authHeader = OandaUtils.createAuthHeader(accessToken);
	}

	CloseableHttpClient getHttpClient() {
		return HttpClientBuilder.create().build();
	}

	String getSingleAccountTransactionUrl(Long transactionId, Long accountId) {
		return url + ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId + TradingConstants.FWD_SLASH
				+ TRANSACTIONS + TradingConstants.FWD_SLASH + transactionId;
	}

	String getAccountMinTransactionUrl(Long minTransactionId,
			Long accountId) {/*
								 * only 50 would be returned, refer
								 * documentation
								 */
		return url + ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId + TradingConstants.FWD_SLASH
				+ TRANSACTIONS + "?minId=" + (minTransactionId + 1) + "&count=500";
	}

	@Override
	public Transaction<Long, Long, String> getTransaction(Long transactionId, Long accountId) {
		Preconditions.checkNotNull(transactionId);
		Preconditions.checkNotNull(accountId);
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
				final String instrument = transactionJson.get(OandaJsonKeys.instrument)
						.toString();/* do not use derive instrument here */
				final String type = transactionJson.get(OandaJsonKeys.type).toString();
				final Long tradeUnits = getUnits(transactionJson);
				final DateTime timestamp = getTransactionTime(transactionJson);
				final Double pnl = getPnl(transactionJson);
				final Double interest = getInterest(transactionJson);
				final Double price = getPrice(transactionJson);
				final String side = getSide(transactionJson);
				return new Transaction<Long, Long, String>(transactionId, OandaUtils.toOandaTransactionType(type),
						accountId, instrument, tradeUnits, OandaUtils.toTradingSignal(side), timestamp, price, interest,
						pnl);
			} else {
				TradingUtils.printErrorMsg(httpResponse);
			}
		} catch (Exception e) {
			LOG.error(e);
		}
		return null;
	}

	@Override
	public List<Transaction<Long, Long, String>> getTransactionsGreaterThanId(Long minTransactionId, Long accountId) {
		Preconditions.checkNotNull(minTransactionId);
		Preconditions.checkNotNull(accountId);
		CloseableHttpClient httpClient = getHttpClient();
		List<Transaction<Long, Long, String>> allTransactions = Lists.newArrayList();
		try {
			HttpUriRequest httpGet = new HttpGet(getAccountMinTransactionUrl(minTransactionId, accountId));
			httpGet.setHeader(authHeader);
			httpGet.setHeader(OandaConstants.UNIX_DATETIME_HEADER);

			LOG.info(TradingUtils.executingRequestMsg(httpGet));
			HttpResponse httpResponse = httpClient.execute(httpGet);
			String strResp = TradingUtils.responseToString(httpResponse);

			if (strResp != StringUtils.EMPTY) {
				Object obj = JSONValue.parse(strResp);
				JSONObject jsonResp = (JSONObject) obj;
				JSONArray transactionsJson = (JSONArray) jsonResp.get(TRANSACTIONS);
				for (Object o : transactionsJson) {
					try {
						JSONObject transactionJson = (JSONObject) o;
						final String type = transactionJson.get(OandaJsonKeys.type).toString();
						Event transactionEvent = OandaUtils.toOandaTransactionType(type);
						Transaction<Long, Long, String> transaction = null;
						if (transactionEvent instanceof TradeEvents) {
							transaction = handleTradeTransactionEvent((TradeEvents) transactionEvent, transactionJson);
						} else if (transactionEvent instanceof AccountEvents) {
							transaction = handleAccountTransactionEvent((AccountEvents) transactionEvent,
									transactionJson);
						} else if (transactionEvent instanceof OrderEvents) {
							transaction = handleOrderTransactionEvent((OrderEvents) transactionEvent, transactionJson);
						}

						if (transaction != null) {
							allTransactions.add(transaction);
						}
					} catch (Exception e) {
						LOG.error("error encountered whilst parsing:" + o, e);
					}

				}
			} else {
				TradingUtils.printErrorMsg(httpResponse);
			}
		} catch (Exception e) {
			LOG.error("error encountered->", e);
		}
		return allTransactions;
	}

	private String deriveInstrument(JSONObject transactionJson) {
		String strInstr = transactionJson.get(OandaJsonKeys.instrument).toString();
		return strInstr;
	}

	private Transaction<Long, Long, String> handleTradeTransactionEvent(TradeEvents tradeEvent,
			JSONObject transactionJson) {
		Transaction<Long, Long, String> transaction = null;
		String strInstr = deriveInstrument(transactionJson);
		switch (tradeEvent) {
		case TAKE_PROFIT_FILLED:
		case STOP_LOSS_FILLED:
		case TRADE_CLOSE:
		case TRAILING_STOP_FILLED:
			transaction = new Transaction<Long, Long, String>(getTransactionId(transactionJson), tradeEvent,
					getAccountId(transactionJson), strInstr, getUnits(transactionJson),
					OandaUtils.toTradingSignal(getSide(transactionJson)), getTransactionTime(transactionJson),
					getPrice(transactionJson), getInterest(transactionJson), getPnl(transactionJson));
			transaction.setLinkedTransactionId(getLinkedTradeId(transactionJson));
			return transaction;
		case TRADE_UPDATE:
			transaction = new Transaction<Long, Long, String>(getTransactionId(transactionJson), tradeEvent,
					getAccountId(transactionJson), strInstr, getUnits(transactionJson), null,
					getTransactionTime(transactionJson), null, null, null);
			transaction.setLinkedTransactionId(getLinkedTradeId(transactionJson));
			return transaction;
		default:
			break;
		}
		return transaction;
	}

	private Transaction<Long, Long, String> handleAccountTransactionEvent(AccountEvents accountEvent,
			JSONObject transactionJson) {
		Transaction<Long, Long, String> transaction = null;
		String strInstr = deriveInstrument(transactionJson);
		switch (accountEvent) {
		case DAILY_INTEREST:
			transaction = new Transaction<Long, Long, String>(getTransactionId(transactionJson), accountEvent,
					getAccountId(transactionJson), strInstr, null, null, getTransactionTime(transactionJson), null,
					getInterest(transactionJson), null);
			transaction.setLinkedTransactionId(getLinkedTradeId(transactionJson));
			return transaction;
		default:
			break;
		}
		return transaction;
	}

	private Transaction<Long, Long, String> handleOrderTransactionEvent(OrderEvents orderEvent,
			JSONObject transactionJson) {
		Transaction<Long, Long, String> transaction = null;
		String strInstr = null;
		switch (orderEvent) {
		case LIMIT_ORDER_CREATE:
			strInstr = deriveInstrument(transactionJson);
			transaction = new Transaction<Long, Long, String>(getTransactionId(transactionJson), orderEvent,
					getAccountId(transactionJson), strInstr, getUnits(transactionJson),
					OandaUtils.toTradingSignal(getSide(transactionJson)), getTransactionTime(transactionJson),
					getPrice(transactionJson), null, null);
			transaction.setLinkedTransactionId(getLinkedTradeId(transactionJson));
			break;
		case MARKET_ORDER_CREATE:
		case ORDER_FILLED:
			strInstr = deriveInstrument(transactionJson);
			transaction = new Transaction<Long, Long, String>(getTransactionId(transactionJson), orderEvent,
					getAccountId(transactionJson), strInstr, getUnits(transactionJson),
					OandaUtils.toTradingSignal(getSide(transactionJson)), getTransactionTime(transactionJson),
					getPrice(transactionJson), getInterest(transactionJson), getPnl(transactionJson));
			transaction.setLinkedTransactionId(getLinkedTradeId(transactionJson));
			break;
		case ORDER_CANCEL:
		case ORDER_UPDATE:
		default:
			break;
		}
		return transaction;
	}

	private String getSide(JSONObject transaction) {
		return transaction.get(OandaJsonKeys.side).toString();
	}

	private Double getPrice(JSONObject transaction) {
		return ((Number) transaction.get(OandaJsonKeys.price)).doubleValue();
	}

	private Long getUnits(JSONObject transaction) {
		return (Long) transaction.get(OandaJsonKeys.units);
	}

	private Double getInterest(JSONObject transaction) {
		return ((Number) transaction.get(OandaJsonKeys.interest)).doubleValue();
	}

	private Double getPnl(JSONObject transaction) {
		return ((Number) transaction.get(OandaJsonKeys.pl)).doubleValue();
	}

	private DateTime getTransactionTime(JSONObject transaction) {
		Long transactionTime = Long.parseLong(transaction.get(OandaJsonKeys.time).toString());
		return new DateTime(TradingUtils.toMillisFromNanos(transactionTime));
	}

	private Long getAccountId(JSONObject transaction) {
		return (Long) transaction.get(OandaJsonKeys.accountId);
	}

	private Long getTransactionId(JSONObject transaction) {
		return (Long) transaction.get(OandaJsonKeys.id);
	}

	private Long getLinkedTradeId(JSONObject transaction) {
		Long lnkedTransactionId = (Long) transaction.get(OandaJsonKeys.tradeId);
		if (lnkedTransactionId == null) {
			lnkedTransactionId = ZERO.longValue();
		}
		return lnkedTransactionId;
	}

}
