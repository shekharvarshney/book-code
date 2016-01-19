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
package com.precioustech.fxtrading.account;

import java.util.Collection;

/**
 * A provider of Account information. An account information might typically
 * include base currency, leverage, margin available, PNL information etc. Some
 * brokerages allow the creation of various sub accounts or currency wallets.
 * The idea is to give ability to fund these accounts from various currency
 * denominated bank accounts. So for e.g. a user in Switzerland might have a CHF
 * current account but also a EUR savings account. One can then open 2 currency
 * accounts or wallets on the brokerage, denominated in CHF and EUR and these
 * can then be funded by the real bank accounts. Alternatively, one can also
 * just create these multiple currency wallets even if they have just a single
 * source funding currency. When the primary account is funded, a transfer trade
 * can be executed to fund the other currency wallet. For e.g. a user in United
 * Kingdom who just has a GBP account, can open a USD wallet, fund the GBP
 * account and then execute a transfer of a given units of GBP into USD.
 * 
 * @author Shekhar Varshney
 *
 * @param <T>
 *            The type of accountId
 * 
 * @see Account
 */
public interface AccountDataProvider<T> {

	/**
	 * 
	 * @param accountId
	 * @return Account information for the given accountId
	 */
	Account<T> getLatestAccountInfo(T accountId);

	/**
	 * 
	 * @return A collection of ALL accounts available
	 */
	Collection<Account<T>> getLatestAccountInfo();
}
