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
package com.precioustech.fxtrading.order;

import java.util.Collection;

import com.precioustech.fxtrading.instrument.TradeableInstrument;

/**
 * A provider of CRUD operations for an instrument Order. An order is normally
 * placed for a given instrument and/or an accountId. An accountId may not be
 * required if only a single account is allowed by the platform provider, in
 * which case all orders are created under the default account.
 * 
 * @author Shekhar Varshney
 *
 * @param <M>
 *            The type of orderId
 * @param <N>
 *            The type of instrumentId in class TradeableInstrument
 * @param <K>
 *            The type of accountId
 * @see TradeableInstrument
 */
public interface OrderManagementProvider<M, N, K> {

	/**
	 * An order is normally of types market or limit. A market order is executed
	 * straight away by the platform whilst a limit order is executed only if
	 * the limit price is hit. Therefore for a limit order this method may not
	 * return an orderId.
	 * 
	 * @param order
	 * @param accountId
	 * @return a valid orderId if possible.
	 */
	M placeOrder(Order<N, M> order, K accountId);

	/**
	 * Modify the attributes of a given order. The platform may only permit to
	 * modify attributes like limit price, stop loss, take profit, expiration
	 * date, units.
	 * 
	 * @param order
	 * @param accountId
	 * @return boolean indicating if the operation was successful.
	 */
	boolean modifyOrder(Order<N, M> order, K accountId);

	/**
	 * Effectively cancel the order if it is waiting to be executed. A valid
	 * orderId and an optional accountId may be required to uniquely identify an
	 * order to close/cancel.
	 * 
	 * @param orderId
	 * @param accountId
	 * @return boolean indicating if the operation was successful.
	 */
	boolean closeOrder(M orderId, K accountId);

	/**
	 * 
	 * @return a collection of all pending orders across all accounts
	 */
	Collection<Order<N, M>> allPendingOrders();

	/**
	 * 
	 * @param accountId
	 * @return a collection of all pending orders for a given accountId.
	 */
	Collection<Order<N, M>> pendingOrdersForAccount(K accountId);

	/**
	 * 
	 * @param orderId
	 * @param accountId
	 * @return Order uniquely identified by orderId and optional accountId
	 */
	Order<N, M> pendingOrderForAccount(M orderId, K accountId);

	/**
	 * 
	 * @param instrument
	 * @return a collection of all pending orders for a given instrument for all
	 *         accounts
	 */
	Collection<Order<N, M>> pendingOrdersForInstrument(TradeableInstrument<N> instrument);

}
