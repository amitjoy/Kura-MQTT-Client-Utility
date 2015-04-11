package com.amitinside.mqtt.client.kura.log;

public interface LogTracker {

	public void log(String message);

	public String getLastLog();

}
