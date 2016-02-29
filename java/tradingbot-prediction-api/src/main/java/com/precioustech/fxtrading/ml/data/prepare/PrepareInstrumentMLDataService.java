package com.precioustech.fxtrading.ml.data.prepare;

import java.io.File;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.historic.CandleStick;
import com.precioustech.fxtrading.marketdata.historic.CandleStickGranularity;
import com.precioustech.fxtrading.marketdata.historic.HistoricMarketDataProvider;

public class PrepareInstrumentMLDataService<T> {
	@Autowired
	private HistoricMarketDataProvider<T> historicMarketDataProvider;
	@Autowired
	private MLDataPrepareConfig dataPrepareConfig;

	/*
	 * features include: interval, day of week, month, Pair Event
	 * 
	 * "BUY",9,"MON","FEB", HIGH
	 * 
	 * predict: buy,sell or hold
	 */
	public File prepareTrainingDataSetForInstrument(TradeableInstrument<T> instrument,
			CandleStickGranularity granularity, DateTime from, DateTime to) {
		List<CandleStick<T>> candles = this.historicMarketDataProvider.getCandleSticks(instrument, granularity, from,
				to);
		for (CandleStick<T> candle : candles) {
			TradingSignal signal = deriveSignal(candle);
		}
		return null;
	}

	private TradingSignal deriveSignal(CandleStick<T> candle) {
		double open = candle.getOpenPrice();
		double close = candle.getClosePrice();
		double delta = close - open;
		double pctMovement = (Math.abs(delta) * 100.0) / open;
		if (pctMovement > this.dataPrepareConfig.getMinPercentMovementRequired()) {
			if (Math.signum(delta) > 1.0) {
				return TradingSignal.LONG;
			} else {
				return TradingSignal.SHORT;
			}
		} else {
			return TradingSignal.NONE;
		}
	}
}
