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
package com.amitinside.mqtt.client.kura.dialog;

import static com.amitinside.mqtt.client.kura.events.KuraClientEventConstants.CONNECTED_EVENT_TOPIC;
import static com.amitinside.mqtt.client.kura.events.KuraClientEventConstants.DISCONNECTED_EVENT_TOPIC;
import static com.amitinside.mqtt.client.kura.util.ClientUtil.clientId;
import static com.amitinside.swt.layout.grid.GridDataUtil.applyGridData;
import static org.eclipse.jface.dialogs.IMessageProvider.INFORMATION;
import static org.eclipse.jface.dialogs.MessageDialog.openError;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.amitinside.mqtt.client.IKuraMQTTClient;
import com.amitinside.mqtt.client.KuraMQTTClient;

public final class ConnectionSettingsDialog extends TitleAreaDialog {

	private static String clientId;

	private static final String DEFAULT_MQTT_PORT = "1883";

	private static String eclipseServer = "";
	private static String m2mEclipse = "";
	private static String mosquittoServer = "";

	private volatile static IKuraMQTTClient mqttClient;
	private static String mqttDashboardServer = "";
	private static String mqttServerAddress;

	private static String mqttServerPassword;
	private static String mqttServerPort;
	private static String mqttServerUsername;

	private static Text txtMqttServerAddress;
	private static Text txtMqttServerPassword;
	private static Text txtMqttServerPort;
	private static Text txtMqttServerUsername;

	public static void openDialogBox(final Shell shell, final IKuraMQTTClient mqttClient, final IEventBroker broker,
			final UISynchronize synchronize, final MWindow window) {
		final ConnectionSettingsDialog dialog = new ConnectionSettingsDialog(shell, broker, synchronize, window);

		retriveAllTheTestServers(window);

		dialog.create();

		if (mqttClient != null) {
			dialog.setMqttServerAddress(mqttClient.getHost());
			dialog.setClientId(mqttClient.getClientId());
			ConnectionSettingsDialog.mqttClient = mqttClient;
		}

		if (dialog.open() == Window.OK) {
			if ("".equals(dialog.getMqttServerAddress()) || (dialog.getMqttServerAddress() == null)) {
				openError(shell, "Error in MQTT Server Address", "MQTT Server Address can't be empty");
				return;
			}

			if ("".equals(dialog.getClientId()) || (dialog.getClientId() == null)) {
				openError(shell, "Error in Client ID", "Client ID can't be empty");
				return;
			}

			if (mqttClient == null) {
				if (!("".equals(mqttServerUsername) || "".equals(mqttServerPassword))) {
					ConnectionSettingsDialog.mqttClient = new KuraMQTTClient.Builder().setHost(mqttServerAddress)
							.setPort(mqttServerPort).setClientId(clientId).setUsername(mqttServerUsername)
							.setPassword(mqttServerPassword).build();
				} else {
					ConnectionSettingsDialog.mqttClient = new KuraMQTTClient.Builder().setHost(mqttServerAddress)
							.setPort(mqttServerPort).setClientId(clientId).build();
				}

			}

			synchronize.asyncExec(new Runnable() {
				@Override
				public void run() {
					boolean status = false;
					try {
						status = ConnectionSettingsDialog.mqttClient.connect();
					} catch (final Exception e) {
						MessageDialog.openError(shell, "Connection Problem",
								"Something bad happened to the connection");
						return;
					}

					if (status) {
						broker.post(CONNECTED_EVENT_TOPIC,
								new Object[] { mqttServerAddress, clientId, ConnectionSettingsDialog.mqttClient });
					} else {
						broker.post(DISCONNECTED_EVENT_TOPIC, new Object[] { mqttServerAddress, clientId });
					}
				}
			});
		}

	}

	private static void retriveAllTheTestServers(final MWindow window) {
		eclipseServer = (String) window.getContext().get("eclipse_broker");
		mosquittoServer = (String) window.getContext().get("mosquitto_broker");
		mqttDashboardServer = (String) window.getContext().get("mqttdashboard");
		m2mEclipse = (String) window.getContext().get("m2m_eclipse");
	}

	private final IEventBroker broker;
	private Combo helpServersCombo;
	private final UISynchronize synchronize;

	private Text txtClientId;

	private final MWindow window;

	private ConnectionSettingsDialog(final Shell parentShell, final IEventBroker broker,
			final UISynchronize synchronize, final MWindow window) {
		super(parentShell);
		this.broker = broker;
		this.synchronize = synchronize;
		this.window = window;
	}

	@Override
	public void create() {
		super.create();
		this.setTitle("Eclipse Kura MQTT Client Connection Settings");
		this.setMessage("Configuration parameters for MQTT Server Connection", INFORMATION);
	}

	private void createClientId(final Composite container) {
		final Label lbtClientId = new Label(container, SWT.NONE);
		lbtClientId.setText("Client ID*");

		this.txtClientId = new Text(container, SWT.BORDER);
		this.txtClientId.setText(clientId());

		if ((clientId != null) && !"".equals(clientId)) {
			this.txtClientId.setText(clientId);
		}

		applyGridData(this.txtClientId).grabExcessHorizontalSpace(true).horizontalAlignment(GridData.FILL);
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite area = (Composite) super.createDialogArea(parent);
		final Composite container = new Composite(area, SWT.NONE);

		applyGridData(container).withFill();
		final GridLayout layout = new GridLayout(2, false);
		applyGridData(container).withFill();
		container.setLayout(layout);

		this.createSomeTestServersDropdown(container);
		this.createMQTTServerAddress(container);
		this.createMQTTServerPort(container);
		this.createMQTTServerUsername(container);
		this.createMQTTServerPassword(container);
		this.createClientId(container);

		return area;
	}

