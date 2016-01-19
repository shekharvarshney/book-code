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
package com.precioustech.fxtrading.position;

import java.util.Collection;

import com.precioustech.fxtrading.instrument.TradeableInstrument;

/**
 * A provider of services for instrument positions. A position for an instrument
 * is by definition aggregated trades for the instrument with an average price
 * where all trades must all be a LONG or a SHORT. It is a useful service to
 * project a summary of a given instrument and also if required close all trades
 * for a given instrument, ideally using a single call.
 * 
 * The implementation might choose to maintain an internal cache of positions in
 * order to reduce latency. If this is the case then it must find means to
 * either 1)hook into the event streaming and refresh the cache based on an
 * order/trade event or 2) regularly refresh the cache after a given time
 * period.
 * 
 * @author Shekhar Varshney
 *
 * @param <M>
 *            The type of instrumentId in class TradeableInstrument
 * @param <N>
 *            the type of accountId
 * 
 * @see TradeableInstrument
 */
public interface PositionManagementProvider<M, N> {

	/**
	 * 
	 * @param accountId
	 * @param instrument
	 * @return Position<M> for a given instrument and accountId(may be null if
	 *         all trades under a single account).
	 */
	Position<M> getPositionForInstrument(N accountId, TradeableInstrument<M> instrument);

	/**
	 * 
	 * @param accountId
	 * @return Collection of Position<M> objects for a given accountId.
	 */
	Collection<Position<M>> getPositionsForAccount(N accountId);

	/**
	 * close the position for a given instrument and accountId. This is one shot
	 * way to close all trades for a given instrument in an account.
	 * 
	 * @param accountId
	 * @param instrument
	 * @return if the operation was successful
	 */
	boolean closePosition(N accountId, TradeableInstrument<M> instrument);

}
