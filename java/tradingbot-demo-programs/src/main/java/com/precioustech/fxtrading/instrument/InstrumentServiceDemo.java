package com.precioustech.fxtrading.instrument;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.precioustech.fxtrading.oanda.restapi.instrument.OandaInstrumentDataProviderService;

public class InstrumentServiceDemo {

	private static final Logger LOG = Logger.getLogger(InstrumentServiceDemo.class);

	private static void usageAndValidation(String[] args) {
		if (args.length != 3) {
			LOG.error("Usage: InstrumentServiceDemo <url> <accountid> <accesstoken>");
			System.exit(1);
		} else {
			if (!StringUtils.isNumeric(args[1])) {
				LOG.error("Argument 2 should be numeric");
				System.exit(1);
			}
		}
	}

	public static void main(String[] args) {
		usageAndValidation(args);
		String url = args[0];
		Long accountId = Long.parseLong(args[1]);
		String accessToken = args[2];

		InstrumentDataProvider<String> instrumentDataProvider = new OandaInstrumentDataProviderService(url, accountId,
				accessToken);

		InstrumentService<String> instrumentService = new InstrumentService<String>(instrumentDataProvider);

		Collection<TradeableInstrument<String>> gbpInstruments = instrumentService.getAllPairsWithCurrency("GBP");

		LOG.info("+++++++++++++++++++++++++++++++ Dumping Instrument Info +++++++++++++++++++++++++++++");
		for (TradeableInstrument<String> instrument : gbpInstruments) {
			LOG.info(instrument);
		}
		LOG.info("+++++++++++++++++++++++Finished Dumping Instrument Info +++++++++++++++++++++++++++++");
		TradeableInstrument<String> euraud = new TradeableInstrument<String>("EUR_AUD");
		TradeableInstrument<String> usdjpy = new TradeableInstrument<String>("USD_JPY");
		TradeableInstrument<String> usdzar = new TradeableInstrument<String>("USD_ZAR");

		Double usdjpyPip = instrumentService.getPipForInstrument(usdjpy);
		Double euraudPip = instrumentService.getPipForInstrument(euraud);
		Double usdzarPip = instrumentService.getPipForInstrument(usdzar);

		LOG.info(String.format("Pip for instrument %s is %1.5f", euraud.getInstrument(), euraudPip));
		LOG.info(String.format("Pip for instrument %s is %1.5f", usdjpy.getInstrument(), usdjpyPip));
		LOG.info(String.format("Pip for instrument %s is %1.5f", usdzar.getInstrument(), usdzarPip));
	}

}
