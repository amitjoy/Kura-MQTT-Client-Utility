package com.amitinside.mqtt.client.adapter;

import com.amitinside.mqtt.client.kura.message.KuraPayload;

public abstract class MessageListener extends MessageAdapter {

	public void processMessage(KuraPayload payload) {
		logTracker.log("Subscription Message Received");
	}

}
