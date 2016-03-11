package com.precioustech.fxtrading.oanda.restapi.account.transaction.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity
@Table(name = "oanda_transaction_history")
public class OandaTransaction implements Serializable {

	private static final long serialVersionUID = 2342622093080627991L;

	@Id
	@Column(name = "transaction_id")
	private Long transactionId;

	@Column(name = "transaction_type", nullable = false)
	private String transactionType;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "account_id")
	private OandaAccount account;

	@Column(name = "units", nullable = true)
	private Long units;

	@Column(name = "transaction_time", nullable = false)
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime transactionTime;

	@Column(name = "price", nullable = true)
	private Double price;

	@Column(name = "interest", nullable = true)
	private Double interest;

	@Column(name = "pnl", nullable = true)
	private Double pnl;

	@Column(name = "lnk_transaction_id", nullable = true)
	private Long linkedTransactionId;

	@Column(name = "instrument", nullable = false)
	private String instrument;

	public String getInstrument() {
		return instrument;
	}

	public void setInstrument(String instrument) {
		this.instrument = instrument;
	}

	public Long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public OandaAccount getAccount() {
		return this.account;
	}

	public void setAccount(OandaAccount account) {
		this.account = account;
	}

	public Long getUnits() {
		return units;
	}

	public void setUnits(Long units) {
		this.units = units;
	}

	public DateTime getTransactionTime() {
		return transactionTime;
	}

	public void setTransactionTime(DateTime transactionTime) {
		this.transactionTime = transactionTime;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Double getInterest() {
		return interest;
	}

	public void setInterest(Double interest) {
		this.interest = interest;
	}

	public Double getPnl() {
		return pnl;
	}

	public void setPnl(Double pnl) {
		this.pnl = pnl;
	}

	public Long getLinkedTransactionId() {
		return linkedTransactionId;
	}

	public void setLinkedTransactionId(Long linkedTransactionId) {
		this.linkedTransactionId = linkedTransactionId;
	}

}
