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
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.precioustech.fxtrading.BaseTradingConfig;
import com.precioustech.fxtrading.TradingDecision;
import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.account.AccountInfoService;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.CurrentPriceInfoProvider;
import com.precioustech.fxtrading.marketdata.Price;

public class OrderExecutionService<M, N, K> implements Runnable {

	private static final Logger LOG = Logger.getLogger(OrderExecutionService.class);

	private final BlockingQueue<TradingDecision<N>> orderQueue;
	private final AccountInfoService<K, N> accountInfoService;
	private final OrderManagementProvider<M, N, K> orderManagementProvider;
	private final BaseTradingConfig baseTradingConfig;
	private final PreOrderValidationService<M, N, K> preOrderValidationService;
	private final CurrentPriceInfoProvider<N> currentPriceInfoProvider;
	private volatile boolean serviceUp = true;
	Thread orderExecThread;

	public OrderExecutionService(BlockingQueue<TradingDecision<N>> orderQueue,
			AccountInfoService<K, N> accountInfoService, OrderManagementProvider<M, N, K> orderManagementProvider,
			BaseTradingConfig baseTradingConfig, PreOrderValidationService<M, N, K> preOrderValidationService,
			CurrentPriceInfoProvider<N> currentPriceInfoProvider) {
		this.orderQueue = orderQueue;
		this.accountInfoService = accountInfoService;
		this.orderManagementProvider = orderManagementProvider;
		this.baseTradingConfig = baseTradingConfig;
		this.preOrderValidationService = preOrderValidationService;
		this.currentPriceInfoProvider = currentPriceInfoProvider;
	}

	@PostConstruct
	public void init() {
		orderExecThread = new Thread(this, this.getClass().getSimpleName());
		orderExecThread.start();
	}

	@PreDestroy
	public void shutDown() {
		this.serviceUp = false;
	}

	private boolean preValidate(TradingDecision<N> decision) {
		if (TradingSignal.NONE != decision.getSignal()
				&& this.preOrderValidationService.checkInstrumentNotAlreadyTraded(decision.getInstrument())
				&& this.preOrderValidationService.checkLimitsForCcy(decision.getInstrument(), decision.getSignal())) {
			Collection<TradeableInstrument<N>> instruments = Lists.newArrayList();
			instruments.add(decision.getInstrument());
			Map<TradeableInstrument<N>, Price<N>> priceMap = this.currentPriceInfoProvider
					.getCurrentPricesForInstruments(instruments);
			if (priceMap.containsKey(decision.getInstrument())) {
				Price<N> currentPrice = priceMap.get(decision.getInstrument());
				return this.preOrderValidationService.isInSafeZone(decision.getSignal(),
						decision.getSignal() == TradingSignal.LONG ? currentPrice.getAskPrice() : currentPrice
								.getBidPrice(), decision.getInstrument());
			}
		}
		return false;
	}

	@Override
	public void run() {
		while (serviceUp) {
			try {
				TradingDecision<N> decision = this.orderQueue.take();
				if (!preValidate(decision)) {
					continue;
				}
				Collection<K> accountIds = this.accountInfoService.findAccountsToTrade();
				if (accountIds.isEmpty()) {
					LOG.info("Not a single eligible account found as the reserve may have been exhausted.");
					continue;
				}
				Order<N, M> order = null;
				if (decision.getLimitPrice() == 0.0) {// market order
					order = new Order<N, M>(decision.getInstrument(), this.baseTradingConfig.getMaxAllowedQuantity(),
							decision.getSignal(), OrderType.MARKET, decision.getTakeProfitPrice(), decision
									.getStopLossPrice());
				} else {
					order = new Order<N, M>(decision.getInstrument(), this.baseTradingConfig.getMaxAllowedQuantity(),
							decision.getSignal(), OrderType.LIMIT, decision.getTakeProfitPrice(), decision
									.getStopLossPrice(), decision.getLimitPrice());
				}
				for (K accountId : accountIds) {
					M orderId = this.orderManagementProvider.placeOrder(order, accountId);
					if (orderId != null) {
						order.setOrderId(orderId);
					}
					break;
				}
			} catch (Exception e) {
				LOG.error("error encountered inside order execution service", e);
			}
		}

	}

}
