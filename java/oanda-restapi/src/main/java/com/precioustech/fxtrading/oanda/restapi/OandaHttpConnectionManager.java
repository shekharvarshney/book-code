/*
 *  Copyright 2016 Shekhar Varshney
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

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class OandaHttpConnectionManager {

	private static final OandaHttpConnectionManager INSTANCE = new OandaHttpConnectionManager();

	private PoolingHttpClientConnectionManager poolingManager = new PoolingHttpClientConnectionManager();
	private OandaHttpConnectionManager() {
		this.poolingManager.setMaxTotal(20);
		this.poolingManager.setDefaultMaxPerRoute(10);
	}

	public static OandaHttpConnectionManager getInstance() {
		return INSTANCE;
	}

	public PoolingHttpClientConnectionManager getConnectionPool() {
		return this.poolingManager;
	}
}
