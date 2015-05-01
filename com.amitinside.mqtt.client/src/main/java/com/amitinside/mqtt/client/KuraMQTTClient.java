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
package com.amitinside.mqtt.client;

import java.util.Set;

import com.amitinside.mqtt.client.adapter.MessageListener;
import com.amitinside.mqtt.client.kura.message.KuraPayload;

public interface KuraMQTTClient {

	class ConnectionException extends RuntimeException {
		public ConnectionException(String message) {
			super(message);
		}
	}

	public static final String PORT = "1883";

	public static final String PROTOCOL = "tcp";

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
