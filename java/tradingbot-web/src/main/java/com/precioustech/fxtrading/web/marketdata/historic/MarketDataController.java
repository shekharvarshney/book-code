package com.precioustech.fxtrading.web.marketdata.historic;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.precioustech.fxtrading.instrument.InstrumentService;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.historic.CandleStick;
import com.precioustech.fxtrading.marketdata.historic.CandleStickGranularity;
import com.precioustech.fxtrading.marketdata.historic.HistoricMarketDataProvider;

@Controller
public class MarketDataController<T> {

	private static final String candleStickDatePattern = "dd/MM/yyyy HH:mm:ss";

	@Autowired
	private HistoricMarketDataProvider<T> historicMarketDataProvider;
	@Autowired
	private InstrumentService<T> instrumentService;

	@RequestMapping(method = RequestMethod.GET, value = "/candles")
	public String candles(Model model) {
		model.addAttribute("instruments", this.instrumentService.getInstruments());
		model.addAttribute("granularities", Arrays.asList(CandleStickGranularity.values()));
		return "candles";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/jsonCandles", produces = "application/json", headers = "Accept=*/*")
	@ResponseBody
	public List<CandleStick<T>> getCandlesAsJson(@RequestParam("currencyPair") String currencyPair,
			@RequestParam("count") Integer candleCount,
			@RequestParam("fromDate") @DateTimeFormat(pattern = candleStickDatePattern) Date fromDate,
			@RequestParam("toDate") @DateTimeFormat(pattern = candleStickDatePattern) Date toDate,
			@RequestParam("granularity") String granularity) {
		TradeableInstrument<T> instrument = new TradeableInstrument<>(currencyPair);
		List<CandleStick<T>> candles = null;
		CandleStickGranularity candleStickGranularity = CandleStickGranularity.valueOf(granularity);
		if (candleCount != null) {
			candles = this.historicMarketDataProvider.getCandleSticks(instrument, candleStickGranularity, candleCount);
		} else {
			candles = this.historicMarketDataProvider.getCandleSticks(instrument, candleStickGranularity,
					new DateTime(fromDate.getTime()), new DateTime(toDate.getTime()));
		}
		return candles;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/getCandles")
	public String getCandles(@RequestParam("currencyPair") String currencyPair,
			@RequestParam("count") Integer candleCount,
			@RequestParam("fromDate") @DateTimeFormat(pattern = candleStickDatePattern) Date fromDate,
			@RequestParam("toDate") @DateTimeFormat(pattern = candleStickDatePattern) Date toDate,
			@RequestParam("granularity") String granularity,
			Model model) {

		List<CandleStick<T>> candles = getCandlesAsJson(currencyPair, candleCount, fromDate, toDate, granularity);

		model.addAttribute("candles", candles);
		return candles(model);
	}
}
