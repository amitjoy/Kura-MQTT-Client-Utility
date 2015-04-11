package com.amitinside.mqtt.client.kura.util;

import java.math.BigInteger;
import java.security.SecureRandom;

public class ClientUtil {

	public static String clientId() {
		final SecureRandom random = new SecureRandom();
		final String str = new BigInteger(130, random).toString(32);
		return "CLIENT_" + str.toUpperCase();
	}

}
