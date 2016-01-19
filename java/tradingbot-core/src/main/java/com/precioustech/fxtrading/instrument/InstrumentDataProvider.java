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

/**
 * A provider of tradeable instrument data information. At the very minimum the
 * provider must provide the instrument name and pip value for each instrument.
 * Since the instrument data almost never changes during trading hours, it is
 * highly recommended that the data returned from this provider is cached in an
 * immutable collection.
 * 
 * @author Shekhar Varshney
 *
 * @param <T>The type of instrumentId in class TradeableInstrument
 * @see TradeableInstrument
 */
public interface InstrumentDataProvider<T> {
	/**
	 * 
	 * @return a collection of all TradeableInstrument available to trade on the
	 *         brokerage platform.
	 */
	Collection<TradeableInstrument<T>> getInstruments();
}
