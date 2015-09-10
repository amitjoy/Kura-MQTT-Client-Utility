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
package com.amitinside.mqtt.client.kura.events;

/**
 * Event Constants for MQTT Communication
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public interface KuraClientEventConstants {

	/**
	 * Event fired when the client gets connected
	 */
	public static final String CONNECTED_EVENT_TOPIC = "broker/connected";

	/**
	 * Event fired when the client gets disconnected
	 */
	public static final String DISCONNECTED_EVENT_TOPIC = "broker/disconnected";

	/**
	 * Event fired when file content is retrieved
	 */
	public static final String FILE_CONTENT_RETRIVAL_EVENT_TOPIC = "file/put/contents";

	/**
	 * Event fired when log data is published
	 */
	public static final String LOG_EVENT_TOPIC = "log/publish/data";

}
