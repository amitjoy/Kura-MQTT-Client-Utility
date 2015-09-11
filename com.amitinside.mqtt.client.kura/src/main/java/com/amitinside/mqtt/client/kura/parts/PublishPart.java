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
import static com.amitinside.mqtt.client.kura.events.KuraClientEventConstants.FILE_CONTENT_RETRIVAL_EVENT_TOPIC;
import static com.amitinside.mqtt.client.kura.util.FormUtil.OFFLINE_STATUS_IMAGE;
import static com.amitinside.mqtt.client.kura.util.FormUtil.ONLINE_STATUS_IMAGE;
import static com.amitinside.mqtt.client.kura.util.FormUtil.safelySetToolbarImage;
import static com.amitinside.mqtt.client.kura.util.FormUtil.setTootipConnectionStatus;
import static com.amitinside.mqtt.client.kura.util.PayloadUtil.generateHintSubscriptionTopic;
import static com.amitinside.mqtt.client.kura.util.PayloadUtil.generateRequestId;
import static com.amitinside.swt.layout.grid.GridDataUtil.applyGridData;
import static org.eclipse.jface.dialogs.MessageDialog.openError;
import static org.mihalis.opal.utils.SWTGraphicUtil.centerShell;

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
import org.eclipse.e4.ui.services.EMenuService;
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
import com.amitinside.mqtt.client.kura.message.KuraPayload;

public final class PublishPart {

	private static IKuraMQTTClient mqttClient;
	private final IEventBroker broker;
	private final IBundleResourceLoader bundleResourceService;
	private Button buttonPublish;
	private Form form;
	private Label label;
	private final EMenuService menuService;
	private Text textPublishMetric;
	private Text textRequesterClientId;
	private Text textRequestId;
	private Text textSubscribeTopicHint;
	private Text textTopic;
	private final UISynchronize uiSynchronize;
	private final MWindow window;

	@Inject
	public PublishPart(final MApplication application, final IEclipseContext context, final IEventBroker broker,
			final UISynchronize uiSynchronize, @Optional final IBundleResourceLoader bundleResourceService,
			final MWindow window, final EMenuService menuService) {
		this.broker = broker;
		this.uiSynchronize = uiSynchronize;
		this.window = window;
		this.menuService = menuService;
		this.bundleResourceService = context.get(IBundleResourceLoader.class);
	}

	@PostConstruct
	public void createContents(final Composite parent) {

		centerShell(parent.getShell());

		final Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new FillLayout());

		final FormToolkit toolkit = new FormToolkit(parent.getDisplay());

		this.form = toolkit.createForm(composite);
		applyGridData(this.form).withFill();

		this.form.setText("Publishing with EDC Payload");
		this.defaultSetImage(this.form);

		this.form.getBody().setLayout(new GridLayout(2, false));
		this.label = toolkit.createLabel(this.form.getBody(), "Topic*", SWT.NULL);
		this.textTopic = toolkit.createText(this.form.getBody(), "");
		this.textTopic.setMessage("TOPIC/NAMESPACE/EXAMPLE");
		applyGridData(this.textTopic).withHorizontalFill();

		this.label = toolkit.createLabel(this.form.getBody(), "Request ID* ", SWT.NULL);
		this.textRequestId = toolkit.createText(this.form.getBody(), "");
		applyGridData(this.textRequestId).withHorizontalFill();

		this.label = toolkit.createLabel(this.form.getBody(), "Requester Client ID* ", SWT.NULL);
		this.textRequesterClientId = toolkit.createText(this.form.getBody(), "");
		applyGridData(this.textRequesterClientId).withHorizontalFill();

		this.label = toolkit.createLabel(this.form.getBody(), "Request Payload ", SWT.NULL);
		this.textPublishMetric = toolkit.createText(this.form.getBody(), "", SWT.WRAP | SWT.V_SCROLL);
		applyGridData(this.textPublishMetric).withFill();

		this.label = toolkit.createLabel(this.form.getBody(), "Subscribe Topic Hint ", SWT.NULL);
		this.textSubscribeTopicHint = toolkit.createText(this.form.getBody(), "", SWT.READ_ONLY);
		applyGridData(this.textSubscribeTopicHint).withHorizontalFill();

		this.buttonPublish = toolkit.createButton(this.form.getBody(), "Publish", SWT.PUSH);

