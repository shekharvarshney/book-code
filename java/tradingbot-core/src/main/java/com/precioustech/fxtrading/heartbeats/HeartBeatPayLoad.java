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

import org.apache.commons.lang3.StringUtils;

public class HeartBeatPayLoad<T> {

	private final T payLoad;
	private final String source;

	public HeartBeatPayLoad(T payLoad) {
		this(payLoad, StringUtils.EMPTY);
	}

	public HeartBeatPayLoad(T payLoad, String source) {
		this.payLoad = payLoad;
		this.source = source;
	}

	public T getHeartBeatPayLoad() {
		return this.payLoad;
	}

	public String getHeartBeatSource() {
		return this.source;
	}
}
