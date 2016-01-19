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
package com.precioustech.fxtrading.heartbeats;

import java.util.Collection;

import org.joda.time.DateTime;

import com.precioustech.fxtrading.streaming.heartbeats.HeartBeatStreamingService;

public class DefaultHeartBeatService extends AbstractHeartBeatService<DateTime> {

	public DefaultHeartBeatService(Collection<HeartBeatStreamingService> heartBeatStreamingServices) {
		super(heartBeatStreamingServices);
	}

	@Override
	protected boolean isAlive(HeartBeatPayLoad<DateTime> payLoad) {
		return payLoad != null
				&& (DateTime.now().getMillis() - payLoad.getHeartBeatPayLoad().getMillis()) < MAX_HEARTBEAT_DELAY;
	}

}
