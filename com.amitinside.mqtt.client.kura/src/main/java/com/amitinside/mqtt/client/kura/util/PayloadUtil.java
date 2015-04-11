package com.amitinside.mqtt.client.kura.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Properties;

public class PayloadUtil {

	public static Properties parsePayloadFromString(String text) {
		final Properties p = new Properties();
		try {
			p.load(new StringReader(text));
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return p;
	}

	public static String parsePayloadFromProto(Map<String, Object> map) {
		final Properties properties = new Properties();
		for (final Map.Entry<String, Object> entry : map.entrySet()) {
			properties.setProperty(entry.getKey(), (String) entry.getValue());
		}
		final StringWriter writer = new StringWriter();
		properties.list(new PrintWriter(writer));
		return writer.getBuffer().toString();
	}

	public static String generateRequestId() {
		final SecureRandom random = new SecureRandom();
		final String str = new BigInteger(130, random).toString(32);
		return "REQUEST_" + str.toUpperCase();
	}

	public static String generateHintSubscriptionTopic(String topic,
			String requestId, String requesterClientId) {
		final String SEPARATOR = "/";
		final String[] topicNamespace = topic.split(SEPARATOR);
		final StringBuffer buffer = new StringBuffer();
		if (topic.startsWith("$EDC"))
			buffer.append(topicNamespace[0]).append(SEPARATOR)
					.append(topicNamespace[1]).append(SEPARATOR)
					.append(requesterClientId).append(SEPARATOR)
					.append(topicNamespace[3]).append(SEPARATOR)
					.append("REPLY").append(SEPARATOR).append(requestId);
		return buffer.toString();
	}
}
