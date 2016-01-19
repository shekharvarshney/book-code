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
package com.precioustech.fxtrading.streaming.events;

/**
 * A service that provides trade/order/account related events streaming.
 * Normally the implementation would create a dedicated connection to the
 * platform or register callback listener(s) to receive events. It is
 * recommended that the service delegate the handling of events to specific
 * handlers which can parse and make sense of the different plethora of events
 * received.
 * 
 * @author Shekhar Varshney
 *
 */
public interface EventsStreamingService {

	/**
	 * Start the streaming service which would ideally create a dedicated
	 * connection to the platform or callback listener(s). Ideally multiple
	 * connections requesting the same event types should not be created.
	 */
	void startEventsStreaming();

	/**
	 * Stop the events streaming services and dispose any resources/connections
	 * in a suitable manner such that no resource leaks are created.
	 */
	void stopEventsStreaming();
}
