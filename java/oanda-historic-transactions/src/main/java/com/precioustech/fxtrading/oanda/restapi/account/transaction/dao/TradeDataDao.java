package com.precioustech.fxtrading.oanda.restapi.account.transaction.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.transaction.annotation.Transactional;

import com.precioustech.fxtrading.oanda.restapi.account.transaction.entities.OandaTransactionResult;
import com.precioustech.fxtrading.oanda.restapi.account.transaction.entities.TradeData;

public class TradeDataDao extends AbstractDao {

	@Transactional
	public List<TradeData> findTradeDataWithMissingTransactionResults() {
		Session session = getSession();
		Criteria criteria = session.createCriteria(TradeData.class, "td");
		DetachedCriteria detCriteria = DetachedCriteria.forClass(OandaTransactionResult.class, "tr");
		detCriteria.add(Restrictions.eqProperty("td.transactionId", "tr.transactionId"));
		criteria.add(Subqueries.notExists(detCriteria.setProjection(Projections.property("tr.transactionId"))));
		return criteria.list();
	}

	@Transactional
	public void saveTransactionResult(OandaTransactionResult transactionResult) {
		getSession().saveOrUpdate(transactionResult);
	}
}
