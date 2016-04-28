package com.precioustech.fxtrading.oanda.restapi.account.transaction.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity
@Table(name = "take_profit_analysis")
public class TakeProfitAnalysis implements Serializable {

	private static final long serialVersionUID = 5241266281932422480L;

	@Id
	@Column(name = "transaction_id")
	private Long transactionId;

	@Column(name = "max_best_price", nullable = false)
	private Double maxBestPrice;

	@Column(name = "best_price_time", nullable = false)
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime bestPriceTime;

	public Long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}

	public Double getMaxBestPrice() {
		return maxBestPrice;
	}

	public void setMaxBestPrice(Double maxBestPrice) {
		this.maxBestPrice = maxBestPrice;
	}

	public DateTime getBestPriceTime() {
		return bestPriceTime;
	}

	public void setBestPriceTime(DateTime bestPriceTime) {
		this.bestPriceTime = bestPriceTime;
	}
}
