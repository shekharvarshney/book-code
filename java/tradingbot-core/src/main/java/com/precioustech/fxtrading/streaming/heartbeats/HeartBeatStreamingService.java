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
package com.precioustech.fxtrading.streaming.heartbeats;

/**
 * A service that provides streaming heartbeats from the trading platform. The
 * service provided in the end by the platform may not be streaming at all but
 * some sort of regular callbacks in order to indicate that the connection is
 * alive. A loss of heartbeats may indicate a general failure to receive any
 * trade/order events and/or market data from the trading platform. Therefore
 * any monitoring of the application may involve directly interacting with this
 * service to raise alerts/notifications.
 * 
 * @author Shekhar Varshney
 *
 */
public interface HeartBeatStreamingService {

	/**
	 * Start the service in order to receive heartbeats from the trading
	 * platform. Ideally the implementation would make sure that multiple
	 * heartbeat connections/handlers are not created for the same kind of
	 * service. Depending on the trading platform, there may be a single
	 * heartbeat for all services or a dedicated one for services such as market
	 * data, trade/order events etc.
	 */
	void startHeartBeatStreaming();

	/**
	 * Stop the service in order to stop receiving heartbeats. The
	 * implementation must dispose any resources/connections in a suitable
	 * manner so as not to cause any resource leaks.
	 */
	void stopHeartBeatStreaming();

	/**
	 * 
	 * @return heartBeat source id which identifies the source for which this
	 *         service is providing heartbeats. This is useful to keep track all
	 *         sources which are heartbeating and can be individually monitored
	 *         on a regular basis. On some platforms there may be a dedicated
	 *         single heartbeat service for ALL in which case this may not be as
	 *         useful.
	 */
	String getHeartBeatSourceId();
}
