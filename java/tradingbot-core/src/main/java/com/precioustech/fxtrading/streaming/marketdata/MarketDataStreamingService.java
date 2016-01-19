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
package com.precioustech.fxtrading.streaming.marketdata;

/**
 * A service that provides streaming market data. Normally the implementation
 * would create a dedicated connection to the trading platform and would receive
 * a stream of prices ideally through a REST service or a callback from the
 * platform. The implementation must handle broken connections and attempt to
 * reconnect in a suitable manner. The service is normally coupled with a
 * heartbeats from the platform which indicates whether the connection is alive
 * or not.
 * 
 * Due to the volume of data expected, it is recommended that the service
 * delegate the handling of market data to another service in order to avoid
 * building up of queue of events, waiting to be processed.
 * 
 * @author Shekhar Varshney
 *
 */
public interface MarketDataStreamingService {

	/**
	 * Start the streaming service which would ideally create a dedicated
	 * connection to the platform or a callback listener. Ideally multiple
	 * connections requesting the same market data should not be created.
	 */
	void startMarketDataStreaming();

	/**
	 * Stop the streaming services and dispose any resources/connections in a
	 * suitable manner such that no resource leaks are created.
	 */
	void stopMarketDataStreaming();

}
