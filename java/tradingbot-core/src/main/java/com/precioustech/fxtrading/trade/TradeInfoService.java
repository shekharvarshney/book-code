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
package com.precioustech.fxtrading.trade;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.precioustech.fxtrading.account.Account;
import com.precioustech.fxtrading.account.AccountDataProvider;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.utils.TradingUtils;

/**
 * A service that provides information regarding a Trade. It maintains an
 * internal cache of all trades grouped per account in order to minimize
 * latency.
 * 
 * <p>
 * It is strongly recommended to use this service as a singleton and not create
 * multiple instances in order to minimize the memory footprint incurred by the
 * cache and the latency induced during the cache construction. This class
 * exposes a method to refresh the cache if required in a ThreadSafe manner and
 * it is imperative that it be called for events which close,create or modify a
 * trade in order to keep the cache in sync with the trades on the trading
 * platform. If such event callback is not supported by the trading platform,
 * then a timer based approach can be implemented externally, that periodically
 * refreshes the cache.
 * </p>
 * <p>
 * The accountId is the key of the internal cache.If accountId is a user defined
 * object, rather than the commonly used ones such as Integer,Long, or String,
 * please make sure hashCode() and equals() methods are implemented.
 * </p>
 * 
 * @author Shekhar Varshney
 *
 * @param <M>
 *            The type of tradeId
 * @param <N>
 *            The type of instrumentId in class TradeableInstrument
 * @param <K>
 *            The type of accountId
 * @see TradeableInstrument
 * @see refreshTradesForAccount
 */
public class TradeInfoService<M, N, K> {

	private static final Logger LOG = Logger.getLogger(TradeInfoService.class);
	private final TradeManagementProvider<M, N, K> tradeManagementProvider;
	private final AccountDataProvider<K> accountDataProvider;
	private final ConcurrentMap<K, Map<TradeableInstrument<N>, Collection<Trade<M, N, K>>>> tradesCache = Maps
			.newConcurrentMap();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	public TradeInfoService(TradeManagementProvider<M, N, K> tradeManagementProvider,
			AccountDataProvider<K> accountDataProvider) {
		this.tradeManagementProvider = tradeManagementProvider;
		this.accountDataProvider = accountDataProvider;
	}

	/**
	 * 
	 * @param instrument
	 * @return a Collection of accountIds that have at least 1 trade for the
	 *         given instrument. An empty Collection is returned if none satisfy
	 *         this condition.
	 */
	public Collection<K> findAllAccountsWithInstrumentTrades(TradeableInstrument<N> instrument) {
		Collection<K> accountIds = Sets.newHashSet();
		lock.readLock().lock();
		try {
			for (K accountId : this.tradesCache.keySet()) {
				if (isTradeExistsForInstrument(instrument, accountId)) {
					accountIds.add(accountId);
				}
			}
		} finally {
			lock.readLock().unlock();
		}
		return accountIds;
	}

	/**
	 * A method that initializes the service and primarily the cache of trades
	 * per account for the service. This method is automatically invoked by
	 * Spring Framework(if used) once the ApplicationContext is initialized. If
	 * not using Spring, the consumer of this service must call this method
	 * first in order to construct the cache.
	 * 
	 * @see TradeManagementProvider
	 */
	@PostConstruct
	public void init() {
		reconstructCache();
	}

