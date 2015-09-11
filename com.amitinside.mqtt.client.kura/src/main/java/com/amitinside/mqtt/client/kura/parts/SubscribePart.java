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
package com.amitinside.mqtt.client.kura.parts;

import static com.amitinside.mqtt.client.kura.dialog.ConnectionSettingsDialog.openDialogBox;
import static com.amitinside.mqtt.client.kura.events.KuraClientEventConstants.CONNECTED_EVENT_TOPIC;
import static com.amitinside.mqtt.client.kura.events.KuraClientEventConstants.DISCONNECTED_EVENT_TOPIC;
import static com.amitinside.mqtt.client.kura.util.FormUtil.OFFLINE_STATUS_IMAGE;
import static com.amitinside.mqtt.client.kura.util.FormUtil.ONLINE_STATUS_IMAGE;
import static com.amitinside.mqtt.client.kura.util.FormUtil.SETTINGS_IMAGE;
import static com.amitinside.mqtt.client.kura.util.FormUtil.UNSUBSCRIBE_IMAGE;
import static com.amitinside.mqtt.client.kura.util.FormUtil.safelySetToolbarImage;
import static com.amitinside.mqtt.client.kura.util.FormUtil.setTootipConnectionStatus;
import static com.amitinside.mqtt.client.kura.util.PayloadUtil.parsePayloadFromProto;
import static com.amitinside.swt.layout.grid.GridDataUtil.applyGridData;
import static org.eclipse.jface.dialogs.MessageDialog.openError;

import java.io.UnsupportedEncodingException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.amitinside.e4.bundleresourceloader.IBundleResourceLoader;
import com.amitinside.mqtt.client.IKuraMQTTClient;
import com.amitinside.mqtt.client.adapter.MessageListener;
import com.amitinside.mqtt.client.kura.log.LogTracker;
import com.amitinside.mqtt.client.kura.message.KuraPayload;

public final class SubscribePart {

	private static final String ID = "com.amitinside.mqtt.client.kura.part.subscribe";
	private static LogTracker logTracker;
	private static IKuraMQTTClient mqttClient;
	private final IEventBroker broker;
	private final IBundleResourceLoader bundleResourceService;
	private Form form;
	private Label label;
	private final EPartService partService;
	private Button subscribeButton;
	private Text textResponseMetrics;
	private Text textTopic;
	private final UISynchronize uiSynchronize;
	private final MWindow window;

	@Inject
	public SubscribePart(final MApplication application, final IEclipseContext context,
			final UISynchronize uiSynchronize, final IEventBroker broker,
			@Optional final IBundleResourceLoader bundleResourceService, final MWindow window,
			final EPartService partService) {
		this.uiSynchronize = uiSynchronize;
		this.broker = broker;
		this.window = window;
		this.bundleResourceService = context.get(IBundleResourceLoader.class);
		logTracker = context.get(LogTracker.class);
		this.partService = partService;
	}

	@PostConstruct
	public void createContents(final Composite parent) {

		final Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new FillLayout());

		final FormToolkit toolkit = new FormToolkit(parent.getDisplay());

		this.form = toolkit.createForm(composite);
		applyGridData(this.form).withFill();

		this.form.setText("Subscribing for EDC Payload");
		this.defaultSetImage(this.form);

		this.form.getBody().setLayout(new GridLayout(2, false));
		this.label = toolkit.createLabel(this.form.getBody(), "Topic* ", SWT.NULL);
		this.textTopic = toolkit.createText(this.form.getBody(), "");
		this.textTopic.setMessage("TOPIC/NAMESPACE/EXAMPLE");
		applyGridData(this.textTopic).withHorizontalFill();

