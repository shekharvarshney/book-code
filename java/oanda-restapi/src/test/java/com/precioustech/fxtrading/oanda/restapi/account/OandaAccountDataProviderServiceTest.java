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
package com.precioustech.fxtrading.oanda.restapi.account;

import static com.precioustech.fxtrading.oanda.restapi.OandaTestConstants.accessToken;
import static com.precioustech.fxtrading.oanda.restapi.OandaTestConstants.accountId;
import static com.precioustech.fxtrading.oanda.restapi.OandaTestConstants.url;
import static com.precioustech.fxtrading.oanda.restapi.OandaTestConstants.userName;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;

import com.precioustech.fxtrading.account.Account;
import com.precioustech.fxtrading.oanda.restapi.OandaTestConstants;
import com.precioustech.fxtrading.oanda.restapi.OandaTestUtils;

public class OandaAccountDataProviderServiceTest {

	private OandaAccountDataProviderService createSpyAndCommonStuff(String fname,
			OandaAccountDataProviderService service) throws Exception {
		OandaAccountDataProviderService spy = spy(service);

		CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
		when(spy.getHttpClient()).thenReturn(mockHttpClient);

		OandaTestUtils.mockHttpInteraction(fname, mockHttpClient);

		return spy;
	}

	// TODO: this test logs "java.io.IOException: Stream Closed" because of same
	// FileInputStream reread once closed
	@Test
	public void allAccountsTest() throws Exception {
		final OandaAccountDataProviderService service = new OandaAccountDataProviderService(url, userName, accessToken);
		assertEquals("https://api-fxtrade.oanda.com/v1/accounts?username=testTrader", service.getAllAccountsUrl());
		OandaAccountDataProviderService spy = createSpyAndCommonStuff("src/test/resources/accountsAll.txt", service);
		spy.getLatestAccountInfo();
		verify(spy, times(1)).getSingleAccountUrl(1898212L);
		verify(spy, times(1)).getSingleAccountUrl(2093221L);
	}

	@Test
	public void accountIdTest() throws Exception {
		final OandaAccountDataProviderService service = new OandaAccountDataProviderService(url, userName, accessToken);
		assertEquals("https://api-fxtrade.oanda.com/v1/accounts/123456", service.getSingleAccountUrl(accountId));

		OandaAccountDataProviderService spy = createSpyAndCommonStuff("src/test/resources/account123456.txt", service);
		Account<Long> accInfo = spy.getLatestAccountInfo(accountId);
		assertNotNull(accInfo);
		assertEquals("CHF", accInfo.getCurrency());
		assertEquals(0.05, accInfo.getMarginRate(), OandaTestConstants.precision);
		assertEquals(-897.1, accInfo.getUnrealisedPnl(), OandaTestConstants.precision);
	}
}
