package com.amitinside.mqtt.client;

import com.amitinside.mqtt.client.kura.log.LogTracker;

public class MessageAdapter {

	protected static volatile LogTracker logTracker;

	protected void bindLogTracker(LogTracker logTracker) {
		this.logTracker = logTracker;
	}

	protected void unbindLogTracker(LogTracker logTracker) {
		if (this.logTracker == logTracker)
			this.logTracker = null;
	}

}
