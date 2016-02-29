package com.precioustech.fxtrading.ml.data.prepare;

public class MLDataPrepareConfig {

	private double minPercentMovementRequired;
	private String eventDataDirectory;

	public double getMinPercentMovementRequired() {
		return minPercentMovementRequired;
	}

	public void setMinPercentMovementRequired(double minPercentMovementRequired) {
		this.minPercentMovementRequired = minPercentMovementRequired;
	}

	public String getEventDataDirectory() {
		return eventDataDirectory;
	}

	public void setEventDataDirectory(String eventDataDirectory) {
		this.eventDataDirectory = eventDataDirectory;
	}
}
