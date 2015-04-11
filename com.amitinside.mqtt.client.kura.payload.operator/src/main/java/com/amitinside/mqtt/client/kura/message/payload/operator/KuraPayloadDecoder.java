package com.amitinside.mqtt.client.kura.message.payload.operator;

import java.io.IOException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amitinside.mqtt.client.kura.message.KuraPayload;
import com.amitinside.mqtt.client.kura.message.KuraPosition;
import com.amitinside.mqtt.client.kura.message.payload.util.GZipUtil;
import com.amitinside.mqtt.client.kura.message.protobuf.KuraPayloadProto;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

public class KuraPayloadDecoder {
	private static final Logger s_logger = LoggerFactory
			.getLogger(KuraPayloadDecoder.class);

	private byte[] m_bytes;

	public KuraPayloadDecoder(byte[] bytes) {
		m_bytes = bytes;
	}

	public KuraPayload buildFromByteArray() throws IOException {
		if (GZipUtil.isCompressed(m_bytes)) {
			try {
				m_bytes = GZipUtil.decompress(m_bytes);
			} catch (final IOException e) {
				s_logger.info("Decompression failed");
			}
		}

		KuraPayloadProto.KuraPayload protoMsg = null;
		try {
			protoMsg = KuraPayloadProto.KuraPayload.parseFrom(m_bytes);
		} catch (final InvalidProtocolBufferException ipbe) {
			throw new RuntimeException(ipbe);
		}

		final KuraPayload kuraMsg = new KuraPayload();

		if (protoMsg.hasTimestamp()) {
			kuraMsg.setTimestamp(new Date(protoMsg.getTimestamp()));
		}

		if (protoMsg.hasPosition()) {
			kuraMsg.setPosition(buildFromProtoBuf(protoMsg.getPosition()));
		}

		for (int i = 0; i < protoMsg.getMetricCount(); i++) {
			final String name = protoMsg.getMetric(i).getName();
			try {
				final Object value = getProtoKuraMetricValue(
						protoMsg.getMetric(i), protoMsg.getMetric(i).getType());
				kuraMsg.addMetric(name, value);
			} catch (final Exception ihte) {
				s_logger.warn("During deserialization, ignoring metric named: "
						+ name + ". Unrecognized value type: "
						+ protoMsg.getMetric(i).getType(), ihte);
			}
		}

		if (protoMsg.hasBody()) {
			kuraMsg.setBody(protoMsg.getBody().toByteArray());
		}

		return kuraMsg;
	}

	private KuraPosition buildFromProtoBuf(
			KuraPayloadProto.KuraPayload.KuraPosition protoPosition) {
		final KuraPosition position = new KuraPosition();

		if (protoPosition.hasLatitude()) {
			position.setLatitude(protoPosition.getLatitude());
		}
		if (protoPosition.hasLongitude()) {
			position.setLongitude(protoPosition.getLongitude());
		}
		if (protoPosition.hasAltitude()) {
			position.setAltitude(protoPosition.getAltitude());
		}
		if (protoPosition.hasPrecision()) {
			position.setPrecision(protoPosition.getPrecision());
		}
		if (protoPosition.hasHeading()) {
			position.setHeading(protoPosition.getHeading());
		}
		if (protoPosition.hasSpeed()) {
			position.setSpeed(protoPosition.getSpeed());
		}
		if (protoPosition.hasSatellites()) {
			position.setSatellites(protoPosition.getSatellites());
		}
		if (protoPosition.hasStatus()) {
			position.setStatus(protoPosition.getStatus());
		}
		if (protoPosition.hasTimestamp()) {
			position.setTimestamp(new Date(protoPosition.getTimestamp()));
		}
		return position;
	}

	private Object getProtoKuraMetricValue(
			KuraPayloadProto.KuraPayload.KuraMetric metric,
			KuraPayloadProto.KuraPayload.KuraMetric.ValueType type)
			throws Exception {
		switch (type) {

		case DOUBLE:
			return metric.getDoubleValue();

		case FLOAT:
			return metric.getFloatValue();

		case INT64:
			return metric.getLongValue();

		case INT32:
			return metric.getIntValue();

		case BOOL:
			return metric.getBoolValue();

		case STRING:
			return metric.getStringValue();

		case BYTES:
			final ByteString bs = metric.getBytesValue();
			return bs.toByteArray();

		default:
			throw new Exception(type.name());
		}
	}
}
