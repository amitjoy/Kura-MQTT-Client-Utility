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

import com.amitinside.mqtt.client.KuraMQTTClient;

public class ConnectionSettingsDialog extends TitleAreaDialog {

	private volatile static KuraMQTTClient mqttClient;

	private final IEventBroker broker;
	private final UISynchronize synchronize;
	private final MWindow window;

	private static Text txtMqttServerAddress;
	private Text txtClientId;
	private Combo helpServersCombo;

	private static Text txtMqttServerUsername;
	private static Text txtMqttServerPassword;
	private static Text txtMqttServerPort;

	private static String mqttServerAddress;
	private static String mqttServerUsername;
	private static String mqttServerPassword;
	private static String mqttServerPort;
	private static String clientId;

	private static String eclipseServer = "";
	private static String mosquittoServer = "";
	private static String mqttDashboardServer = "";
	private static String m2mEclipse = "";

	private ConnectionSettingsDialog(Shell parentShell,
			KuraMQTTClient mqttClient, IEventBroker broker,
			UISynchronize synchronize, MWindow window) {
		super(parentShell);
		this.mqttClient = mqttClient;
		this.broker = broker;
		this.synchronize = synchronize;
		this.window = window;
	}

	public static void openDialogBox(Shell shell,
			final KuraMQTTClient mqttClient, final IEventBroker broker,
			UISynchronize synchronize, MWindow window) {
		final ConnectionSettingsDialog dialog = new ConnectionSettingsDialog(
				shell, mqttClient, broker, synchronize, window);

		retriveAllTheTestServers(window);

		dialog.create();

		dialog.setMqttServerAddress(mqttClient.getHost());
		dialog.setClientId(mqttClient.getClientId());

		if (dialog.open() == Window.OK) {
			if ("".equals(dialog.getMqttServerAddress())
					|| dialog.getMqttServerAddress() == null) {
				openError(shell, "Error in MQTT Server Address",
						"MQTT Server Address can't be empty");
				return;
			}

			if ("".equals(dialog.getClientId()) || dialog.getClientId() == null) {
				openError(shell, "Error in Client ID",
						"Client ID can't be empty");
				return;
			}

			synchronize.asyncExec(new Runnable() {
				@Override
				public void run() {
					boolean status = false;
					status = mqttClient.connect(mqttServerAddress,
							mqttServerPort, clientId, mqttServerUsername,
							mqttServerPassword);

					if (status)
						broker.post(CONNECTED_EVENT_TOPIC, new String[] {
								mqttServerAddress, clientId });
					else
						broker.post(DISCONNECTED_EVENT_TOPIC, new String[] {
								mqttServerAddress, clientId });
				}
			});
		}

	}

	private static void retriveAllTheTestServers(MWindow window) {
		eclipseServer = (String) window.getContext().get("eclipse_broker");
		mosquittoServer = (String) window.getContext().get("mosquitto_broker");
		mqttDashboardServer = (String) window.getContext().get("mqttdashboard");
		m2mEclipse = (String) window.getContext().get("m2m_eclipse");
	}

	@Override
	public void create() {
		super.create();
		setTitle("Eclipse Kura MQTT Client Connection Settings");
		setMessage("Configuration parameters for MQTT Server Connection",
				INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite area = (Composite) super.createDialogArea(parent);
		final Composite container = new Composite(area, SWT.NONE);

		applyGridData(container).withFill();
		final GridLayout layout = new GridLayout(2, false);
		applyGridData(container).withFill();
		container.setLayout(layout);

		createSomeTestServersDropdown(container);
		createMQTTServerAddress(container);
		createMQTTServerPort(container);
		createMQTTServerUsername(container);
		createMQTTServerPassword(container);
		createClientId(container);

		return area;
	}

	private void createSomeTestServersDropdown(Composite container) {
		final Label lbtTestServers = new Label(container, SWT.NONE);
		lbtTestServers.setText("Test Broker");

		helpServersCombo = new Combo(container, SWT.READ_ONLY);
		helpServersCombo.setBounds(50, 50, 150, 65);
		final String items[] = { "-- Select Test Broker --", eclipseServer,
				mosquittoServer, mqttDashboardServer, m2mEclipse };
		helpServersCombo.setItems(items);
		helpServersCombo.select(0);
		helpServersCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				switch (helpServersCombo.getSelectionIndex()) {
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
				txtMqttServerPort.setText("1883");
			}
		});

