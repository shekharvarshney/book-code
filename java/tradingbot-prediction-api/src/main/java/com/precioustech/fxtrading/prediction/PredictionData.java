package com.precioustech.fxtrading.prediction;

public class PredictionData {

	private final String instrument;
	private final TradingSessionEnum tradingSession;
	private final String badDecision;
	private final DirectionEnum directionEnum;
	private final int count;

	public PredictionData(String instrument, TradingSessionEnum tradingSession, String isBadDecision,
			DirectionEnum directionEnum, int count) {
		super();
		this.instrument = instrument;
		this.tradingSession = tradingSession;
		this.badDecision = isBadDecision;
		this.directionEnum = directionEnum;
		this.count = count;
	}

	public String getInstrument() {
		return instrument;
	}

	public TradingSessionEnum getTradingSession() {
		return tradingSession;
	}

	public String getBadDecision() {
		return badDecision;
	}

	public DirectionEnum getDirectionEnum() {
		return directionEnum;
	}

	public int getCount() {
		return count;
	}
}
