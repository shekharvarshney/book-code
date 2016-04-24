package com.precioustech.fxtrading.oanda.restapi.account.transaction;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.helper.ProviderHelper;
import com.precioustech.fxtrading.instrument.InstrumentService;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.PipJumpCutOffCalculator;
import com.precioustech.fxtrading.marketdata.historic.CandleStick;
import com.precioustech.fxtrading.marketdata.historic.CandleStickGranularity;
import com.precioustech.fxtrading.marketdata.historic.HistoricMarketDataProvider;
import com.precioustech.fxtrading.oanda.restapi.account.transaction.dao.TradeDataDao;
import com.precioustech.fxtrading.oanda.restapi.account.transaction.entities.OandaTransactionResult;
import com.precioustech.fxtrading.oanda.restapi.account.transaction.entities.TradeData;

public class AnalyseTransactionResultService {

	@Autowired
	private TradeDataDao tradeDataDao;
	@Autowired
	private HistoricMarketDataProvider<String> historicMarketDataProvider;
	@Autowired
	private ProviderHelper<String> providerHelper;
	@Autowired
	private InstrumentService<String> instrumentService;
	@Autowired
	private PipJumpCutOffCalculator<String> pipJumpCutOffCalculator;

	private CandleStickGranularity deriveAppropriateGranularity(int durationOpen) {
		for (CandleStickGranularity granularity : CandleStickGranularity.values()) {
			if (granularity.getGranularityInSeconds() >= CandleStickGranularity.M1.getGranularityInSeconds()) {
				long countReqd = (durationOpen * CandleStickGranularity.M1.getGranularityInSeconds())
						/ granularity.getGranularityInSeconds();
				if (countReqd <= 10000) {
					return granularity;
				}
			}
		}
		throw new IllegalStateException("Should not happen!!");
	}

	public void analyseMissingTransactions() {
		List<TradeData> missingTransactions = this.tradeDataDao.findTradeDataWithMissingTransactionResults();
		for (TradeData tradeData : missingTransactions) {
			final TradingSignal tradeDirection = deriveTradeDirection(tradeData);
			TradeableInstrument<String> instrument = new TradeableInstrument<>(
					providerHelper.fromPairSeparatorFormat(tradeData.getInstrument()));
			List<CandleStick<String>> candles = historicMarketDataProvider.getCandleSticks(instrument,
					deriveAppropriateGranularity(tradeData.getDurationOpen()),
					tradeData.getTransactionOpenTime(), tradeData.getTransactionCloseTime());
			double maxAdversePrice = tradeData.getTransactionPrice();
			for (CandleStick<String> candle : candles) {
				if (tradeDirection == TradingSignal.LONG) {
					maxAdversePrice = candle.getLowPrice() < maxAdversePrice ? candle.getLowPrice() : maxAdversePrice;
				} else {
					maxAdversePrice = candle.getHighPrice() > maxAdversePrice ? candle.getHighPrice() : maxAdversePrice;
				}
			}
			boolean badDecision = tradeData.getPnl() < 0;
			if (!badDecision) {
				double pip = this.instrumentService.getPipForInstrument(instrument);
				double pipDiff = (tradeData.getTransactionPrice() - maxAdversePrice) / pip;
				double pipJumpReqd = this.pipJumpCutOffCalculator.calculatePipJumpCutOff(instrument);
				badDecision = Math.abs(pipDiff) > pipJumpReqd;
			}

			OandaTransactionResult transactionResult = new OandaTransactionResult();
			transactionResult.setBadDecision(badDecision ? "Y" : "N");
			transactionResult.setMaxAdversePrice(maxAdversePrice);
			transactionResult.setTransactionId(tradeData.getTransactionId());
			this.tradeDataDao.saveTransactionResult(transactionResult);
		}
		// System.out.println(missingTransactions.size());
	}

	private TradingSignal deriveTradeDirection(TradeData tradeData) {
		double pnl = tradeData.getPnl();
		double openPrice = tradeData.getTransactionPrice();
		double closePrice = tradeData.getClosePrice();
		if (pnl >= 0) {
			return openPrice > closePrice ? TradingSignal.SHORT : TradingSignal.LONG;
		} else {
			return openPrice > closePrice ? TradingSignal.LONG : TradingSignal.SHORT;
		}
	}

	public static void main(String[] args) {
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("oanda-transactions-app.xml");
		AnalyseTransactionResultService service = appContext.getBean(AnalyseTransactionResultService.class);
		service.analyseMissingTransactions();
		appContext.close();
	}

}
