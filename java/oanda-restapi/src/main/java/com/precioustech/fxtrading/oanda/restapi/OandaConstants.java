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

package com.precioustech.fxtrading.oanda.restapi;

import org.apache.http.message.BasicHeader;

import com.precioustech.fxtrading.TradingConstants;

public final class OandaConstants {
	private OandaConstants() {
	}

	// public static final String ACCESS_TOKEN =
	// "9d741c8312f25d9f5a094e53a354875b-2c9a7b49523374e177210af8e111c2f6";
	// public static final String CHF_ACCOUNT_ID = "764454";
	// public static final String PROD_STREAM_URL =
	// "https://stream-fxtrade.oanda.com";
	// public static final String PROD_API_URL =
	// "https://api-fxtrade.oanda.com";
	public static final String PRICES_RESOURCE = "/v1/prices";
	public static final String EVENTS_RESOURCE = "/v1/events";
	public static final String INSTRUMENTS_RESOURCE = "/v1/instruments";
	public static final String CANDLES_RESOURCE = "/v1/candles";
	public static final String ACCOUNTS_RESOURCE = "/v1/accounts";
	// public static final String TRADER_USERNAME = "cvarshney";
	public static final double LOT_SIZE = 10000.00;
	public static final String ORDER_MARKET = "market";
	public static final String ORDER_LIMIT = "limit";
	public static final String ORDER_MARKET_IF_TOUCHED = "marketIfTouched";
	// public static final BasicHeader AUTH_HEADER = new
	// BasicHeader("Authorization", "Bearer " + ACCESS_TOKEN);
	public static final BasicHeader UNIX_DATETIME_HEADER = new BasicHeader("X-Accept-Datetime-Format", "UNIX");
	public static final String CCY_PAIR_SEP = TradingConstants.CURRENCY_PAIR_SEP_UNDERSCORE;
	public static final String CCY_PAIR_SEP2 = "/";
	public static final String BUY = "buy";
	public static final String SELL = "sell";
	public static final String NONE = "none";
	public static final String INTEREST = "Interest";
	public static final String BUY_MKT = "Buy Market";
	public static final String SELL_MKT = "Sell Market";
	public static final String STOP_LOSS = "Stop Loss";
	public static final String TAKE_PROFIT = "Take Profit";
	public static final String CHANGE_TRADE = "Change Trade";
	public static final String CLOSE_TRADE = "Close Trade";
	public static final String TRAILING_STOP = "Trailing Stop";
	public static final String DAILYFX_CALENDAR_URL = "http://www.dailyfx.com/files/Calendar-%tm-%td-%tY.csv";
}
