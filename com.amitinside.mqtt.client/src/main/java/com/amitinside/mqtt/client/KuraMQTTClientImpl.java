package com.amitinside.mqtt.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
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

import com.amitinside.mqtt.client.kura.log.LogTracker;
import com.amitinside.mqtt.client.kura.message.KuraPayload;
import com.amitinside.mqtt.client.kura.message.payload.operator.KuraPayloadDecoder;
import com.amitinside.mqtt.client.kura.message.payload.operator.KuraPayloadEncoder;

public class KuraMQTTClientImpl implements KuraMQTTClient {

	private String host;
	private String clientId;
	private String errorMsg;
	private volatile LogTracker logTracker;
	private boolean isConnected;
	private final Lock connectionLock;

	protected CallbackConnection connection = null;

	protected Map<String, MessageListener> channels = null;

	protected void bindLogTracker(LogTracker logTracker) {
		this.logTracker = logTracker;
	}

	protected void unbindLogTracker(LogTracker logTracker) {
		if (this.logTracker == logTracker)
			this.logTracker = null;
	}

	/**
	 * Creates a simple MQTT client and connects it to the specified MQTT broker
	 *
	 * @param host
	 *            the hostname of the broker
	 * @param clientId
	 *            the UNIQUE id of this client
	 */
	public KuraMQTTClientImpl() {
		connectionLock = new ReentrantLock();
	}

	@Override
	public boolean connect(String host, String clientId) {

		this.host = host;
		this.clientId = clientId;

		final MQTT mqtt = new MQTT();
		try {
			mqtt.setHost(hostToURI(host));
			mqtt.setClientId(clientId);
		} catch (final URISyntaxException e) {
			logTracker.log("Invalid Host URL");
		}
		try {
			if (connectionLock.tryLock(5, TimeUnit.SECONDS)) {
				safelyConnect(mqtt);
			}
			isConnected = true;
		} catch (final InterruptedException e) {
			isConnected = false;
		} catch (final ConnectionException e) {
			isConnected = false;
		} finally {
			connectionLock.unlock();
		}
		return isConnected;
	}

	private void safelyConnect(final MQTT mqtt) throws ConnectionException {
		if (isConnected)
			disconnect();
		// Initialize channels
		channels = new HashMap<>();
		// Register callbacks
		connection = mqtt.callbackConnection();
		connection.listener(new Listener() {
			@Override
			public void onConnected() {
				logTracker.log("Host connected");
			}

			@Override
			public void onDisconnected() {
				logTracker.log("Host disconnected");
			}

			@Override
			public void onPublish(UTF8Buffer mqttChannel, Buffer mqttMessage,
					Runnable ack) {
				if (channels.containsKey(mqttChannel.toString())) {
					final KuraPayloadDecoder decoder = new KuraPayloadDecoder(
							mqttMessage.toByteArray());
					try {
						channels.get(mqttChannel.toString()).processMessage(
								decoder.buildFromByteArray());
					} catch (final IOException e) {
						logTracker.log("I/O Exception Occurred: "
								+ e.getMessage());
					}
				}
				ack.run();
			}

			@Override
			public void onFailure(Throwable throwable) {
				logTracker.log("Exception Occurred: " + throwable.getMessage());
			}
		});
		// Connect to broker in a blocking fashion
		final CountDownLatch l = new CountDownLatch(1);
		connection.connect(new Callback<Void>() {
			@Override
			public void onSuccess(Void aVoid) {
				l.countDown();
				logTracker.log("Successfully Connected to Host");
			}

			@Override
			public void onFailure(Throwable throwable) {
				errorMsg = "Impossible to CONNECT to the MQTT server, terminating";
				logTracker.log(errorMsg);
				exceptionOccurred(errorMsg);
			}

		});
		try {
			if (!l.await(5, TimeUnit.SECONDS)) {
				errorMsg = "Impossible to CONNECT to the MQTT server: TIMEOUT. Terminating";
				logTracker.log(errorMsg);
				exceptionOccurred(errorMsg);
			}
		} catch (final InterruptedException e) {
			errorMsg = "\"Impossible to CONNECT to the MQTT server, terminating\"";
			logTracker.log(errorMsg);
			exceptionOccurred(errorMsg);

		}
	}

