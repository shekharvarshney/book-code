package com.precioustech.fxtrading.currency.ecoevents.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.precioustech.fxtrading.currency.ecoevents.entities.CurrencyEconomicEvent;

public class CurrencyEconomicEventDao {

	@Autowired
	private SessionFactory sessionFactory;

	@Transactional
	private void saveCurrencyEconomicEvent(CurrencyEconomicEvent currencyEconomicEvent) {
		getSession().saveOrUpdate(currencyEconomicEvent);
	}

	private Session getSession() {
		return this.sessionFactory.getCurrentSession();
	}
}
