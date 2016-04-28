package com.precioustech.fxtrading.currency.ecoevents;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.precioustech.fxtrading.currency.ecoevents.dao.CurrencyEconomicEventDao;

public class LoadEconomicEventsFromFiles {

	@Autowired
	private CurrencyEconomicEventDao currencyEconomicEventDao;

	public static void main(String[] args) {

	}

	@Transactional
	public void load(File baseDir) throws Exception {

	}

}
