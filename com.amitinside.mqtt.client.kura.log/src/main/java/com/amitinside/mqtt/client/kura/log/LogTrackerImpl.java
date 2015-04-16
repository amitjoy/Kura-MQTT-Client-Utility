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