		this.label = toolkit.createLabel(this.form.getBody(), "Response Payload ", SWT.NULL);
		this.textResponseMetrics = toolkit.createText(this.form.getBody(), "", SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
		applyGridData(this.textResponseMetrics).withFill();

		this.subscribeButton = toolkit.createButton(this.form.getBody(), "Subscribe", SWT.PUSH);
		this.subscribeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (mqttClient == null) {
					openError(parent.getShell(), "Communication Problem", "Something bad happened to the connection");
					return;
				}

				if (!mqttClient.isConnected()) {
					openDialogBox(parent.getShell(), mqttClient, SubscribePart.this.broker,
							SubscribePart.this.uiSynchronize, SubscribePart.this.window);
					return;
				}

				if ((mqttClient.isConnected()) && ((SubscribePart.this.textTopic == null)
						|| "".equals(SubscribePart.this.textTopic.getText()))) {
					openError(parent.getShell(), "Error while Subscribing", "Topic can not be left blank");
					return;
				}
				if (mqttClient.isConnected()) {
					mqttClient.subscribe(SubscribePart.this.textTopic.getText(), new MessageListener() {
						@Override
						public void processMessage(final KuraPayload payload) {
							logTracker.log("Message Received");
							SubscribePart.this.updateForm(payload);
							SubscribePart.this.uiSynchronize.asyncExec(new Runnable() {

								@Override
								public void run() {
									SubscribePart.this.partService.showPart(ID, PartState.VISIBLE);
								}
							});
						}
					});
				}
			}
		});

		applyGridData(this.subscribeButton).horizontalSpan(2).horizontalAlignment(GridData.END);

		this.form.getToolBarManager().add(new Action("Connection") {

			@Override
			public ImageDescriptor getImageDescriptor() {
				return ImageDescriptor.createFromImage(
						SubscribePart.this.bundleResourceService.loadImage(this.getClass(), SETTINGS_IMAGE));
			}

			@Override
			public void run() {
				openDialogBox(parent.getShell(), mqttClient, SubscribePart.this.broker,
						SubscribePart.this.uiSynchronize, SubscribePart.this.window);
			}
		});

		this.form.getToolBarManager().add(new Action("Unsubscribe") {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return ImageDescriptor.createFromImage(
						SubscribePart.this.bundleResourceService.loadImage(this.getClass(), UNSUBSCRIBE_IMAGE));
			}

			@Override
			public void run() {
				try {
					if (!mqttClient.isConnected()) {
						openDialogBox(parent.getShell(), mqttClient, SubscribePart.this.broker,
								SubscribePart.this.uiSynchronize, SubscribePart.this.window);
						return;
					}

					if ((mqttClient.isConnected()) && ((SubscribePart.this.textTopic.getText() == null)
							|| "".equals(SubscribePart.this.textTopic.getText()))) {
						openError(parent.getShell(), "Error while Unsubscribing", "Topic can not be left blank");
						return;
					}

					if (mqttClient.isConnected()) {
						mqttClient.unsubscribe(SubscribePart.this.textTopic.getText());
					}
				} catch (final Exception e) {
					openError(parent.getShell(), "Communication Problem", "Something bad happened to the connection");
				}
			}
		});

		this.form.updateToolBar();
		safelySetToolbarImage(this.form, this.uiSynchronize, this.bundleResourceService, OFFLINE_STATUS_IMAGE);
	}

	private void defaultSetImage(final Form form) {
		if (mqttClient != null) {
			if (mqttClient.isConnected()) {
				safelySetToolbarImage(form, this.uiSynchronize, this.bundleResourceService, ONLINE_STATUS_IMAGE);
			} else {
				safelySetToolbarImage(form, this.uiSynchronize, this.bundleResourceService, OFFLINE_STATUS_IMAGE);
			}
		}
	}

	private void updateForm(final KuraPayload payload) {
		if (this.textResponseMetrics != null) {
			this.uiSynchronize.asyncExec(new Runnable() {

				@Override
				public void run() {
					final StringBuilder responseBuilder = new StringBuilder();
					try {
						responseBuilder.append(parsePayloadFromProto(payload.metrics())).append("\n")
								.append((payload.getBody() != null) ? new String(payload.getBody(), "UTF-8") : "");
					} catch (final UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					SubscribePart.this.textResponseMetrics.setText(responseBuilder.toString());
				}
			});
		}
	}

	@Inject
	@Optional
	public void updateUIWithClientIdAndConnectionStatus(@UIEventTopic(CONNECTED_EVENT_TOPIC) final Object message) {
		safelySetToolbarImage(this.form, this.uiSynchronize, this.bundleResourceService, ONLINE_STATUS_IMAGE);
		setTootipConnectionStatus(this.uiSynchronize, this.subscribeButton, ((Object[]) message)[0].toString(), true);
		mqttClient = (IKuraMQTTClient) ((Object[]) message)[2];
	}

	@Inject
	@Optional
	public void updateUIWithConnectionStatus(@UIEventTopic(DISCONNECTED_EVENT_TOPIC) final Object message) {
		safelySetToolbarImage(this.form, this.uiSynchronize, this.bundleResourceService, OFFLINE_STATUS_IMAGE);
		setTootipConnectionStatus(this.uiSynchronize, this.subscribeButton, null, false);
	}
}