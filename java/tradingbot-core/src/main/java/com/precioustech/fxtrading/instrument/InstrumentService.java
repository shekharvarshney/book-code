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
package com.precioustech.fxtrading.instrument;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class InstrumentService<T> {

	private final Map<String, TradeableInstrument<T>> instrumentMap;

	public InstrumentService(InstrumentDataProvider<T> instrumentDataProvider) {
		Preconditions.checkNotNull(instrumentDataProvider);
		Collection<TradeableInstrument<T>> instruments = instrumentDataProvider.getInstruments();
		Map<String, TradeableInstrument<T>> tradeableInstrumenMap = Maps.newTreeMap();
		for (TradeableInstrument<T> instrument : instruments) {
			tradeableInstrumenMap.put(instrument.getInstrument(), instrument);
		}
		this.instrumentMap = Collections.unmodifiableMap(tradeableInstrumenMap);
	}

	public Collection<TradeableInstrument<T>> getInstruments() {
		return this.instrumentMap.values();
	}

	public Collection<TradeableInstrument<T>> getAllPairsWithCurrency(String currency) {
		Collection<TradeableInstrument<T>> allPairs = Sets.newHashSet();
		if (StringUtils.isEmpty(currency)) {
			return allPairs;
		}
		for (Map.Entry<String, TradeableInstrument<T>> entry : instrumentMap.entrySet()) {
			if (entry.getKey().contains(currency)) {
				allPairs.add(entry.getValue());
			}
		}
		return allPairs;
	}

	public Double getPipForInstrument(TradeableInstrument<T> instrument) {
		Preconditions.checkNotNull(instrument);
		TradeableInstrument<T> tradeableInstrument = this.instrumentMap.get(instrument.getInstrument());
		if (tradeableInstrument != null) {
			return tradeableInstrument.getPip();
		} else {
			return 1.0;
		}
	}
}
