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

public enum TradeEvents implements Event {
	TRADE_UPDATE,
	TRADE_CLOSE,
	MIGRATE_TRADE_OPEN,
	MIGRATE_TRADE_CLOSE,
	STOP_LOSS_FILLED,
	TAKE_PROFIT_FILLED,
	TRAILING_STOP_FILLED;
}
