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
package com.precioustech.fxtrading.oanda.restapi;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

public class OandaTestUtils {
	private OandaTestUtils() {

	}

	public static final void mockHttpInteraction(String fname, HttpClient mockHttpClient) throws Exception {
		CloseableHttpResponse mockResp = mock(CloseableHttpResponse.class);
		when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(mockResp);

		HttpEntity mockEntity = mock(HttpEntity.class);

		when(mockResp.getEntity()).thenReturn(mockEntity);

		StatusLine mockStatusLine = mock(StatusLine.class);

		when(mockResp.getStatusLine()).thenReturn(mockStatusLine);
		when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
		when(mockEntity.getContent()).thenReturn(new FileInputStream(fname));
	}
}
