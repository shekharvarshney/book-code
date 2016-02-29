package com.precioustech.fxtrading.instrument.economicevent;

import org.joda.time.DateTime;

public class InstrumentEconomicEvent {
	private final DateTime eventDate;
	private final String currency, eventDescription;
	private final InstrumentEconomicEventImpact eventImpact;
	private final String previous, actual, forecast;

	public InstrumentEconomicEvent(String currency, String eventDescription, DateTime eventDate,
			InstrumentEconomicEventImpact eventImpact, String previous, String actual, String forecast) {
		this.currency = currency;
		this.eventDescription = eventDescription;
		this.eventImpact = eventImpact;
		this.eventDate = eventDate;
		this.previous = previous;
		this.actual = actual;
		this.forecast = forecast;
	}

	public String getPrevious() {
		return previous;
	}

	public String getActual() {
		return actual;
	}

	public String getForecast() {
		return forecast;
	}

	public DateTime getEventDate() {
		return eventDate;
	}

	@Override
	public String toString() {
		return "InstrumentEconomicEvent [eventDate=" + eventDate + ", currency=" + currency + ", eventDescription="
				+ eventDescription + ", eventSignificance=" + eventImpact + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((currency == null) ? 0 : currency.hashCode());
		result = prime * result + ((eventDate == null) ? 0 : eventDate.hashCode());
		result = prime * result + ((eventDescription == null) ? 0 : eventDescription.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InstrumentEconomicEvent other = (InstrumentEconomicEvent) obj;
		if (currency == null) {
			if (other.currency != null)
				return false;
		} else if (!currency.equals(other.currency))
			return false;
		if (eventDate == null) {
			if (other.eventDate != null)
				return false;
		} else if (!eventDate.equals(other.eventDate))
			return false;
		if (eventDescription == null) {
			if (other.eventDescription != null)
				return false;
		} else if (!eventDescription.equals(other.eventDescription))
			return false;
		return true;
	}

	public String getCurrency() {
		return currency;
	}

	public String getEventDescription() {
		return eventDescription;
	}

	public InstrumentEconomicEventImpact getEventImpact() {
		return eventImpact;
	}

}