	private void exceptionOccurred(String message) throws ConnectionException {
		throw new ConnectionException(message);
	}

	@Override
	public String getHost() {
		return host;
	}

	@Override
	public String getClientId() {
		return clientId;
	}

	/**
	 * Subscribes to a channel and registers a callback that is fired every time
	 * a new message is published on the channel.
	 *
	 * @param channel
	 *            the channel we are subscribing to
	 * @param callback
	 *            the callback to be fired whenever a message is received on
	 *            this channel
	 */
	@Override
	public void subscribe(final String channel, final MessageListener callback) {
		if (connection != null) {
			if (channels.containsKey(channel))
				return;
			final CountDownLatch l = new CountDownLatch(1);
			final Topic[] topic = { new Topic(channel, QoS.AT_MOST_ONCE) };
			connection.subscribe(topic, new Callback<byte[]>() {
				@Override
				public void onSuccess(byte[] bytes) {
					channels.put(channel, callback);
					l.countDown();
					logTracker.log("Successfully subscribed to " + channel);
				}

				@Override
				public void onFailure(Throwable throwable) {
					logTracker.log("Impossible to SUBSCRIBE to channel \""
							+ channel + "\"");
					l.countDown();
				}
			});
			try {
				l.await();
			} catch (final InterruptedException e) {
				logTracker.log("Impossible to SUBSCRIBE to channel \""
						+ channel + "\"");
			}
		}
	}

	/**
	 * Unsubscribes from a channel.
	 *
	 * @param channel
	 *            the channel we are unsubscribing to
	 */
	@Override
	public void unsubscribe(String channel) {
		if (connection != null) {
			channels.remove(channel);
			final UTF8Buffer[] topic = { UTF8Buffer.utf8(channel) };
			connection.unsubscribe(topic, new Callback<Void>() {
				@Override
				public void onSuccess(Void aVoid) {
					logTracker.log("Successfully unsubscribed");
				}

				@Override
				public void onFailure(Throwable throwable) {
					logTracker.log("Exception occurred while unsubscribing: "
							+ throwable.getMessage());
				}
			});
		}
	}

	/**
	 * Returns the channels the client is currently subscribed to.
	 *
	 * @return set of channels the client is currently subscribed to
	 */
	@Override
	public Set<String> getSubscribedChannels() {
		return channels.keySet();
	}

	/**
	 * Publish a message to a channel
	 *
	 * @param channel
	 *            the channel we are publishing to
	 * @param message
	 *            the message we are publishing
	 */
	@Override
	public void publish(final String channel, KuraPayload payload) {
		if (connection != null) {
			final KuraPayloadEncoder encoder = new KuraPayloadEncoder(payload);
			try {
				connection.publish(channel, encoder.getBytes(),
						QoS.AT_MOST_ONCE, false, new Callback<Void>() {
							@Override
							public void onSuccess(Void aVoid) {
								logTracker.log("Successfully published");
							}

							@Override
							public void onFailure(Throwable throwable) {
								logTracker
										.log("Impossible to publish message to channel "
												+ channel);
							}
						});
			} catch (final IOException e) {
				logTracker.log("I/O Exception Occurred: " + e.getMessage());
			}
		}
	}

	/**
	 * Disconnects the client.
	 */
	@Override
	public void disconnect() {
		if (connection != null) {
			connection.disconnect(new Callback<Void>() {
				@Override
				public void onSuccess(Void aVoid) {
					logTracker.log("Successfully disconnected");
				}

				@Override
				public void onFailure(Throwable throwable) {
					logTracker.log("Error while disconnecting");
				}
			});
		}
	}

	private String hostToURI(String host) {
		return "tcp://" + host + ":1883";
	}

	@Override
	public boolean isConnected() {
		return isConnected;
	}

}
