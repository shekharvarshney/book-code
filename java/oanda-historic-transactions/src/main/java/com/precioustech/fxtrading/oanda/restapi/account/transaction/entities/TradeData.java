package com.precioustech.fxtrading.oanda.restapi.account.transaction.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity
@Table(name = "trade_data")
public class TradeData implements Serializable {

	private static final long serialVersionUID = -7944286837900980309L;

	@Id
	@Column(name = "transaction_id")
	private Long transactionId;

	@Column(name = "instrument", nullable = false)
	private String instrument;

	@Column(name = "units", nullable = true)
	private Long units;

	@Column(name = "close_price", nullable = true)
	private Double closePrice;

	@Column(name = "pnl", nullable = true)
	private Double pnl;

	@Column(name = "account_id", nullable = false)
	private Long accountId;

	@Column(name = "transaction_close", nullable = true)
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime transactionCloseTime;

	@Column(name = "transaction_price", nullable = true)
	private Double transactionPrice;

	@Column(name = "transaction_open", nullable = false)
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime transactionOpenTime;

	@Column(name = "transaction_type", nullable = false)
	private String transactionType;

	@Column(name = "duration_open", nullable = false)
	private Integer durationOpen;

	public Long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}

	public String getInstrument() {
		return instrument;
	}

	public void setInstrument(String instrument) {
		this.instrument = instrument;
	}

	public Long getUnits() {
		return units;
	}

	public void setUnits(Long units) {
		this.units = units;
	}

	public Double getClosePrice() {
		return closePrice;
	}

	public void setClosePrice(Double closePrice) {
		this.closePrice = closePrice;
	}

	public Double getPnl() {
		return pnl;
	}

	public void setPnl(Double pnl) {
		this.pnl = pnl;
	}

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public DateTime getTransactionCloseTime() {
		return transactionCloseTime;
	}

	public void setTransactionCloseTime(DateTime transactionClose) {
		this.transactionCloseTime = transactionClose;
	}

	public Double getTransactionPrice() {
		return transactionPrice;
	}

	public void setTransactionPrice(Double transactionPrice) {
		this.transactionPrice = transactionPrice;
	}

	public DateTime getTransactionOpenTime() {
		return transactionOpenTime;
	}

	public void setTransactionOpenTime(DateTime transactionOpen) {
		this.transactionOpenTime = transactionOpen;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public Integer getDurationOpen() {
		return durationOpen;
	}

	public void setDurationOpen(Integer durationOpen) {
		this.durationOpen = durationOpen;
	}

}
