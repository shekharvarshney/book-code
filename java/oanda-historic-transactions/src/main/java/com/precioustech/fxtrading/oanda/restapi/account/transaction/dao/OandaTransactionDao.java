package com.precioustech.fxtrading.oanda.restapi.account.transaction.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Maps;
import com.precioustech.fxtrading.oanda.restapi.account.transaction.entities.OandaAccount;
import com.precioustech.fxtrading.oanda.restapi.account.transaction.entities.OandaTransaction;

public class OandaTransactionDao extends AbstractDao {



	@Transactional
	public OandaTransaction findByTransactionId(Long transactionId) {
		Criteria criteria = getSession().createCriteria(OandaTransaction.class);
		criteria.add(Restrictions.idEq(transactionId));
		return (OandaTransaction) criteria.uniqueResult();
	}

	@Transactional
	public OandaTransaction findByLinkedTransactionId(Long lnkTransactionId) {
		Criteria criteria = getSession().createCriteria(OandaTransaction.class);
		criteria.add(Restrictions.eq("linkedTransactionId", lnkTransactionId));
		return (OandaTransaction) criteria.uniqueResult();
	}



	@SuppressWarnings("unchecked")
	@Transactional
	public Collection<OandaTransaction> findByTransactionType(String transactionType) {
		Criteria criteria = getSession().createCriteria(OandaTransaction.class);
		criteria.add(Restrictions.eq("transactionType", transactionType));
		return (Collection<OandaTransaction>) criteria.list();
	}

	@Transactional
	public void saveTransaction(OandaTransaction transaction) {
		getSession().saveOrUpdate(transaction);
	}

	@Transactional
	public Map<OandaAccount, Long> transactionsCountByAccount() {
		final String property = "account";
		Criteria criteria = getSession().createCriteria(OandaTransaction.class).setProjection(
				Projections.projectionList().add(Projections.groupProperty(property)).add(Projections.count(property)));
		@SuppressWarnings("rawtypes")
		List results = criteria.list();
		Map<OandaAccount, Long> accountCountMap = Maps.newLinkedHashMap();
		for (Object row : results) {
			Object[] rowData = (Object[]) row;
			accountCountMap.put((OandaAccount) rowData[0], (Long) rowData[1]);
		}
		return accountCountMap;
	}

	@Transactional
	public Long transactionMaxIdForAccount(OandaAccount account) {
		Criteria criteria = getSession().createCriteria(OandaTransaction.class)
				.setProjection(Projections.projectionList().add(Projections.max("transactionId")))
				.add(Restrictions.eq("account", account));
		return (Long) criteria.uniqueResult();
	}

	@Transactional
	public Map<OandaAccount, Long> transactionsMaxIdByAccount() {
		final String property = "account";
		Criteria criteria = getSession().createCriteria(OandaTransaction.class).setProjection(Projections
				.projectionList().add(Projections.groupProperty(property)).add(Projections.max("transactionId")));
		@SuppressWarnings("rawtypes")
		List results = criteria.list();
		Map<OandaAccount, Long> accountCountMap = Maps.newLinkedHashMap();
		for (Object row : results) {
			Object[] rowData = (Object[]) row;
			accountCountMap.put((OandaAccount) rowData[0], (Long) rowData[1]);
		}
		return accountCountMap;
	}

	@Transactional
	public Double pnlForAccount(OandaAccount account) {
		return null;
	}

	@Transactional
	public Double pnlForInstrument(String instrument) {
		return null;
	}

	@Transactional
	public Double pnlForInstrumentAndAccount(String instrument, OandaAccount account) {
		return null;
	}

	@Transactional
	public List<OandaTransaction> transactionsFromTo(DateTime from, DateTime to) {
		return null;
	}

	@Transactional
	public List<OandaTransaction> transactionsFromToForAccount(DateTime from, DateTime to, OandaAccount account) {
		return null;
	}

}
