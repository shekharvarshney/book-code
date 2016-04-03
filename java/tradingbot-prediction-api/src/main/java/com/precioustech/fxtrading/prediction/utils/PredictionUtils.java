package com.precioustech.fxtrading.prediction.utils;

import org.joda.time.DateTime;

import com.google.common.base.Preconditions;
import com.precioustech.fxtrading.prediction.TradingSessionEnum;

public class PredictionUtils {
	private PredictionUtils() {

	}

	public static TradingSessionEnum deriveTradingSession(DateTime dt) {
		Preconditions.checkNotNull(dt);
		int hrOfDay = dt.getHourOfDay();
		if (hrOfDay <= 6 || hrOfDay > 22) {
			return TradingSessionEnum.NIGHT;
		} else if (hrOfDay <= 14) {
			return TradingSessionEnum.MORNING;
		} else {
			return TradingSessionEnum.EVENING;
		}
	}
}
