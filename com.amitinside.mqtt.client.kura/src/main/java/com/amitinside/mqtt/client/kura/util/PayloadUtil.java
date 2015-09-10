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
package com.amitinside.mqtt.client.kura.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Properties;

public final class PayloadUtil {

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
			properties.setProperty(entry.getKey(), entry.getValue().toString());
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
		if (topic.startsWith("$EDC") && topicNamespace.length < 4) {
			throw new ArrayIndexOutOfBoundsException();
		}
		if (topic.startsWith("$EDC"))
			buffer.append(topicNamespace[0]).append(SEPARATOR)
					.append(topicNamespace[1]).append(SEPARATOR)
					.append(requesterClientId).append(SEPARATOR)
					.append(topicNamespace[3]).append(SEPARATOR)
					.append("REPLY").append(SEPARATOR).append(requestId);
		return buffer.toString();
	}
}
