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
package com.precioustech.fxtrading.oanda.restapi.helper;

import com.precioustech.fxtrading.TradingConstants;
import com.precioustech.fxtrading.helper.ProviderHelper;
import com.precioustech.fxtrading.oanda.restapi.OandaConstants;
import com.precioustech.fxtrading.oanda.restapi.utils.OandaUtils;
import com.precioustech.fxtrading.utils.TradingUtils;

public class OandaProviderHelper implements ProviderHelper<String> {

	@Override
	public String fromIsoFormat(String instrument) {
		return OandaUtils.isoCcyToOandaCcy(instrument);
	}

	@Override
	public String fromPairSeparatorFormat(String instrument) {
		String[] pair = TradingUtils.splitInstrumentPair(instrument);
		return String.format("%s%s%s", pair[0], OandaConstants.CCY_PAIR_SEP, pair[1]);
	}

	@Override
	public String toIsoFormat(String instrument) {
		String tokens[] = TradingUtils.splitCcyPair(instrument, TradingConstants.CURRENCY_PAIR_SEP_UNDERSCORE);
		String isoInstrument = tokens[0] + tokens[1];
		return isoInstrument;
	}

	@Override
	public String fromHashTagCurrency(String instrument) {
		return OandaUtils.hashTagCcyToOandaCcy(instrument);
	}

	@Override
	public String getLongNotation() {
		return OandaConstants.BUY;
	}

	@Override
	public String getShortNotation() {
		return OandaConstants.SELL;
	}

}
