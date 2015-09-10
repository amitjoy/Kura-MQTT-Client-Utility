/*******************************************************************************
 * Copyright (C) 2015 - Amit Kumar Mondal <admin@amitinside.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.amitinside.mqtt.client.kura.log;

import java.util.Dictionary;
import java.util.List;
import java.util.Properties;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import com.amitinside.mqtt.client.kura.events.KuraClientEventConstants;
import com.google.common.collect.Lists;

/**
 * Implementation of {@link LogTracker}
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public final class LogTrackerImpl implements LogTracker {

	/**
	 * Event Admin Reference
	 */
	private volatile EventAdmin eventAdmin;

	/**
	 * Lists of log to be stored
	 */
	private final List<String> logList = Lists.newArrayList();

	/**
	 * Callback when {@link EventAdmin} is registered
	 */
	protected void bindEventAdmin(final EventAdmin eventAdmin) {
		if (this.eventAdmin == null) {
			this.eventAdmin = eventAdmin;
		}
	}

	/** {@inheritDoc}} */
	@Override
	public String getLastLog() {
		return this.logList.get(this.logList.size() - 1);
	}

	/** {@inheritDoc}} */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void log(final String message) {
		if (message != null) {
			if (this.logList.add(message)) {
				final Dictionary properties = new Properties();
				final Event event = new Event(KuraClientEventConstants.LOG_EVENT_TOPIC, properties);
				this.eventAdmin.sendEvent(event);
			}
		}
	}

	/**
	 * Callback when {@link EventAdmin} is deregistered
	 */
	protected void unbindEventAdmin(final EventAdmin eventAdmin) {
		if (this.eventAdmin == eventAdmin) {
			this.eventAdmin = null;
		}
	}
}
