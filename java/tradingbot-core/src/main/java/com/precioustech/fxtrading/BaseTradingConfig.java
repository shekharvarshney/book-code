/*
 *  Copyright 2015 Shekhar Varshney
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.precioustech.fxtrading;

public class BaseTradingConfig {
	private double minReserveRatio;
	private double minAmountRequired;

	private int maxAllowedQuantity;
	private int maxAllowedNetContracts;
	private double max10yrWmaOffset;

	public double getMinAmountRequired() {
		return minAmountRequired;
	}

	public void setMinAmountRequired(double minAmountRequired) {
		this.minAmountRequired = minAmountRequired;
	}

	public double getMax10yrWmaOffset() {
		return max10yrWmaOffset;
	}

	public void setMax10yrWmaOffset(double max10yrWmaOffset) {
		this.max10yrWmaOffset = max10yrWmaOffset;
	}

	public int getMaxAllowedNetContracts() {
		return maxAllowedNetContracts;
	}

	public void setMaxAllowedNetContracts(int maxAllowedNetContracts) {
		this.maxAllowedNetContracts = maxAllowedNetContracts;
	}

	public double getMinReserveRatio() {
		return minReserveRatio;
	}

	public void setMinReserveRatio(double minReserveRatio) {
		this.minReserveRatio = minReserveRatio;
	}

	public int getMaxAllowedQuantity() {
		return maxAllowedQuantity;
	}

	public void setMaxAllowedQuantity(int maxAllowedQuantity) {
		this.maxAllowedQuantity = maxAllowedQuantity;
	}
}
