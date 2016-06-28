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
import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.precioustech.fxtrading.TradingConstants;
import com.precioustech.fxtrading.events.Event;
import com.precioustech.fxtrading.events.EventPayLoad;
import com.precioustech.fxtrading.events.notification.email.EmailContentGenerator;
import com.precioustech.fxtrading.events.notification.email.EmailPayLoad;
import com.precioustech.fxtrading.remotecontrol.Command;
import com.precioustech.fxtrading.remotecontrol.handlers.CommandHandler;
import com.precioustech.fxtrading.tradingbot.TradingConfig;

public class EventEmailNotifier<T> {

	private static final Logger LOG = Logger.getLogger(EventEmailNotifier.class);

	@Autowired
	JavaMailSender mailSender;
	@Resource
	Map<Event, EmailContentGenerator<T>> eventEmailContentGeneratorMap;
	@Resource
	Map<Command, CommandHandler> remoteControlMap;
	@Autowired
	TradingConfig tradingConfig;
	private static final int TWO = 2;
	private static final String COMMAND_GROUP = "BOT";

	@Subscribe
	@AllowConcurrentEvents
	public void notifyByEmail(EmailPayLoad emailPayLoad) {
		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setSubject(emailPayLoad.getSubject());
		msg.setTo(tradingConfig.getMailTo());
		msg.setText(emailPayLoad.getBody());
		this.mailSender.send(msg);
	}

	@Subscribe
	@AllowConcurrentEvents
	public void notifyByEmail(EventPayLoad<T> payLoad) {
		Preconditions.checkNotNull(payLoad);
		EmailContentGenerator<T> emailContentGenerator = eventEmailContentGeneratorMap.get(payLoad.getEvent());
		if (emailContentGenerator != null) {
			EmailPayLoad emailPayLoad = emailContentGenerator.generate(payLoad);
			notifyByEmail(emailPayLoad);
		} else {
			LOG.warn("No email content generator found for event:" + payLoad.getEvent().name());
		}
	}

	public void receiveEmail(MimeMessage mimeMessage) throws Exception {

		String subject = mimeMessage.getSubject();
		if (!accept(mimeMessage)) {
			LOG.info(String.format("message with subject %s not accepted", subject));
			return;
		}

		// should be like BOT <command> <param1,param2,...paramN>
		// for e.g. BOT SUSPEND GBP
		// for e.g. BOT ORDER GBP_USD LONG
		String tokens[] = subject.trim().toUpperCase().split(TradingConstants.SPACE_RGX);
		Command cmd = strToCommand(tokens[1]);

		CommandHandler handler = this.remoteControlMap.get(cmd);
		if (handler != null) {
			String args[] = null;
			if (tokens.length > TWO) {
				args = new String[tokens.length - TWO];
				for (int i = TWO; i < tokens.length; i++) {
					args[i - TWO] = tokens[i];
				}
			} else {
				args = new String[0];
			}
			handler.handleCommand(args);
		}

	}

	private boolean accept(MimeMessage mimeMessage) throws Exception {
		Address[] address = mimeMessage.getFrom();
		if (address != null && address.length > 0 && address[0] instanceof InternetAddress) {
			InternetAddress iaddr = (InternetAddress)address[0];
			String subject = mimeMessage.getSubject();
			String tokens[] = subject.trim().toUpperCase().split(TradingConstants.SPACE_RGX);
			return tradingConfig.getMailTo().equalsIgnoreCase(iaddr.getAddress()) && tokens.length >= TWO
					&& COMMAND_GROUP.equals(tokens[0])
					&& strToCommand(tokens[1]) != null;
		}
		return false;
	}

	private Command strToCommand(String cmd) {
		try {
			return Command.valueOf(cmd);
		} catch (IllegalArgumentException e) {
			return null;
		}

	}
}
