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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amitinside.mqtt.client.adapter.MessageListener;
import com.amitinside.mqtt.client.kura.log.LogTracker;
import com.amitinside.mqtt.client.kura.message.KuraPayload;
import com.amitinside.mqtt.client.kura.message.payload.operator.KuraPayloadDecoder;
import com.amitinside.mqtt.client.kura.message.payload.operator.KuraPayloadEncoder;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

/**
 * Implementation of {@link IKuraMQTTClient}
 *
 * @author AMIT KUMAR MONDAL
 * @see IKuraMQTTClient
 *
 */
public final class KuraMQTTClient implements IKuraMQTTClient {

	/**
	 * Kura Client Builder
	 */
	public static class Builder {

		private String clientId;
		private String host;
		private String password;
		private String port;
		private String username;

		public KuraMQTTClient build() {
			return new KuraMQTTClient(this.host, this.port, this.clientId, this.username, this.password);
		}

		public Builder setClientId(final String clientId) {
			this.clientId = clientId;
			return this;
		}

		public Builder setHost(final String host) {
			this.host = host;
			return this;
		}

		public Builder setPassword(final String password) {
			this.password = password;
			return this;
		}

		public Builder setPort(final String port) {
			this.port = port;
			return this;
		}

		public Builder setUsername(final String username) {
			this.username = username;
			return this;
		}
	}

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(KuraMQTTClient.class);
	private static volatile LogTracker logTracker;
	protected Map<String, MessageListener> channels = null;
	private String clientId;
	protected CallbackConnection connection = null;

	private Lock connectionLock;

	private String errorMsg;

	/**
	 * Connection Params
	 */
	private String host;
	private boolean isConnected;
	private String password;

	private String port;

	private String username;

	/**
	 * Required for DS
	 */
	public KuraMQTTClient() {
	}

	/**
	 * Creates a simple MQTT client and connects it to the specified MQTT broker
	 *
	 * @param host
	 *            the hostname of the broker
	 * @param clientId
	 *            the UNIQUE id of this client
	 */
	private KuraMQTTClient(final String host, final String port, final String clientId, final String username,
			final String password) {
		this.host = host;
		this.port = port;
		this.clientId = clientId;
		this.username = username;
		this.password = password;
		this.connectionLock = new ReentrantLock();
	}

	protected synchronized void activate(final ComponentContext context) {
		LOGGER.debug("Activating Service Component");
	}

	protected synchronized void bindLogTracker(final LogTracker logTracker) {
		KuraMQTTClient.logTracker = logTracker;
	}

	/** {@inheritDoc} */
	@Override
	public boolean connect() {

		checkNotNull(this.host);
		checkNotNull(this.port);
		checkNotNull(this.clientId);

		final MQTT mqtt = new MQTT();

		try {

			mqtt.setHost(this.hostToURI(this.host, this.port));
			mqtt.setClientId(this.clientId);
			mqtt.setPassword(this.password);
			mqtt.setUserName(this.username);

		} catch (final URISyntaxException e) {
			logTracker.log("Invalid Host URL");
			LOGGER.error(Throwables.getStackTraceAsString(e));
		}
		try {
			if (this.connectionLock.tryLock(5, TimeUnit.SECONDS)) {
				this.safelyConnect(mqtt);
			}
			this.isConnected = true;
		} catch (final InterruptedException e) {
			this.isConnected = false;
		} catch (final ConnectionException e) {
			this.isConnected = false;
		} finally {
			this.connectionLock.unlock();
		}
		return this.isConnected;
	}

	protected synchronized void deactivate(final ComponentContext context) {
		LOGGER.debug("Dectivating Service Component");
	}

	/** {@inheritDoc} */
	@Override
	public void disconnect() {
		try {
			if (this.connectionLock.tryLock(5, TimeUnit.SECONDS)) {
				this.safelyDisconnect();
			}
		} catch (final Exception e) {
			LOGGER.debug("Exception while disconnecting");
			logTracker.log("Exception while disconnecting");
		}
	}

	/**
	 * Connection Exception triggerer
	 */
	private void exceptionOccurred(final String message) throws ConnectionException {
		throw new ConnectionException(message);
	}

	/** {@inheritDoc} */
	@Override
	public String getClientId() {
		return this.clientId;
	}

