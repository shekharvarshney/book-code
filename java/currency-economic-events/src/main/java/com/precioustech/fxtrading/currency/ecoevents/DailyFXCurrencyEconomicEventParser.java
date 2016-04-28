package com.precioustech.fxtrading.currency.ecoevents;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.precioustech.fxtrading.currency.ecoevents.entities.CurrencyEconomicEvent;

public class DailyFXCurrencyEconomicEventParser implements CurrencyEconomicEventFileParser {

	private static Map<String, Integer> monthMap = Maps.newLinkedHashMap();

	static {
		monthMap.put("Jan", 1);
		monthMap.put("Feb", 2);
		monthMap.put("Mar", 3);
		monthMap.put("Apr", 4);
		monthMap.put("May", 5);
		monthMap.put("Jun", 6);
		monthMap.put("Jul", 7);
		monthMap.put("Aug", 8);
		monthMap.put("Sep", 9);
		monthMap.put("Oct", 10);
		monthMap.put("Nov", 11);
		monthMap.put("Dec", 12);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CurrencyEconomicEvent> parseEvents(File ecoEventsFile) throws Exception {
		FileInputStream fis = new FileInputStream(ecoEventsFile);
		final int year = deriveYear(ecoEventsFile);
		List<String> lines = IOUtils.readLines(fis);
		List<CurrencyEconomicEvent> events = Lists.newArrayList();
		if (!CollectionUtils.isEmpty(lines)) {
			for (int rowCtr = 1; rowCtr < lines.size(); rowCtr++) {
				String csvRow = lines.get(rowCtr);
				String[] tokens = StringUtils.split(csvRow, ',');
				CurrencyEconomicEvent cee = new CurrencyEconomicEvent();
				cee.setActual(tokens[6]);
				cee.setCurrency(tokens[3].toUpperCase());
				cee.setCurrencyEconomicEventDate(deriveEconomicEventDateInGMT(year, tokens[0], tokens[1], tokens[2]));
				cee.setCurrencyEconomicEventDescription(tokens[4]);
			}
		}
		IOUtils.closeQuietly(fis);
		return events;
	}

	private DateTime deriveEconomicEventDateInGMT(int year, String dateToken, String timeToken, String tzToken) {
		if (!StringUtils.isEmpty(dateToken) && !StringUtils.isEmpty(timeToken) && !StringUtils.isEmpty(tzToken)) {
			String[] timeTokens = StringUtils.split(timeToken, ':');
			String[] dateTokens = StringUtils.split(dateToken);
			// only EST or GMT will be given
			DateTimeZone tz = "EST".equals(tzToken) ? DateTimeZone.forID("America/New_York") : DateTimeZone.UTC;
			DateTime dt = new DateTime(year, monthMap.get(dateTokens[1]), Integer.parseInt(dateTokens[2]),
					Integer.parseInt(timeTokens[0]), Integer.parseInt(timeTokens[1]), tz);
		}
		return null;
	}

	private int deriveYear(File ecoEventsFile) {
		String fname = ecoEventsFile.getName();
		String tokens[] = StringUtils.split(fname, '-');
		return Integer.parseInt(tokens[tokens.length - 1].substring(0, 4));
	}

}
