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
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;

import com.google.common.collect.Maps;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.precioustech.fxtrading.streaming.heartbeats.HeartBeatStreamingService;

public abstract class AbstractHeartBeatService<T> {

	protected abstract boolean isAlive(HeartBeatPayLoad<T> payLoad);

	protected static final Logger LOG = Logger.getLogger(AbstractHeartBeatService.class);

	protected static final long MAX_HEARTBEAT_DELAY = 60000L;/*we must get a heartbeat within a minute max*/
	private final Map<String, HeartBeatStreamingService> heartBeatProducerMap = Maps.newHashMap();
	private final Map<String, HeartBeatPayLoad<T>> payLoadMap = Maps.newConcurrentMap();
	volatile boolean serviceUp = true;
	protected final Collection<HeartBeatStreamingService> heartBeatStreamingServices;
	protected final long initWait = 10000L;
	long warmUpTime = MAX_HEARTBEAT_DELAY;/*accessibility package friendly for unit testing*/

	public AbstractHeartBeatService(Collection<HeartBeatStreamingService> heartBeatStreamingServices) {
		this.heartBeatStreamingServices = heartBeatStreamingServices;
		for (HeartBeatStreamingService heartBeatStreamingService : heartBeatStreamingServices) {
			this.heartBeatProducerMap.put(heartBeatStreamingService.getHeartBeatSourceId(), heartBeatStreamingService);
		}
	}

	@PostConstruct
	public void init() {
		this.heartBeatsObserverThread.start();
	}

	final Thread heartBeatsObserverThread = new Thread(new Runnable() {

		private void sleep() {
			try {
				Thread.sleep(warmUpTime);/*let the streams start naturally*/
			} catch (InterruptedException e1) {
				LOG.error(e1);
			}
		}

		@Override
		public void run() {
			while (serviceUp) {
				sleep();
				for (Map.Entry<String, HeartBeatStreamingService> entry : heartBeatProducerMap.entrySet()) {
					long startWait = initWait;// start with 2secs
					while (serviceUp && !isAlive(payLoadMap.get(entry.getKey()))) {
						entry.getValue().startHeartBeatStreaming();
						LOG.warn(String
								.format("heartbeat source %s is not responding. just restarted it and will listen for heartbeat after %d ms",
										entry.getKey(), startWait));
						try {
							Thread.sleep(startWait);
						} catch (InterruptedException e) {
							LOG.error(e);
						}
						startWait = Math.min(MAX_HEARTBEAT_DELAY, 2 * startWait);
					}
				}
			}
		}
	}, "HeartBeatMonitorThread");

	@Subscribe
	@AllowConcurrentEvents
	public void handleHeartBeats(HeartBeatPayLoad<T> payLoad) {// jjkmdjwk
		this.payLoadMap.put(payLoad.getHeartBeatSource(), payLoad);
	}

}