	/** {@inheritDoc} */
	@Override
	public String getHost() {
		return this.host;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> getSubscribedChannels() {
		return this.channels.keySet();
	}

	/**
	 * Returns the MQTT URI Scheme
	 */
	private String hostToURI(final String host, final String port) {
		return PROTOCOL + "://" + host + ":" + port;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isConnected() {
		return this.isConnected;
	}

	/** {@inheritDoc} */
	@Override
	public void publish(final String channel, final KuraPayload payload) {
		if (this.connection != null) {
			final KuraPayloadEncoder encoder = new KuraPayloadEncoder(payload);
			try {
				this.connection.publish(channel, encoder.getBytes(), QoS.AT_MOST_ONCE, false, new Callback<Void>() {
					@Override
					public void onFailure(final Throwable throwable) {
						LOGGER.debug("Impossible to publish message to channel " + channel);
						logTracker.log("Impossible to publish message to channel " + channel);
					}

					@Override
					public void onSuccess(final Void aVoid) {
						LOGGER.debug("Successfully published");
						logTracker.log("Successfully published");
					}
				});
			} catch (final IOException e) {
				LOGGER.debug("I/O Exception Occurred: " + e.getMessage());
				logTracker.log("I/O Exception Occurred: " + e.getMessage());
			}
		}
	}

	/**
	 * Connect in a thread safe manner
	 */
	private void safelyConnect(final MQTT mqtt) throws ConnectionException {
		if (this.isConnected) {
			this.disconnect();
		}
		// Initialize channels
		this.channels = Maps.newHashMap();
		// Register callbacks
		this.connection = mqtt.callbackConnection();
		this.connection.listener(new Listener() {
			@Override
			public void onConnected() {
				LOGGER.debug("Host connected");
			}

			@Override
			public void onDisconnected() {
				LOGGER.debug("Host disconnected");
			}

			@Override
			public void onFailure(final Throwable throwable) {
				LOGGER.debug("Exception Occurred: " + throwable.getMessage());
			}

			@Override
			public void onPublish(final UTF8Buffer mqttChannel, final Buffer mqttMessage, final Runnable ack) {
				if (KuraMQTTClient.this.channels.containsKey(mqttChannel.toString())) {
					final KuraPayloadDecoder decoder = new KuraPayloadDecoder(mqttMessage.toByteArray());

					try {
						KuraMQTTClient.this.channels.get(mqttChannel.toString())
								.processMessage(decoder.buildFromByteArray());
					} catch (final IOException e) {
						logTracker.log("I/O Exception Occurred: " + e.getMessage());
						LOGGER.debug("I/O Exception Occurred: " + e.getMessage());
					}
				}
				ack.run();
			}
		});
		// Connect to broker in a blocking fashion
		final CountDownLatch l = new CountDownLatch(1);
		this.connection.connect(new Callback<Void>() {
			@Override
			public void onFailure(final Throwable throwable) {
				KuraMQTTClient.this.errorMsg = "Impossible to CONNECT to the MQTT server, terminating";
				LOGGER.debug(KuraMQTTClient.this.errorMsg);
				logTracker.log("Exception Occurred: " + throwable.getMessage());
			}

			@Override
			public void onSuccess(final Void aVoid) {
				l.countDown();
				LOGGER.debug("Successfully Connected to Host");
				logTracker.log("Successfully Connected to Host");
			}

		});
		try {
			if (!l.await(5, TimeUnit.SECONDS)) {
				this.errorMsg = "Impossible to CONNECT to the MQTT server: TIMEOUT. Terminating";
				LOGGER.debug(this.errorMsg);
				logTracker.log(this.errorMsg);
				this.exceptionOccurred(this.errorMsg);
			}
		} catch (final InterruptedException e) {
			this.errorMsg = "\"Impossible to CONNECT to the MQTT server, terminating\"";
			LOGGER.debug(this.errorMsg);
			logTracker.log(this.errorMsg);
			this.exceptionOccurred(this.errorMsg);
		}
	}

	/**
	 * Disconnects the client in a thread safe way
	 */
	private void safelyDisconnect() {
		if (this.connection != null) {
			this.connection.disconnect(new Callback<Void>() {
				@Override
				public void onFailure(final Throwable throwable) {
					LOGGER.debug("Error while disconnecting");
				}

				@Override
				public void onSuccess(final Void aVoid) {
					LOGGER.debug("Successfully disconnected");
				}
			});
		}
	}

	/** {@inheritDoc} */
	@Override
	public void subscribe(final String channel, final MessageListener callback) {
		if (this.connection != null) {
			if (this.channels.containsKey(channel)) {
				return;
			}
			final CountDownLatch l = new CountDownLatch(1);
			final Topic[] topic = { new Topic(channel, QoS.AT_MOST_ONCE) };
			this.connection.subscribe(topic, new Callback<byte[]>() {
				@Override
				public void onFailure(final Throwable throwable) {
					LOGGER.debug("Impossible to SUBSCRIBE to channel \"" + channel + "\"");
					logTracker.log("Impossible to SUBSCRIBE to channel \"" + channel + "\"");
					l.countDown();
				}

				@Override
				public void onSuccess(final byte[] bytes) {
					KuraMQTTClient.this.channels.put(channel, callback);
					l.countDown();
					logTracker.log("Successfully subscribed to " + channel);
					LOGGER.debug("Successfully subscribed to " + channel);
				}
			});
			try {
				l.await();
			} catch (final InterruptedException e) {
				LOGGER.debug("Impossible to SUBSCRIBE to channel \"" + channel + "\"");
				logTracker.log("Impossible to SUBSCRIBE to channel \"" + channel + "\"");
			}
		}
	}

	protected synchronized void unbindLogTracker(final LogTracker logTracker) {
		if (KuraMQTTClient.logTracker == logTracker) {
			KuraMQTTClient.logTracker = null;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void unsubscribe(final String channel) {
		if (this.connection != null) {
			this.channels.remove(channel);
			final UTF8Buffer[] topic = { UTF8Buffer.utf8(channel) };
			this.connection.unsubscribe(topic, new Callback<Void>() {
				@Override
				public void onFailure(final Throwable throwable) {
					LOGGER.debug("Exception occurred while unsubscribing: " + throwable.getMessage());
					logTracker.log("Exception occurred while unsubscribing");
				}

				@Override
				public void onSuccess(final Void aVoid) {
					LOGGER.debug("Successfully unsubscribed");
					logTracker.log("Successfully unsubscribed");
				}
			});
		}
	}

}
