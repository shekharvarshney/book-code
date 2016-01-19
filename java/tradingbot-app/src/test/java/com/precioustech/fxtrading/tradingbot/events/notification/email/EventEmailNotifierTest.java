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

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.google.common.collect.Maps;
import com.precioustech.fxtrading.events.Event;
import com.precioustech.fxtrading.events.EventPayLoad;
import com.precioustech.fxtrading.events.notification.email.EmailContentGenerator;
import com.precioustech.fxtrading.events.notification.email.EmailPayLoad;
import com.precioustech.fxtrading.tradingbot.TradingConfig;

public class EventEmailNotifierTest {

	@Test
	@SuppressWarnings("unchecked")
	public void emailNotifyTest() {
		EventEmailNotifier<String> mailNotifier = new EventEmailNotifier<String>();
		try {
			mailNotifier.notifyByEmail(null);
			fail("Null payload should have resulted in an exception");
		} catch (Exception e) {
			// as expected
		}

		JavaMailSender mailSender = mock(JavaMailSender.class);
		Map<Event, EmailContentGenerator<String>> eventEmailContentGeneratorMap = Maps.newIdentityHashMap();
		Event event = mock(Event.class);
		EmailContentGenerator<String> contentGenerator = mock(EmailContentGenerator.class);
		eventEmailContentGeneratorMap.put(event, contentGenerator);
		TradingConfig tradingConfig = mock(TradingConfig.class);
		mailNotifier.eventEmailContentGeneratorMap = eventEmailContentGeneratorMap;
		mailNotifier.mailSender = mailSender;
		mailNotifier.tradingConfig = tradingConfig;

		EventPayLoad<String> eventPayLoad = new EventPayLoad<String>(mock(Event.class), "null");
		mailNotifier.notifyByEmail(eventPayLoad);
		verify(mailSender, times(0)).send(any(SimpleMailMessage.class));

		eventPayLoad = new EventPayLoad<String>(event, "hello world");
		EmailPayLoad emailPayLoad = new EmailPayLoad("hi", "test email");
		when(contentGenerator.generate(eventPayLoad)).thenReturn(emailPayLoad);
		when(tradingConfig.getMailTo()).thenReturn("info@foobar.com");
		mailNotifier.notifyByEmail(eventPayLoad);
		verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
	}

}
