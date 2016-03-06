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
package com.precioustech.fxtrading.oanda.restapi.order;

import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.expiry;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.id;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.instrument;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.orderOpened;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.orders;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.price;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.side;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.stopLoss;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.takeProfit;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.tradeOpened;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.type;
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
import org.apache.http.client.methods.HttpPost;
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
import com.precioustech.fxtrading.account.Account;
import com.precioustech.fxtrading.account.AccountDataProvider;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.oanda.restapi.OandaConstants;
import com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys;
import com.precioustech.fxtrading.oanda.restapi.utils.OandaUtils;
import com.precioustech.fxtrading.order.Order;
import com.precioustech.fxtrading.order.OrderManagementProvider;
import com.precioustech.fxtrading.order.OrderType;
import com.precioustech.fxtrading.utils.TradingUtils;

public class OandaOrderManagementProvider implements OrderManagementProvider<Long, String, Long> {

	private static final Logger LOG = Logger.getLogger(OandaOrderManagementProvider.class);

	private final String url;
	private final BasicHeader authHeader;
	private static final String ordersResource = "/orders";
	private final AccountDataProvider<Long> accountDataProvider;

	public OandaOrderManagementProvider(String url, String accessToken, AccountDataProvider<Long> accountDataProvider) {
		this.url = url;
		this.authHeader = OandaUtils.createAuthHeader(accessToken);
		this.accountDataProvider = accountDataProvider;
	}

	CloseableHttpClient getHttpClient() {
		return HttpClientBuilder.create().build();
	}

