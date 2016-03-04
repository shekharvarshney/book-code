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

import org.apache.log4j.Logger;

import com.precioustech.fxtrading.BaseTradingConfig;
import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.historic.CandleStickGranularity;
import com.precioustech.fxtrading.marketdata.historic.MovingAverageCalculationService;
import com.precioustech.fxtrading.trade.TradeInfoService;
import com.precioustech.fxtrading.utils.TradingUtils;

/**
 *
 * @param <M>
 *            The type of tradeId
 * @param <N>
 *            The type of instrumentId in class TradeableInstrument
 * @param <K>
 *            The type of accountId
 * @see TradeableInstrument
 */
public class PreOrderValidationService<M, N, K> {
	private static final Logger LOG = Logger.getLogger(PreOrderValidationService.class);
	private final TradeInfoService<M, N, K> tradeInfoService;
	private final MovingAverageCalculationService<N> movingAverageCalculationService;
	private final BaseTradingConfig baseTradingConfig;
	private final OrderInfoService<M, N, K> orderInfoService;
	static final int FIVE_YRS_IN_MTHS = 60;

	public PreOrderValidationService(TradeInfoService<M, N, K> tradeInfoService,
			MovingAverageCalculationService<N> movingAverageCalculationService, BaseTradingConfig baseTradingConfig,
			OrderInfoService<M, N, K> orderInfoService) {
		this.tradeInfoService = tradeInfoService;
		this.movingAverageCalculationService = movingAverageCalculationService;
		this.baseTradingConfig = baseTradingConfig;
		this.orderInfoService = orderInfoService;
	}

	public boolean isInSafeZone(TradingSignal signal, double price, TradeableInstrument<N> instrument) {
		// check 10yr wma and make sure we are 10% on either side
		double wma10yr = this.movingAverageCalculationService.calculateWMA(instrument, FIVE_YRS_IN_MTHS,
				CandleStickGranularity.M);
		final double max10yrWmaOffset = baseTradingConfig.getMax10yrWmaOffset();
		double minPrice = (1.0 - max10yrWmaOffset) * wma10yr;
		double maxPrice = (1.0 + max10yrWmaOffset) * wma10yr;
		if ((signal == TradingSignal.SHORT && price > minPrice) || (signal == TradingSignal.LONG && price < maxPrice)) {
			return true;
		} else {
			LOG.info(String.format(
					"Rejecting %s  %s because price %2.5f is 10pct on either side of wma 10yr price of %2.5f", signal,
					instrument.getInstrument(), price, wma10yr));
			return false;
		}
	}

	public boolean checkInstrumentNotAlreadyTraded(TradeableInstrument<N> instrument) {
		Collection<K> accIds = this.tradeInfoService.findAllAccountsWithInstrumentTrades(instrument);
		if (accIds.size() > 0) {
			LOG.warn(String.format("Rejecting trade with instrument %s as trade already exists",
					instrument.getInstrument()));
			return false;
		} else {
			Collection<Order<N, M>> pendingOrders = this.orderInfoService.pendingOrdersForInstrument(instrument);
			if (!pendingOrders.isEmpty()) {
				LOG.warn(String.format("Pending order with instrument %s already exists", instrument.getInstrument()));
				return false;
			}
			return true;
		}
	}

	public boolean checkLimitsForCcy(TradeableInstrument<N> instrument, TradingSignal signal) {
		String currencies[] = TradingUtils.splitInstrumentPair(instrument.getInstrument());
		for (String currency : currencies) {
			int positionCount = this.tradeInfoService.findNetPositionCountForCurrency(currency)
					+ this.orderInfoService.findNetPositionCountForCurrency(currency);
			int sign = TradingUtils.getSign(instrument.getInstrument(), signal, currency);
			int newPositionCount = positionCount + sign;
			if (Math.abs(newPositionCount) > this.baseTradingConfig.getMaxAllowedNetContracts()
					&& Integer.signum(sign) == Integer.signum(positionCount)) {
				LOG.warn(String.format("Cannot place trade %s because max limit exceeded. max allowed=%d and "
								+ "future net positions=%d for currency %s if trade executed",
						instrument.getInstrument(), this.baseTradingConfig
						.getMaxAllowedNetContracts(), newPositionCount, currency));
				return false;
			}
		}
		return true;
	}

}
