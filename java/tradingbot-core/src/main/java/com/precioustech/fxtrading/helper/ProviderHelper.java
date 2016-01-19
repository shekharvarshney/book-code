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
package com.precioustech.fxtrading.helper;

/**
 *
 * @param <T>
 *            The type of Long/Short notation
 */
public interface ProviderHelper<T> {

	/**
	 * 
	 * @param instrument
	 *            in ISO currency standard, such as GBPUSD
	 * @return currency pair denoted in the platform specific format
	 */
	String fromIsoFormat(String instrument);

	/**
	 * 
	 * @param instrument
	 *            in platform specific format such as GBP_USD
	 * @return currency pair denoted in ISO format
	 */
	String toIsoFormat(String instrument);

	/**
	 * 
	 * @param instrument
	 *            in a 7 character format, separated by an arbitrary separator
	 *            character like -,/,_
	 * @return currency pair denoted in the platform specific format
	 */
	String fromPairSeparatorFormat(String instrument);

	/**
	 * 
	 * @param instrument
	 *            denoted as a hashtag, for e.g. #GBPUSD
	 * @return currency pair denoted in the platform specific format
	 */
	String fromHashTagCurrency(String instrument);

	/**
	 * 
	 * @return T that denotes the action of Buying the currency pair on the
	 *         platform
	 */
	T getLongNotation();

	/**
	 * 
	 * @return T that denotes the action of Selling the currency pair on the
	 *         platform
	 */
	T getShortNotation();
}
