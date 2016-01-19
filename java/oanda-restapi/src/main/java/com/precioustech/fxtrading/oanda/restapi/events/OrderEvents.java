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
package com.precioustech.fxtrading.oanda.restapi.events;

import com.precioustech.fxtrading.events.Event;

public enum OrderEvents implements Event {
	MARKET_ORDER_CREATE,
	STOP_ORDER_CREATE,
	LIMIT_ORDER_CREATE,
	MARKET_IF_TOUCHED_ORDER_CREATE,
	ORDER_UPDATE,
	ORDER_CANCEL,
	ORDER_FILLED;

}
