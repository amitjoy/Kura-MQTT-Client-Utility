package com.amitinside.mqtt.client.kura.dialog;

import static com.amitinside.mqtt.client.kura.events.KuraClientEventConstants.CONNECTED_EVENT_TOPIC;
import static com.amitinside.mqtt.client.kura.events.KuraClientEventConstants.DISCONNECTED_EVENT_TOPIC;
import static com.amitinside.mqtt.client.kura.util.ClientUtil.clientId;
import static com.amitinside.swt.layout.grid.GridDataUtil.applyGridData;
import static org.eclipse.jface.dialogs.IMessageProvider.INFORMATION;
import static org.eclipse.jface.dialogs.MessageDialog.openError;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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

	private static Text txtMqttServerAddress;
	private Text txtClientId;

	private static String mqttServerAddress;
	private static String clientId;

	private ConnectionSettingsDialog(Shell parentShell,
			KuraMQTTClient mqttClient, IEventBroker broker,
			UISynchronize synchronize) {
		super(parentShell);
		this.mqttClient = mqttClient;
		this.broker = broker;
		this.synchronize = synchronize;
	}

	public static void openDialogBox(Shell shell,
			final KuraMQTTClient mqttClient, final IEventBroker broker,
			UISynchronize synchronize) {
		final ConnectionSettingsDialog dialog = new ConnectionSettingsDialog(
				shell, mqttClient, broker, synchronize);
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
					final boolean status = mqttClient.connect(
							mqttServerAddress, clientId);
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

		createMQTTServerAddress(container);
		createClientId(container);

		return area;
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
}