	@Override
	public boolean closeOrder(Long orderId, Long accountId) {
		CloseableHttpClient httpClient = getHttpClient();
		try {
			HttpDelete httpDelete = new HttpDelete(orderForAccountUrl(accountId, orderId));
			httpDelete.setHeader(authHeader);
			LOG.info(TradingUtils.executingRequestMsg(httpDelete));
			HttpResponse resp = httpClient.execute(httpDelete);
			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				LOG.info(String.format("Order %d successfully deleted for account %d", orderId, accountId));
				return true;
			} else {
				LOG.warn(String.format("Order %d could not be deleted. Recd error code %d", orderId, resp
						.getStatusLine().getStatusCode()));
			}
		} catch (Exception e) {
			LOG.warn("error deleting order id:" + orderId, e);
		} finally {
			TradingUtils.closeSilently(httpClient);
		}
		return false;
	}

	HttpPatch createPatchCommand(Order<String, Long> order, Long accountId) throws Exception {
		HttpPatch httpPatch = new HttpPatch(orderForAccountUrl(accountId, order.getOrderId()));
		httpPatch.setHeader(this.authHeader);
		List<NameValuePair> params = Lists.newArrayList();
		params.add(new BasicNameValuePair(takeProfit, String.valueOf(order.getTakeProfit())));
		params.add(new BasicNameValuePair(stopLoss, String.valueOf(order.getStopLoss())));
		params.add(new BasicNameValuePair(units, String.valueOf(order.getUnits())));
		params.add(new BasicNameValuePair(price, String.valueOf(order.getPrice())));
		httpPatch.setEntity(new UrlEncodedFormEntity(params));
		return httpPatch;
	}

	HttpPost createPostCommand(Order<String, Long> order, Long accountId) throws Exception {
		HttpPost httpPost = new HttpPost(this.url + OandaConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH
				+ accountId + ordersResource);
		httpPost.setHeader(this.authHeader);
		List<NameValuePair> params = Lists.newArrayList();
		// TODO: apply proper rounding. Oanda rejects 0.960000001
		params.add(new BasicNameValuePair(instrument, order.getInstrument().getInstrument()));
		params.add(new BasicNameValuePair(side, OandaUtils.toSide(order.getSide())));
		params.add(new BasicNameValuePair(type, OandaUtils.toType(order.getType())));
		params.add(new BasicNameValuePair(units, String.valueOf(order.getUnits())));
		params.add(new BasicNameValuePair(takeProfit, String.valueOf(order.getTakeProfit())));
		params.add(new BasicNameValuePair(stopLoss, String.valueOf(order.getStopLoss())));
		if (order.getType() == OrderType.LIMIT && order.getPrice() != 0.0) {
			DateTime now = DateTime.now();
			DateTime nowplus4hrs = now.plusHours(4);// TODO: why this code
													// for expiry?
			String dateStr = nowplus4hrs.toString();
			params.add(new BasicNameValuePair(price, String.valueOf(order.getPrice())));
			params.add(new BasicNameValuePair(expiry, dateStr));
		}
		httpPost.setEntity(new UrlEncodedFormEntity(params));
		return httpPost;
	}

	@Override
	public Long placeOrder(Order<String, Long> order, Long accountId) {
		CloseableHttpClient httpClient = getHttpClient();
		try {
			HttpPost httpPost = createPostCommand(order, accountId);
			LOG.info(TradingUtils.executingRequestMsg(httpPost));
			HttpResponse resp = httpClient.execute(httpPost);
			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK
					|| resp.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
				if (resp.getEntity() != null) {
					String strResp = TradingUtils.responseToString(resp);
					Object o = JSONValue.parse(strResp);
					JSONObject orderResponse;
					if (order.getType() == OrderType.MARKET) {
						orderResponse = (JSONObject) ((JSONObject) o).get(tradeOpened);
					} else {
						orderResponse = (JSONObject) ((JSONObject) o).get(orderOpened);
					}
					Long orderId = (Long) orderResponse.get(OandaJsonKeys.id);
					LOG.info("Order executed->" + strResp);
					return orderId;
				} else {
					return null;
				}

			} else {
				LOG.info(String.format("Order not executed. http code=%d. Order pojo->%s",
						resp.getStatusLine().getStatusCode(), order.toString()));
				return null;
			}
		} catch (Exception e) {
			LOG.warn(e);
			return null;
		} finally {
			TradingUtils.closeSilently(httpClient);
		}
	}

	@Override
	public Order<String, Long> pendingOrderForAccount(Long orderId, Long accountId) {
		CloseableHttpClient httpClient = getHttpClient();
		try {
			HttpUriRequest httpGet = new HttpGet(orderForAccountUrl(accountId, orderId));
			httpGet.setHeader(this.authHeader);
			httpGet.setHeader(OandaConstants.UNIX_DATETIME_HEADER);
			LOG.info(TradingUtils.executingRequestMsg(httpGet));
			HttpResponse resp = httpClient.execute(httpGet);
			String strResp = TradingUtils.responseToString(resp);
			if (strResp != StringUtils.EMPTY) {
				JSONObject order = (JSONObject) JSONValue.parse(strResp);
				return parseOrder(order);
			} else {
				TradingUtils.printErrorMsg(resp);
			}
		} catch (Exception e) {
			LOG.error(String.format("error encountered whilst fetching pending order for account %d and order id %d",
					accountId, orderId), e);
		} finally {
			TradingUtils.closeSilently(httpClient);
		}
		return null;
	}

	private Order<String, Long> parseOrder(JSONObject order) {
		final String orderInstrument = (String) order.get(instrument);
		final Long orderUnits = (Long) order.get(units);
		final TradingSignal orderSide = OandaUtils.toTradingSignal((String) order.get(side));
		final OrderType orderType = OandaUtils.toOrderType((String) order.get(type));
		final double orderTakeProfit = ((Number) order.get(takeProfit)).doubleValue();
		final double orderStopLoss = ((Number) order.get(stopLoss)).doubleValue();
		final double orderPrice = ((Number) order.get(price)).doubleValue();
		final Long orderId = (Long) order.get(id);
		Order<String, Long> pendingOrder = new Order<String, Long>(new TradeableInstrument<String>(orderInstrument),
				orderUnits, orderSide, orderType, orderTakeProfit, orderStopLoss, orderPrice);
		pendingOrder.setOrderId(orderId);
		return pendingOrder;
	}

	@Override
	public Collection<Order<String, Long>> pendingOrdersForInstrument(TradeableInstrument<String> instrument) {
		Collection<Account<Long>> accounts = this.accountDataProvider.getLatestAccountInfo();
		Collection<Order<String, Long>> allOrders = Lists.newArrayList();
		for (Account<Long> account : accounts) {
			allOrders.addAll(this.pendingOrdersForAccount(account.getAccountId(), instrument));
		}
		return allOrders;
	}

	@Override
	public Collection<Order<String, Long>> allPendingOrders() {
		return pendingOrdersForInstrument(null);
	}

	private Collection<Order<String, Long>> pendingOrdersForAccount(Long accountId,
			TradeableInstrument<String> instrument) {
		Collection<Order<String, Long>> pendingOrders = Lists.newArrayList();
		CloseableHttpClient httpClient = getHttpClient();
		try {
			HttpUriRequest httpGet = new HttpGet(this.url + OandaConstants.ACCOUNTS_RESOURCE
					+ TradingConstants.FWD_SLASH + accountId + ordersResource
					+ (instrument != null ? "?instrument=" + instrument.getInstrument() : StringUtils.EMPTY));
			httpGet.setHeader(this.authHeader);
			httpGet.setHeader(OandaConstants.UNIX_DATETIME_HEADER);
			LOG.info(TradingUtils.executingRequestMsg(httpGet));
			HttpResponse resp = httpClient.execute(httpGet);
			String strResp = TradingUtils.responseToString(resp);
			if (strResp != StringUtils.EMPTY) {
				Object obj = JSONValue.parse(strResp);
				JSONObject jsonResp = (JSONObject) obj;
				JSONArray accountOrders = (JSONArray) jsonResp.get(orders);

				for (Object o : accountOrders) {
					JSONObject order = (JSONObject) o;
					Order<String, Long> pendingOrder = parseOrder(order);
					pendingOrders.add(pendingOrder);
				}
			} else {
				TradingUtils.printErrorMsg(resp);
			}
		} catch (Exception e) {
			LOG.error(String.format("error encountered whilst fetching pending orders for account %d and instrument %s",
					accountId, instrument.getInstrument()), e);
		} finally {
			TradingUtils.closeSilently(httpClient);
		}
		return pendingOrders;
	}

	@Override
	public Collection<Order<String, Long>> pendingOrdersForAccount(Long accountId) {
		return this.pendingOrdersForAccount(accountId, null);
	}

	String orderForAccountUrl(Long accountId, Long orderId) {
		return this.url + OandaConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId + ordersResource
				+ TradingConstants.FWD_SLASH + orderId;
	}

	@Override
	public boolean modifyOrder(Order<String, Long> order, Long accountId) {
		CloseableHttpClient httpClient = getHttpClient();
		try {
			HttpPatch httpPatch = createPatchCommand(order, accountId);
			LOG.info(TradingUtils.executingRequestMsg(httpPatch));
			HttpResponse resp = httpClient.execute(httpPatch);
			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK && resp.getEntity() != null) {
				LOG.info("Order Modified->" + TradingUtils.responseToString(resp));
				return true;
			}
			LOG.warn(String.format("order %s could not be modified.", order.toString()));
		} catch (Exception e) {
			LOG.error(String.format("error encountered whilst modifying order %d for account %d", order, accountId), e);
		} finally {
			TradingUtils.closeSilently(httpClient);
		}
		return false;
	}

}
