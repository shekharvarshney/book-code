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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.precioustech.fxtrading.streaming.heartbeats.HeartBeatStreamingService;

public class DefaultHeartBeatServiceTest {

	private final static String TESTSTREAM = "TESTSTREAM";

	@Test
	public void heartBeatTest() throws Exception {
		HeartBeatStreamingService heartBeatStreamingService = mock(HeartBeatStreamingService.class);
		when(heartBeatStreamingService.getHeartBeatSourceId()).thenReturn(TESTSTREAM);
		DefaultHeartBeatService service = new DefaultHeartBeatService(Lists.newArrayList(heartBeatStreamingService));
		service.warmUpTime = 1L;
		service.init();
		EventBus eventBus = new EventBus();
		eventBus.register(service);
		HeartBeatCallback<DateTime> heartBeatCallBack = new HeartBeatCallbackImpl<DateTime>(eventBus);
		DateTime now = DateTime.now();
		HeartBeatPayLoad<DateTime> payload = new HeartBeatPayLoad<DateTime>(now.minusMinutes(2), TESTSTREAM);
		heartBeatCallBack.onHeartBeat(payload);
		verify(heartBeatStreamingService, times(1)).startHeartBeatStreaming();
		heartBeatCallBack.onHeartBeat(new HeartBeatPayLoad<DateTime>(DateTime.now(), TESTSTREAM));
		service.serviceUp = false;
		do {
			Thread.sleep(2L);
		} while (service.heartBeatsObserverThread.isAlive());
	}
}
