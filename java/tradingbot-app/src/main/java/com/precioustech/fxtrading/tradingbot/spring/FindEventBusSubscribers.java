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
package com.precioustech.fxtrading.tradingbot.spring;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class FindEventBusSubscribers implements BeanPostProcessor {

	@Autowired
	private EventBus eventBus;
	private static final Logger LOG = Logger.getLogger(FindEventBusSubscribers.class);

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		Method[] beanMethods = bean.getClass().getMethods();
		for (Method beanMethod : beanMethods) {
			if (beanMethod.isAnnotationPresent(Subscribe.class)) {
				eventBus.register(bean);
				LOG.info(String.format("Found event bus subscriber class %s. Subscriber method name=%s", bean
						.getClass().getSimpleName(), beanMethod.getName()));
				break;
			}
		}
		return bean;
	}

}
