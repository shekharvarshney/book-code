package com.precioustech.fxtrading.currency.ecoevents.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity
@Table(name = "currency_events")
public class CurrencyEconomicEvent implements Serializable {
	private static final long serialVersionUID = 4007702478559675700L;

	@Id
	@Column(name = "currency_event_id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer currencyEconomicEventId;

	@Column(name = "currency_event_date", nullable = true)
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime currencyEconomicEventDate;

	@Column(name = "currency", nullable = true)
	private String currency;

	@Column(name = "currency_event_descr", nullable = true)
	private String currencyEconomicEventDescription;

	@Column(name = "importance", nullable = true)
	private String importance;

	@Column(name = "actual", nullable = true)
	private String actual;

	@Column(name = "forecast", nullable = true)
	private String forecast;

	@Column(name = "previous", nullable = true)
	private String previous;

	public Integer getCurrencyEconomicEventId() {
		return currencyEconomicEventId;
	}

	public void setCurrencyEconomicEventId(Integer currencyEconomicEventId) {
		this.currencyEconomicEventId = currencyEconomicEventId;
	}

	public DateTime getCurrencyEconomicEventDate() {
		return currencyEconomicEventDate;
	}

	public void setCurrencyEconomicEventDate(DateTime currencyEconomicEventDate) {
		this.currencyEconomicEventDate = currencyEconomicEventDate;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getCurrencyEconomicEventDescription() {
		return currencyEconomicEventDescription;
	}

	public void setCurrencyEconomicEventDescription(String currencyEconomicEventDescription) {
		this.currencyEconomicEventDescription = currencyEconomicEventDescription;
	}

	public String getImportance() {
		return importance;
	}

	public void setImportance(String importance) {
		this.importance = importance;
	}

	public String getActual() {
		return actual;
	}

	public void setActual(String actual) {
		this.actual = actual;
	}

	public String getForecast() {
		return forecast;
	}

	public void setForecast(String forecast) {
		this.forecast = forecast;
	}

	public String getPrevious() {
		return previous;
	}

	public void setPrevious(String previous) {
		this.previous = previous;
	}

}