	private void createMQTTServerAddress(final Composite container) {
		final Label lbtMQTTServerName = new Label(container, SWT.NONE);
		lbtMQTTServerName.setText("MQTT Server Address*");

		txtMqttServerAddress = new Text(container, SWT.BORDER);
		txtMqttServerAddress.setMessage("test.example.org");

		if ((mqttServerAddress != null) && !"".equals(mqttServerAddress)) {
			txtMqttServerAddress.setText(mqttServerAddress);
		}

		applyGridData(txtMqttServerAddress).grabExcessHorizontalSpace(true).horizontalAlignment(GridData.FILL);
	}

	private void createMQTTServerPassword(final Composite container) {
		final Label lbtMQTTServerPassword = new Label(container, SWT.NONE);
		lbtMQTTServerPassword.setText("Password");

		txtMqttServerPassword = new Text(container, SWT.BORDER);
		txtMqttServerPassword.setMessage("MQTT Server Password");

		if ((mqttServerPassword != null) && !"".equals(mqttServerPassword)) {
			txtMqttServerPassword.setText(mqttServerPassword);
		}

		applyGridData(txtMqttServerPassword).grabExcessHorizontalSpace(true).horizontalAlignment(GridData.FILL);
	}

	private void createMQTTServerPort(final Composite container) {
		final Label lbtMQTTServerPort = new Label(container, SWT.NONE);
		lbtMQTTServerPort.setText("Port");

		txtMqttServerPort = new Text(container, SWT.BORDER);
		txtMqttServerPort.setMessage("MQTT Server Port");

		if ((mqttServerPort != null) && !"".equals(mqttServerPort)) {
			txtMqttServerPort.setText(mqttServerPort);
		}

		applyGridData(txtMqttServerPort).grabExcessHorizontalSpace(true).horizontalAlignment(GridData.FILL);
	}

	private void createMQTTServerUsername(final Composite container) {
		final Label lbtMQTTServerUsername = new Label(container, SWT.NONE);
		lbtMQTTServerUsername.setText("Username");

		txtMqttServerUsername = new Text(container, SWT.BORDER);
		txtMqttServerUsername.setMessage("MQTT Server Username");

		if ((mqttServerUsername != null) && !"".equals(mqttServerUsername)) {
			txtMqttServerUsername.setText(mqttServerUsername);
		}

		applyGridData(txtMqttServerUsername).grabExcessHorizontalSpace(true).horizontalAlignment(GridData.FILL);
	}

	private void createSomeTestServersDropdown(final Composite container) {
		final Label lbtTestServers = new Label(container, SWT.NONE);
		lbtTestServers.setText("Test Broker");

		this.helpServersCombo = new Combo(container, SWT.READ_ONLY);
		this.helpServersCombo.setBounds(50, 50, 150, 65);
		final String items[] = { "-- Select Test Broker --", eclipseServer, mosquittoServer, mqttDashboardServer,
				m2mEclipse };
		this.helpServersCombo.setItems(items);
		this.helpServersCombo.select(0);
		this.helpServersCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				switch (ConnectionSettingsDialog.this.helpServersCombo.getSelectionIndex()) {
				case 1:
					txtMqttServerAddress.setText(eclipseServer);
					break;
				case 2:
					txtMqttServerAddress.setText(mosquittoServer);
					break;
				case 3:
					txtMqttServerAddress.setText(mqttDashboardServer);
					break;
				case 4:
					txtMqttServerAddress.setText(m2mEclipse);
					break;
				default:
					break;
				}
				txtMqttServerPort.setText(DEFAULT_MQTT_PORT);
			}
		});

		applyGridData(this.helpServersCombo).grabExcessHorizontalSpace(true).horizontalAlignment(GridData.FILL);
	}

	public String getClientId() {
		return clientId;
	}

	public String getMqttServerAddress() {
		return mqttServerAddress;
	}

	/**
	 * @return the mqttServerPassword
	 */
	public String getMqttServerPassword() {
		return mqttServerPassword;
	}

	public String getMqttServerPort() {
		return mqttServerPort;
	}

	/**
	 * @return the mqttServerUsername
	 */
	public String getMqttServerUsername() {
		return mqttServerUsername;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void okPressed() {
		this.saveInput();
		super.okPressed();
	}

	private void saveInput() {
		mqttServerAddress = txtMqttServerAddress.getText();
		clientId = this.txtClientId.getText();
		mqttServerPassword = txtMqttServerPassword.getText();
		mqttServerUsername = txtMqttServerUsername.getText();
		mqttServerPort = txtMqttServerPort.getText();
	}

	public void setClientId(final String clientId) {
		this.clientId = clientId;
	}

	public void setMqttServerAddress(final String mqttServerAddress) {
		this.mqttServerAddress = mqttServerAddress;
	}

	/**
	 * @param mqttServerPassword
	 *            the mqttServerPassword to set
	 */
	public void setMqttServerPassword(final String mqttServerPassword) {
		ConnectionSettingsDialog.mqttServerPassword = mqttServerPassword;
	}

	public void setMqttServerPort(final String mqttServerPort) {
		ConnectionSettingsDialog.mqttServerPort = mqttServerPort;
	}

	/**
	 * @param mqttServerUsername
	 *            the mqttServerUsername to set
	 */
	public void setMqttServerUsername(final String mqttServerUsername) {
		ConnectionSettingsDialog.mqttServerUsername = mqttServerUsername;
	}

}
