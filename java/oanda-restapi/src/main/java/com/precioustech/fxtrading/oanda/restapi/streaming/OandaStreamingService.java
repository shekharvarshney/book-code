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
package com.precioustech.fxtrading.oanda.restapi.streaming;

import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.heartbeat;
import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.time;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;

import com.precioustech.fxtrading.heartbeats.HeartBeatCallback;
import com.precioustech.fxtrading.heartbeats.HeartBeatPayLoad;
import com.precioustech.fxtrading.oanda.restapi.OandaConstants;
import com.precioustech.fxtrading.oanda.restapi.utils.OandaUtils;
import com.precioustech.fxtrading.streaming.heartbeats.HeartBeatStreamingService;
import com.precioustech.fxtrading.utils.TradingUtils;

public abstract class OandaStreamingService implements HeartBeatStreamingService {
	protected static final Logger LOG = Logger.getLogger(OandaStreamingService.class);
	protected volatile boolean serviceUp = true;
	private final HeartBeatCallback<DateTime> heartBeatCallback;
	private final String hearbeatSourceId;
	protected Thread streamThread;
	private final BasicHeader authHeader;

	protected abstract String getStreamingUrl();

	protected abstract void startStreaming();

	protected abstract void stopStreaming();

	protected CloseableHttpClient getHttpClient() {
		return HttpClientBuilder.create().build();
	}

	protected OandaStreamingService(String accessToken, HeartBeatCallback<DateTime> heartBeatCallback,
			String heartbeatSourceId) {
		this.hearbeatSourceId = heartbeatSourceId;
		this.heartBeatCallback = heartBeatCallback;
		this.authHeader = OandaUtils.createAuthHeader(accessToken);
	}

	protected void handleHeartBeat(JSONObject streamEvent) {
		Long t = Long.parseLong(((JSONObject) streamEvent.get(heartbeat)).get(time).toString());
		heartBeatCallback.onHeartBeat(new HeartBeatPayLoad<DateTime>(new DateTime(TradingUtils.toMillisFromNanos(t)),
				hearbeatSourceId));
	}

	protected BufferedReader setUpStreamIfPossible(CloseableHttpClient httpClient) throws Exception {
		HttpUriRequest httpGet = new HttpGet(getStreamingUrl());
		httpGet.setHeader(authHeader);
		httpGet.setHeader(OandaConstants.UNIX_DATETIME_HEADER);
		LOG.info(TradingUtils.executingRequestMsg(httpGet));
		HttpResponse resp = httpClient.execute(httpGet);
		HttpEntity entity = resp.getEntity();
		if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK && entity != null) {
			InputStream stream = entity.getContent();
			serviceUp = true;
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
			return br;
		} else {
			String responseString = EntityUtils.toString(entity, "UTF-8");
			LOG.warn(responseString);
			return null;
		}
	}

	protected void handleDisconnect(String line) {
		serviceUp = false;
		LOG.warn(String.format("Disconnect message received for stream %s. PayLoad->%s", getHeartBeatSourceId(), line));
	}

	protected boolean isStreaming() {
		return serviceUp;
	}

	@Override
	public void stopHeartBeatStreaming() {
		stopStreaming();
	}

	@Override
	public void startHeartBeatStreaming() {
		startStreaming();
	}

	@Override
	public String getHeartBeatSourceId() {
		return this.hearbeatSourceId;
	}
}
