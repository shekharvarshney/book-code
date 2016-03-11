package com.precioustech.fxtrading.oanda.restapi.account.transaction.dao;

import java.util.Collection;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.precioustech.fxtrading.oanda.restapi.account.transaction.entities.OandaAccount;

public class OandaAccountDao extends AbstractDao {

	@Transactional
	public Collection<OandaAccount> allAccounts() {
		@SuppressWarnings("rawtypes")
		List results = getSession().createCriteria(OandaAccount.class).list();
		Collection<OandaAccount> accounts = Lists.newArrayList();
		for (Object row : results) {
			accounts.add((OandaAccount) row);
		}
		return accounts;
	}
}
