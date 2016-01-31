package com.precioustech.fxtrading.oanda.restapi.streaming.marketdata;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.historic.CandleStickGranularity;
import com.precioustech.fxtrading.marketdata.historic.HistoricMarketDataProvider;
import com.precioustech.fxtrading.marketdata.historic.MovingAverageCalculationService;
import com.precioustech.fxtrading.oanda.restapi.marketdata.historic.OandaHistoricMarketDataProvider;

public class MovingAverageCalculationServiceDemo {

	private static final Logger LOG = Logger.getLogger(MovingAverageCalculationServiceDemo.class);

	private static void usage(String[] args) {
		if (args.length != 2) {
			LOG.error("Usage: MovingAverageCalculationServiceDemo <url> <accesstoken>");
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		usage(args);
		final String url = args[0];
		final String accessToken = args[1];
		HistoricMarketDataProvider<String> historicMarketDataProvider = new OandaHistoricMarketDataProvider(url,
				accessToken);
		MovingAverageCalculationService<String> movingAverageCalcService = new MovingAverageCalculationService<String>(
				historicMarketDataProvider);
		TradeableInstrument<String> eurnzd = new TradeableInstrument<String>("EUR_NZD");
		final int countIntervals = 30;
		ImmutablePair<Double, Double> eurnzdSmaAndWma = movingAverageCalcService.calculateSMAandWMAasPair(eurnzd,
				countIntervals, CandleStickGranularity.H1);

		LOG.info(String.format("SMA=%2.5f,WMA=%2.5f for instrument=%s,granularity=%s for the last %d intervals",
				eurnzdSmaAndWma.left, eurnzdSmaAndWma.right, eurnzd.getInstrument(), CandleStickGranularity.H1,
				countIntervals));
		DateTime from = new DateTime(1444003200000L);// 5 Oct 2015
		DateTime to = new DateTime(1453075200000L);// 18 Jan 2016

		TradeableInstrument<String> gbpchf = new TradeableInstrument<String>("GBP_CHF");
		ImmutablePair<Double, Double> gbpchfSmaAndWma = movingAverageCalcService.calculateSMAandWMAasPair(gbpchf, from,
				to, CandleStickGranularity.W);

		LOG.info(String
				.format("SMA=%2.5f,WMA=%2.5f for instrument=%s,granularity=%s from %s to %s", gbpchfSmaAndWma.left,
						gbpchfSmaAndWma.right, gbpchf.getInstrument(), CandleStickGranularity.W, from, to));

	}
}