		this.buttonPublish.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (mqttClient == null) {
					openError(parent.getShell(), "Communication Problem", "Something bad happened to the connection");
					return;
				}

				if (!mqttClient.isConnected()) {
					openDialogBox(parent.getShell(), mqttClient, PublishPart.this.broker,
							PublishPart.this.uiSynchronize, PublishPart.this.window);
					return;
				}

				if ((PublishPart.this.textTopic == null) || "".equals(PublishPart.this.textTopic.getText())) {
					openError(parent.getShell(), "Error in Publishing", "Topic can not be left blank");
					return;
				}

				if ((PublishPart.this.textRequestId == null) || "".equals(PublishPart.this.textRequestId.getText())) {
					openError(parent.getShell(), "Error in Publishing", "Request ID can not be left blank");
					return;
				}

				if ((PublishPart.this.textRequesterClientId == null)
						|| "".equals(PublishPart.this.textRequesterClientId.getText())) {
					openError(parent.getShell(), "Error in Publishing", "Requester Client ID can not be left blank");
					return;
				}

				final KuraPayload payload = new KuraPayload();
				payload.addMetric("request.id", PublishPart.this.textRequestId.getText());
				payload.addMetric("requester.client.id", PublishPart.this.textRequesterClientId.getText());

				if ((PublishPart.this.textPublishMetric != null)
						&& !"".equals(PublishPart.this.textPublishMetric.getText())) {
					final String inputText = PublishPart.this.textPublishMetric.getText();
					try {
						payload.setBody(inputText.getBytes("UTF-8"));
					} catch (final UnsupportedEncodingException e1) {
						e1.printStackTrace();
					}
				}
				mqttClient.publish(PublishPart.this.textTopic.getText(), payload);
				PublishPart.this.createSubscriptionHint(parent);
			}
		});
		this.regsiterMenuOn(this.textPublishMetric);

		applyGridData(this.buttonPublish).horizontalSpan(2).horizontalAlignment(GridData.END);

		safelySetToolbarImage(this.form, this.uiSynchronize, this.bundleResourceService, OFFLINE_STATUS_IMAGE);

	}

	private void createSubscriptionHint(final Composite parent) {
		String hint = "";
		try {
			hint = generateHintSubscriptionTopic(this.textTopic.getText(), this.textRequestId.getText(),
					this.textRequesterClientId.getText());
		} catch (final ArrayIndexOutOfBoundsException e) {
			openError(parent.getShell(), "Error in Topic", "Kura Specific Topic is invalid");
		}
		this.textSubscribeTopicHint.setText(hint);
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

	private void regsiterMenuOn(final Text textPublishMetric) {
		this.menuService.registerContextMenu(textPublishMetric,
				"com.amitinside.mqtt.client.kura.popupmenu.filechooser");
	}

	@Inject
	@Optional
	public void updateUIWithClientIdAndConnectionStatus(@UIEventTopic(CONNECTED_EVENT_TOPIC) final Object message) {
		if (message instanceof Object[]) {
			this.uiSynchronize.asyncExec(new Runnable() {
				@Override
				public void run() {
					PublishPart.this.textRequesterClientId.setText(((Object[]) message)[1].toString());
					PublishPart.this.textRequestId.setText(generateRequestId());
					PublishPart.this.form.setImage(
							PublishPart.this.bundleResourceService.loadImage(this.getClass(), ONLINE_STATUS_IMAGE));
					setTootipConnectionStatus(PublishPart.this.uiSynchronize, PublishPart.this.buttonPublish,
							((Object[]) message)[0].toString(), true);
					mqttClient = (IKuraMQTTClient) ((Object[]) message)[2];
				}
			});
		}
	}

	@Inject
	@Optional
	public void updateUIWithConnectionStatus(@UIEventTopic(DISCONNECTED_EVENT_TOPIC) final Object message) {
		safelySetToolbarImage(this.form, this.uiSynchronize, this.bundleResourceService, OFFLINE_STATUS_IMAGE);
		setTootipConnectionStatus(this.uiSynchronize, this.buttonPublish, null, false);
	}

	@Inject
	@Optional
	public void updateUIWithFileContent(@UIEventTopic(FILE_CONTENT_RETRIVAL_EVENT_TOPIC) final Object message) {
		if (message != null) {
			this.textPublishMetric.setText((String) message);
		}
	}
}