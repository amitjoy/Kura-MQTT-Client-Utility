package com.amitinside.mqtt.client;

import com.amitinside.mqtt.client.kura.message.KuraPayload;

public abstract class MessageListener extends MessageAdapter {

	protected void processMessage(KuraPayload payload) {
		logTracker.log("Subscription Message Received");
	}

}
