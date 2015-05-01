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
package com.amitinside.mqtt.client.kura.message.payload.operator;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amitinside.mqtt.client.kura.message.KuraPayload;
import com.amitinside.mqtt.client.kura.message.KuraPosition;
import com.amitinside.mqtt.client.kura.message.protobuf.KuraPayloadProto;
import com.google.protobuf.ByteString;

/**
 * Encodes an KuraPayload class using the Google ProtoBuf binary format.
 */
public class KuraPayloadEncoder {
	private static final Logger s_logger = LoggerFactory
			.getLogger(KuraPayloadEncoder.class);

	private final KuraPayload m_kuraPayload;

	public KuraPayloadEncoder(KuraPayload KuraPayload) {
		m_kuraPayload = KuraPayload;
	}

	public byte[] getBytes() throws IOException {
		final KuraPayloadProto.KuraPayload.Builder protoMsg = KuraPayloadProto.KuraPayload
				.newBuilder();

		if (m_kuraPayload.getTimestamp() != null) {
			protoMsg.setTimestamp(m_kuraPayload.getTimestamp().getTime());
		}

		if (m_kuraPayload.getPosition() != null) {
			protoMsg.setPosition(buildPositionProtoBuf());
		}

		for (final String name : m_kuraPayload.metricNames()) {

			final Object value = m_kuraPayload.getMetric(name);
			try {
				final KuraPayloadProto.KuraPayload.KuraMetric.Builder metricB = KuraPayloadProto.KuraPayload.KuraMetric
						.newBuilder();
				metricB.setName(name);

				setProtoKuraMetricValue(metricB, value);
				metricB.build();

				protoMsg.addMetric(metricB);
			} catch (final Exception eihte) {
				try {
					s_logger.error(
							"During serialization, ignoring metric named: {}. Unrecognized value type: {}.",
							name, value.getClass().getName());
				} catch (final NullPointerException npe) {
					s_logger.error(
							"During serialization, ignoring metric named: {}. The value is null.",
							name);
				}
				throw new RuntimeException(eihte);
			}
		}

		if (m_kuraPayload.getBody() != null) {
			protoMsg.setBody(ByteString.copyFrom(m_kuraPayload.getBody()));
		}

		return protoMsg.build().toByteArray();
	}

	private KuraPayloadProto.KuraPayload.KuraPosition buildPositionProtoBuf() {
		KuraPayloadProto.KuraPayload.KuraPosition.Builder protoPos = null;
		protoPos = KuraPayloadProto.KuraPayload.KuraPosition.newBuilder();

		final KuraPosition position = m_kuraPayload.getPosition();
		if (position.getLatitude() != null) {
			protoPos.setLatitude(position.getLatitude());
		}
		if (position.getLongitude() != null) {
			protoPos.setLongitude(position.getLongitude());
		}
		if (position.getAltitude() != null) {
			protoPos.setAltitude(position.getAltitude());
		}
		if (position.getPrecision() != null) {
			protoPos.setPrecision(position.getPrecision());
		}
		if (position.getHeading() != null) {
			protoPos.setHeading(position.getHeading());
		}
		if (position.getSpeed() != null) {
			protoPos.setSpeed(position.getSpeed());
		}
		if (position.getTimestamp() != null) {
			protoPos.setTimestamp(position.getTimestamp().getTime());
		}
		if (position.getSatellites() != null) {
			protoPos.setSatellites(position.getSatellites());
		}
		if (position.getStatus() != null) {
			protoPos.setStatus(position.getStatus());
		}
		return protoPos.build();
	}

	private static void setProtoKuraMetricValue(
			KuraPayloadProto.KuraPayload.KuraMetric.Builder metric, Object o)
			throws Exception {

		if (o instanceof String) {
			metric.setType(KuraPayloadProto.KuraPayload.KuraMetric.ValueType.STRING);
			metric.setStringValue((String) o);
		} else if (o instanceof Double) {
			metric.setType(KuraPayloadProto.KuraPayload.KuraMetric.ValueType.DOUBLE);
			metric.setDoubleValue((Double) o);
		} else if (o instanceof Integer) {
			metric.setType(KuraPayloadProto.KuraPayload.KuraMetric.ValueType.INT32);
			metric.setIntValue((Integer) o);
		} else if (o instanceof Float) {
			metric.setType(KuraPayloadProto.KuraPayload.KuraMetric.ValueType.FLOAT);
			metric.setFloatValue((Float) o);
		} else if (o instanceof Long) {
			metric.setType(KuraPayloadProto.KuraPayload.KuraMetric.ValueType.INT64);
			metric.setLongValue((Long) o);
		} else if (o instanceof Boolean) {
			metric.setType(KuraPayloadProto.KuraPayload.KuraMetric.ValueType.BOOL);
			metric.setBoolValue((Boolean) o);
		} else if (o instanceof byte[]) {
			metric.setType(KuraPayloadProto.KuraPayload.KuraMetric.ValueType.BYTES);
			metric.setBytesValue(ByteString.copyFrom((byte[]) o));
		} else if (o == null) {
			throw new Exception("null value");
		} else {
			throw new Exception(o.getClass().getName());
		}
	}
}
