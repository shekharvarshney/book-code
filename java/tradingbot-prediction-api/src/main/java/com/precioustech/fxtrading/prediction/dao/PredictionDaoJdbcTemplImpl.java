package com.precioustech.fxtrading.prediction.dao;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.precioustech.fxtrading.prediction.DirectionEnum;
import com.precioustech.fxtrading.prediction.PredictionData;
import com.precioustech.fxtrading.prediction.TradingSessionEnum;

public class PredictionDaoJdbcTemplImpl implements PredictionDao {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Transactional
	@Override
	public List<PredictionData> getLearningDataSet() {
		List<PredictionData> learningSet = Lists.newArrayList();
		List<Map<String, Object>> results = this.jdbcTemplate
				.queryForList("select instrument, session, direction, is_bad_decision,"
						+ "count(*) ct from trade_data t1  join oanda_transaction_result t2 "
						+ "on t1.transaction_id=t2.transaction_id "
						+ "group by instrument,  is_bad_decision, session, direction");
		for (Map<String, Object> row : results) {
			String strBadDecision = row.get("is_bad_decision").toString();
			learningSet.add(new PredictionData(row.get("instrument").toString(),
					TradingSessionEnum.valueOf(row.get("session").toString()), strBadDecision,
					DirectionEnum.valueOf(row.get("direction").toString()), ((Long) row.get("ct")).intValue()));
		}
		return learningSet;
	}
}
