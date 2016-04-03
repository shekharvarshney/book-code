package com.precioustech.fxtrading.oanda.restapi.account.transaction.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "oanda_transaction_result")
public class OandaTransactionResult implements Serializable {

	private static final long serialVersionUID = 4206656523672868773L;

	@Id
	@Column(name = "transaction_id")
	private Long transactionId;

	@Column(name = "max_adverse_price", nullable = true)
	private Double maxAdversePrice;

	@Column(name = "is_bad_decision", nullable = true)
	private String badDecision;

	public Long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}

	public Double getMaxAdversePrice() {
		return maxAdversePrice;
	}

	public void setMaxAdversePrice(Double maxAdversePrice) {
		this.maxAdversePrice = maxAdversePrice;
	}

	public String getBadDecision() {
		return badDecision;
	}

	public void setBadDecision(String badDecision) {
		this.badDecision = badDecision;
	}

}
