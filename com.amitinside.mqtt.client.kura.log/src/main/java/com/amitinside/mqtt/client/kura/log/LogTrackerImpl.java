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

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Properties;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import com.amitinside.mqtt.client.kura.events.KuraClientEventConstants;

public class LogTrackerImpl implements LogTracker {

	private final List<String> logList = new ArrayList<String>();

	private volatile EventAdmin eventAdmin;

	protected void bindEventAdmin(EventAdmin eventAdmin) {
		this.eventAdmin = eventAdmin;
	}

	protected void unbindEventAdmin(EventAdmin eventAdmin) {
		if (this.eventAdmin == eventAdmin)
			this.eventAdmin = null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void log(String message) {
		if (message != null) {
			if (logList.add(message)) {
				final Dictionary properties = new Properties();
				final Event event = new Event(
						KuraClientEventConstants.LOG_EVENT_TOPIC, properties);
				eventAdmin.sendEvent(event);
			}
		}
	}

	@Override
	public String getLastLog() {
		return logList.get(logList.size() - 1);
	}
}
