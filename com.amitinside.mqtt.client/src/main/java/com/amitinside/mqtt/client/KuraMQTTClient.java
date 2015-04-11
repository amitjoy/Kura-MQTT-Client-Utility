package com.amitinside.mqtt.client;

import java.util.Set;

import com.amitinside.mqtt.client.kura.message.KuraPayload;

public interface KuraMQTTClient {

	class ConnectionException extends RuntimeException {
		public ConnectionException(String message) {
			super(message);
		}
	}

	public void publish(final String channel, KuraPayload payload);

	public void subscribe(final String channel, final MessageListener callback);

	public String getHost();

	public String getClientId();

	public void unsubscribe(String channel);

	public Set<String> getSubscribedChannels();

	public void disconnect();

	public boolean connect(String host, String clientId);

	public boolean isConnected();

}