	private void reconstructCache() {
		lock.writeLock().lock();
		try {
			tradesCache.clear();
			Collection<Account<K>> accounts = this.accountDataProvider.getLatestAccountInfo();
			for (Account<K> account : accounts) {
				Map<TradeableInstrument<N>, Collection<Trade<M, N, K>>> tradeMap = getTradesPerInstrumentForAccount(account
						.getAccountId());
				tradesCache.put(account.getAccountId(), tradeMap);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * 
	 * @param currency
	 * @return the net position for a given currency considering all Trades for
	 *         all Accounts. A negative value suggests that the system has net
	 *         short position for the given currency, whilst a positive value
	 *         suggests a net long position.
	 * @see TradingUtils#getSign(String,
	 *      com.precioustech.fxtrading.TradingSignal, String)
	 */
	public int findNetPositionCountForCurrency(String currency) {
		int posCt = 0;
		lock.readLock().lock();
		try {
			for (K accountId : this.tradesCache.keySet()) {
				posCt += findNetPositionCountForCurrency(currency, accountId);
			}
		} finally {
			lock.readLock().unlock();
		}
		return posCt;
	}

	/**
	 * 
	 * @return a Collection of all trades that belong to all accounts for the
	 *         user
	 */
	public Collection<Trade<M, N, K>> getAllTrades() {
		lock.readLock().lock();
		try {
			Collection<Trade<M, N, K>> trades = Lists.newArrayList();
			for (K accId : this.tradesCache.keySet()) {
				trades.addAll(getTradesForAccount(accId));
			}
			return trades;
		} finally {
			lock.readLock().unlock();
		}

	}

	private Map<TradeableInstrument<N>, Collection<Trade<M, N, K>>> getTradesPerInstrumentForAccount(K accountId) {
		Collection<Trade<M, N, K>> trades = this.tradeManagementProvider.getTradesForAccount(accountId);
		Map<TradeableInstrument<N>, Collection<Trade<M, N, K>>> tradeMap = Maps.newHashMap();
		for (Trade<M, N, K> ti : trades) {
			Collection<Trade<M, N, K>> tradeLst = null;
			if (tradeMap.containsKey(ti.getInstrument())) {
				tradeLst = tradeMap.get(ti.getInstrument());
			} else {
				tradeLst = Lists.newArrayList();
				tradeMap.put(ti.getInstrument(), tradeLst);
			}
			tradeLst.add(ti);
		}
		return tradeMap;
	}

	/**
	 * 
	 * @param accountId
	 * @param instrument
	 * @return a Collection of Trades for a given instrument and for a given
	 *         account.An empty Collection is returned if none found.
	 */
	public Collection<Trade<M, N, K>> getTradesForAccountAndInstrument(K accountId, TradeableInstrument<N> instrument) {
		Map<TradeableInstrument<N>, Collection<Trade<M, N, K>>> tradesForAccount = this.tradesCache.get(accountId);
		synchronized (tradesForAccount) {
			if (!TradingUtils.isEmpty(tradesForAccount) && tradesForAccount.containsKey(instrument)) {
				return Lists.newArrayList(tradesForAccount.get(instrument));
			}
		}
		return Collections.emptyList();
	}

	/**
	 * 
	 * @param accountId
	 * @return a Collection of all Trades for a given accountId. An empty
	 *         Collection is returned if none found.
	 */
	public Collection<Trade<M, N, K>> getTradesForAccount(K accountId) {
		Map<TradeableInstrument<N>, Collection<Trade<M, N, K>>> tradesForAccount = this.tradesCache.get(accountId);
		Collection<Trade<M, N, K>> trades = Lists.newArrayList();
		synchronized (tradesForAccount) {
			if (TradingUtils.isEmpty(tradesForAccount)) {
				return trades;
			}
			for (Collection<Trade<M, N, K>> tradeLst : tradesForAccount.values()) {
				trades.addAll(tradeLst);
			}
		}
		return trades;
	}

	private int findNetPositionCountForCurrency(String currency, K accountId) {
		Map<TradeableInstrument<N>, Collection<Trade<M, N, K>>> tradeMap = this.tradesCache.get(accountId);
		synchronized (tradeMap) {
			if (TradingUtils.isEmpty(tradeMap)) {
				return 0;
			} else {
				int positionCtr = 0;
				for (Collection<Trade<M, N, K>> trades : tradeMap.values()) {
					for (Trade<M, N, K> tradeInfo : trades) {
						positionCtr += TradingUtils.getSign(tradeInfo.getInstrument().getInstrument(), tradeInfo
								.getSide(), currency);
					}
				}

				return positionCtr;
			}
		}
	}

	/**
	 * A convenient method to refresh the internal cache of trades for an
	 * account in a ThreadSafe manner. Ideally this method should be invoked
	 * when the Trading platform notifies of a Trading event such as a creation
	 * of Trade when an Order is filled or the Trade is settled when a stopLoss
	 * or a takeProfit level is hit.
	 * 
	 * @param accountId
	 */
	public void refreshTradesForAccount(K accountId) {
		Map<TradeableInstrument<N>, Collection<Trade<M, N, K>>> tradeMap = getTradesPerInstrumentForAccount(accountId);
		Map<TradeableInstrument<N>, Collection<Trade<M, N, K>>> oldTradeMap = tradesCache.get(accountId);
		synchronized (oldTradeMap) {
			oldTradeMap.clear();
			oldTradeMap.putAll(tradeMap);
		}

	}

	/**
	 * 
	 * @param instrument
	 * @return boolean to indicate whether a trade with given instrument exists.
	 */
	public boolean isTradeExistsForInstrument(TradeableInstrument<N> instrument) {
		lock.readLock().lock();
		try {
			for (K accountId : this.tradesCache.keySet()) {
				if (isTradeExistsForInstrument(instrument, accountId)) {
					return true;
				}
			}
		} finally {
			lock.readLock().unlock();
		}
		return false;
	}

	private boolean isTradeExistsForInstrument(TradeableInstrument<N> instrument, K accountId) {
		Map<TradeableInstrument<N>, Collection<Trade<M, N, K>>> tradesForAccount = this.tradesCache.get(accountId);
		synchronized (tradesForAccount) {
			if (TradingUtils.isEmpty(tradesForAccount)) {
				return false;
			}
			return tradesForAccount.containsKey(instrument);
		}

	}
}
