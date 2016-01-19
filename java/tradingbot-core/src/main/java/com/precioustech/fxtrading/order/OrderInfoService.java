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
import com.precioustech.fxtrading.utils.TradingUtils;

//TODO: introduce a cache  like in TradeInfoService in order to avoid making expensive rest calls.
public class OrderInfoService<M, N, K> {

	private final OrderManagementProvider<M, N, K> orderManagementProvider;

	public OrderInfoService(OrderManagementProvider<M, N, K> orderManagementProvider) {
		this.orderManagementProvider = orderManagementProvider;
	}

	public Collection<Order<N, M>> allPendingOrders() {
		return this.orderManagementProvider.allPendingOrders();
	}

	public Collection<Order<N, M>> pendingOrdersForAccount(K accountId) {
		return this.orderManagementProvider.pendingOrdersForAccount(accountId);
	}

	public Collection<Order<N, M>> pendingOrdersForInstrument(TradeableInstrument<N> instrument) {
		return this.orderManagementProvider.pendingOrdersForInstrument(instrument);
	}

	public Order<N, M> pendingOrderForAccount(M orderId, K accountId) {
		return this.orderManagementProvider.pendingOrderForAccount(orderId, accountId);
	}

	public int findNetPositionCountForCurrency(String currency) {
		Collection<Order<N, M>> allOrders = allPendingOrders();
		int positionCount = 0;
		for (Order<N, M> order : allOrders) {
			positionCount += TradingUtils.getSign(order.getInstrument().getInstrument(), order.getSide(), currency);
		}
		return positionCount;
	}
}