		applyGridData(helpServersCombo).grabExcessHorizontalSpace(true)
				.horizontalAlignment(GridData.FILL);
	}

	private void createMQTTServerAddress(Composite container) {
		final Label lbtMQTTServerName = new Label(container, SWT.NONE);
		lbtMQTTServerName.setText("MQTT Server Address*");

		txtMqttServerAddress = new Text(container, SWT.BORDER);
		txtMqttServerAddress.setMessage("test.example.org");

		if (mqttServerAddress != null && !"".equals(mqttServerAddress))
			txtMqttServerAddress.setText(mqttServerAddress);

		applyGridData(txtMqttServerAddress).grabExcessHorizontalSpace(true)
				.horizontalAlignment(GridData.FILL);
	}

	private void createMQTTServerUsername(Composite container) {
		final Label lbtMQTTServerUsername = new Label(container, SWT.NONE);
		lbtMQTTServerUsername.setText("Username");

		txtMqttServerUsername = new Text(container, SWT.BORDER);
		txtMqttServerUsername.setMessage("MQTT Server Username");

		if (mqttServerUsername != null && !"".equals(mqttServerUsername))
			txtMqttServerUsername.setText(mqttServerUsername);

		applyGridData(txtMqttServerUsername).grabExcessHorizontalSpace(true)
				.horizontalAlignment(GridData.FILL);
	}

	private void createMQTTServerPassword(Composite container) {
		final Label lbtMQTTServerPassword = new Label(container, SWT.NONE);
		lbtMQTTServerPassword.setText("Password");

		txtMqttServerPassword = new Text(container, SWT.BORDER);
		txtMqttServerPassword.setMessage("MQTT Server Password");

		if (mqttServerPassword != null && !"".equals(mqttServerPassword))
			txtMqttServerPassword.setText(mqttServerPassword);

		applyGridData(txtMqttServerPassword).grabExcessHorizontalSpace(true)
				.horizontalAlignment(GridData.FILL);
	}

	private void createMQTTServerPort(Composite container) {
		final Label lbtMQTTServerPort = new Label(container, SWT.NONE);
		lbtMQTTServerPort.setText("Port");

		txtMqttServerPort = new Text(container, SWT.BORDER);
		txtMqttServerPort.setMessage("MQTT Server Port");

		if (mqttServerPort != null && !"".equals(mqttServerPort))
			txtMqttServerPort.setText(mqttServerPort);

		applyGridData(txtMqttServerPort).grabExcessHorizontalSpace(true)
				.horizontalAlignment(GridData.FILL);
	}

	private void createClientId(Composite container) {
		final Label lbtClientId = new Label(container, SWT.NONE);
		lbtClientId.setText("Client ID*");

		txtClientId = new Text(container, SWT.BORDER);
		txtClientId.setText(clientId());

		if (clientId != null && !"".equals(clientId))
			txtClientId.setText(clientId);

		applyGridData(txtClientId).grabExcessHorizontalSpace(true)
				.horizontalAlignment(GridData.FILL);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	private void saveInput() {
		mqttServerAddress = txtMqttServerAddress.getText();
		clientId = txtClientId.getText();
		mqttServerPassword = txtMqttServerPassword.getText();
		mqttServerUsername = txtMqttServerUsername.getText();
		mqttServerPort = txtMqttServerPort.getText();
	}

	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}

	public String getMqttServerAddress() {
		return mqttServerAddress;
	}

	public String getClientId() {
		return clientId;
	}

	public void setMqttServerAddress(String mqttServerAddress) {
		this.mqttServerAddress = mqttServerAddress;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * @return the mqttServerUsername
	 */
	public String getMqttServerUsername() {
		return mqttServerUsername;
	}

	/**
	 * @param mqttServerUsername
	 *            the mqttServerUsername to set
	 */
	public void setMqttServerUsername(String mqttServerUsername) {
		ConnectionSettingsDialog.mqttServerUsername = mqttServerUsername;
	}

	/**
	 * @return the mqttServerPassword
	 */
	public String getMqttServerPassword() {
		return mqttServerPassword;
	}

	/**
	 * @param mqttServerPassword
	 *            the mqttServerPassword to set
	 */
	public void setMqttServerPassword(String mqttServerPassword) {
		ConnectionSettingsDialog.mqttServerPassword = mqttServerPassword;
	}

	public String getMqttServerPort() {
		return mqttServerPort;
	}

	public void setMqttServerPort(String mqttServerPort) {
		ConnectionSettingsDialog.mqttServerPort = mqttServerPort;
	}

}
