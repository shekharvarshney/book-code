package com.precioustech.fxtrading.currency.ecoevents;

import java.io.File;
import java.util.List;

import com.precioustech.fxtrading.currency.ecoevents.entities.CurrencyEconomicEvent;

public interface CurrencyEconomicEventFileParser {

	List<CurrencyEconomicEvent> parseEvents(File ecoEventsFile) throws Exception;
}
