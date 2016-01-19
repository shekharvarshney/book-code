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
package com.precioustech.fxtrading.tradingbot.events.notification.email;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.precioustech.fxtrading.events.Event;
import com.precioustech.fxtrading.events.EventPayLoad;
import com.precioustech.fxtrading.events.notification.email.EmailContentGenerator;
import com.precioustech.fxtrading.events.notification.email.EmailPayLoad;
import com.precioustech.fxtrading.tradingbot.TradingConfig;

public class EventEmailNotifier<T> {

	private static final Logger LOG = Logger.getLogger(EventEmailNotifier.class);

	@Autowired
	JavaMailSender mailSender;
	@Resource
	Map<Event, EmailContentGenerator<T>> eventEmailContentGeneratorMap;
	@Autowired
	TradingConfig tradingConfig;

	@Subscribe
	@AllowConcurrentEvents
	public void notifyByEmail(EventPayLoad<T> payLoad) {
		Preconditions.checkNotNull(payLoad);
		EmailContentGenerator<T> emailContentGenerator = eventEmailContentGeneratorMap.get(payLoad.getEvent());
		if (emailContentGenerator != null) {
			EmailPayLoad emailPayLoad = emailContentGenerator.generate(payLoad);
			SimpleMailMessage msg = new SimpleMailMessage();
			msg.setSubject(emailPayLoad.getSubject());
			msg.setTo(tradingConfig.getMailTo());
			msg.setText(emailPayLoad.getBody());
			this.mailSender.send(msg);
		} else {
			LOG.warn("No email content generator found for event:" + payLoad.getEvent().name());
		}
	}
}
