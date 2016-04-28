package com.precioustech.fxtrading.oanda.restapi.account.transaction;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.instrument.InstrumentService;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.historic.CandleStick;
import com.precioustech.fxtrading.marketdata.historic.CandleStickGranularity;
import com.precioustech.fxtrading.marketdata.historic.HistoricMarketDataProvider;
import com.precioustech.fxtrading.oanda.restapi.account.transaction.dao.TradeDataDao;
import com.precioustech.fxtrading.oanda.restapi.account.transaction.entities.TradeData;
import com.precioustech.fxtrading.oanda.restapi.events.TradeEvents;

public class AnalyseFadeTheMoveTradesService {

	private String TP_TRANSACTION_TYPE = TradeEvents.TAKE_PROFIT_FILLED.name();

	@Autowired
	private TradeDataDao tradeDataDao;
	@Autowired
	private InstrumentService<String> instrumentService;
	@Autowired
	private HistoricMarketDataProvider<String> historicMarketDataProvider;

	public void analyseTakeProfitTrades() {
		Collection<TradeData> takeProfitTrades = tradeDataDao.tradesByTransactionType(TP_TRANSACTION_TYPE);
		takeProfitTrades.stream().filter(fadeTheMoveTrades())
				.sorted((t1, t2) -> t1.getDurationOpen().compareTo(t2.getDurationOpen())).forEach(trade -> {
					// DateTime takeProfitEvtDate =
					// trade.getTransactionCloseTime();
					/*
					 * time travel and find out what is the best we could have
					 * got. We get the next 5000 5s candles and see the best. in
					 * 99.9% of the case we should be able to get back to the
					 * execution price with 5K candles.
					 */
					TradeableInstrument<String> instr = new TradeableInstrument<>(trade.getInstrument());
					double pipForInstrument = instrumentService.getPipForInstrument(instr);
					double bestPrice = trade.getClosePrice();
					double startPrice = trade.getTransactionPrice();
					TradingSignal signal = TradingSignal.valueOf(trade.getDirection());
					List<CandleStick<String>> candles = historicMarketDataProvider.getCandleSticks(instr,
							CandleStickGranularity.S5, trade.getTransactionCloseTime(),
							trade.getTransactionCloseTime().plusHours(6));
					int i = 0;
					for (CandleStick<String> candle : candles) {
						if (signal == TradingSignal.LONG) {

							if (candle.getHighPrice() > bestPrice) {
								bestPrice = candle.getHighPrice();
							}
							if (candle.getLowPrice() < startPrice) {
								break;
							}
						} else {
							if (candle.getLowPrice() < bestPrice) {
								bestPrice = candle.getLowPrice();
							}
							if (candle.getHighPrice() > startPrice) {
								break;
							}
						}
						i++;
					}
					double bestPips = Math.abs(bestPrice - trade.getTransactionPrice()) / pipForInstrument;
					System.out.println(String.format(
							"id=%d %s open=%3.5f, close=%3.5f pnl=%2.5f duration=%d best=%3.5f bestpips=%2.2f ctr=%d",
							trade.getTransactionId(), trade.getInstrument(), trade.getTransactionPrice(),
							trade.getClosePrice(), trade.getPnl(), trade.getDurationOpen(), bestPrice, bestPips, i));
				});
	}

	private Predicate<TradeData> fadeTheMoveTrades() {
		return new Predicate<TradeData>() {

			@Override
			public boolean test(TradeData trade) {
				double pipForInstrument = instrumentService
						.getPipForInstrument(new TradeableInstrument<>(trade.getInstrument()));
				double pipsWon = Math.abs(trade.getClosePrice() - trade.getTransactionPrice()) / pipForInstrument;
				return pipsWon >= 9.0 && pipsWon <= 11.0;
			}
		};

	}

	public static void main(String[] args) {
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("oanda-transactions-app.xml");
		AnalyseFadeTheMoveTradesService service = appContext.getBean(AnalyseFadeTheMoveTradesService.class);
		service.analyseTakeProfitTrades();
		appContext.close();
	}
}
