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
package com.precioustech.fxtrading.tradingbot;

import com.precioustech.fxtrading.BaseTradingConfig;

public class TradingConfig extends BaseTradingConfig {

	private String mailTo;
	private int fadeTheMoveJumpReqdToTrade;
	private int fadeTheMoveDistanceToTrade;
	private int fadeTheMovePipsDesired;
	private int fadeTheMovePriceExpiry;

	public int getFadeTheMovePriceExpiry() {
		return fadeTheMovePriceExpiry;
	}

	public void setFadeTheMovePriceExpiry(int fadeTheMovePriceExpiry) {
		this.fadeTheMovePriceExpiry = fadeTheMovePriceExpiry;
	}

	public String getMailTo() {
		return mailTo;
	}

	public void setMailTo(String mailTo) {
		this.mailTo = mailTo;
	}

	public int getFadeTheMoveJumpReqdToTrade() {
		return fadeTheMoveJumpReqdToTrade;
	}

	public void setFadeTheMoveJumpReqdToTrade(int fadeTheMoveJumpReqdToTrade) {
		this.fadeTheMoveJumpReqdToTrade = fadeTheMoveJumpReqdToTrade;
	}

	public int getFadeTheMoveDistanceToTrade() {
		return fadeTheMoveDistanceToTrade;
	}

	public void setFadeTheMoveDistanceToTrade(int fadeTheMoveDistanceToTrade) {
		this.fadeTheMoveDistanceToTrade = fadeTheMoveDistanceToTrade;
	}

	public int getFadeTheMovePipsDesired() {
		return fadeTheMovePipsDesired;
	}

	public void setFadeTheMovePipsDesired(int fadeTheMovePipsDesired) {
		this.fadeTheMovePipsDesired = fadeTheMovePipsDesired;
	}

}
