package com.amitinside.mqtt.client.kura.parts;

import java.util.Enumeration;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
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
import com.amitinside.mqtt.client.KuraMQTTClient;
import com.amitinside.mqtt.client.kura.dialog.ConnectionSettingsDialog;
import com.amitinside.mqtt.client.kura.message.KuraPayload;
import com.amitinside.mqtt.client.kura.util.PayloadUtil;
import com.amitinside.swt.layout.grid.GridDataUtil;

public final class PublishPart {

	private final EPartService partService;
	private final MApplication application;
	private Label label;
	private Text textTopic;
	private Text textRequestId;
	private Text textRequesterClientId;
	private Text textPublishMetric;
	private Text textSubscribeTopicHint;
	private MPart part;
	private Form form;
	private final KuraMQTTClient mqttClient;
	private final IEclipseContext context;
	private final IEventBroker broker;
	private final UISynchronize uiSynchronize;
	private final IBundleResourceLoader bundleResourceService;

	@Inject
	public PublishPart(MApplication application, EPartService partService,
			IEclipseContext context, IEventBroker broker,
			UISynchronize uiSynchronize,
			@Optional IBundleResourceLoader bundleResourceService) {
		this.partService = partService;
		this.application = application;
		this.context = context;
		this.broker = broker;
		this.uiSynchronize = uiSynchronize;
		this.mqttClient = context.get(KuraMQTTClient.class);
		this.bundleResourceService = context.get(IBundleResourceLoader.class);
	}

	@PostConstruct
	public void createContents(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new FillLayout());

		final FormToolkit toolkit = new FormToolkit(parent.getDisplay());

		form = toolkit.createForm(composite);
		GridDataUtil.applyGridData(form).withFill();

		form.setText("Publishing with KuraPayload");
		defaultSetImage(form);

		form.getBody().setLayout(new GridLayout(2, false));
		Label label = toolkit.createLabel(form.getBody(), "Topic*", SWT.NULL);
		textTopic = toolkit.createText(form.getBody(), "");
		textTopic.setMessage("TOPIC/NAMESPACE/EXAMPLE");
		GridDataUtil.applyGridData(textTopic).withHorizontalFill();

		label = toolkit.createLabel(form.getBody(), "Request ID* ", SWT.NULL);
		textRequestId = toolkit.createText(form.getBody(), "");
		GridDataUtil.applyGridData(textRequestId).withHorizontalFill();

		label = toolkit.createLabel(form.getBody(), "Requester Client ID* ",
				SWT.NULL);
		textRequesterClientId = toolkit.createText(form.getBody(), "");
		GridDataUtil.applyGridData(textRequesterClientId).withHorizontalFill();

		label = toolkit.createLabel(form.getBody(), "Payload Metrics ",
				SWT.NULL);
		textPublishMetric = toolkit.createText(form.getBody(), "", SWT.WRAP
				| SWT.V_SCROLL);
		GridDataUtil.applyGridData(textPublishMetric).withFill();

		label = toolkit.createLabel(form.getBody(), "Subscribe Topic Hint ",
				SWT.NULL);
		textSubscribeTopicHint = toolkit.createText(form.getBody(), "",
				SWT.READ_ONLY);
		GridDataUtil.applyGridData(textSubscribeTopicHint).withHorizontalFill();

		final Button buttonPublish = toolkit.createButton(form.getBody(),
				"Publish", SWT.PUSH);
		buttonPublish.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!mqttClient.isConnected()) {
					ConnectionSettingsDialog.openDialogBox(parent.getShell(),
							application, mqttClient, broker);
					return;
				}

				if (textTopic == null || "".equals(textTopic.getText())) {
					MessageDialog.openError(parent.getShell(),
							"Error in Publishing",
							"Topic can not be left blank");
					return;
				}

				if (textRequestId == null || "".equals(textRequestId.getText())) {
					MessageDialog.openError(parent.getShell(),
							"Error in Publishing",
							"Request ID can not be left blank");
					return;
				}

				if (textRequesterClientId == null
						|| "".equals(textRequesterClientId.getText())) {
					MessageDialog.openError(parent.getShell(),
							"Error in Publishing",
							"Requester Client ID can not be left blank");
					return;
				}

				final KuraPayload payload = new KuraPayload();
				payload.addMetric("request.id", textRequestId.getText());
				payload.addMetric("requester.client.id",
						textRequesterClientId.getText());

				if (textPublishMetric != null
						&& !"".equals(textPublishMetric.getText())) {
					final Properties properties = PayloadUtil
							.parsePayloadFromString(textPublishMetric.getText());

					final Enumeration enumeration = properties.propertyNames();

					while (enumeration.hasMoreElements()) {
						final String key = (String) enumeration.nextElement();
						payload.addMetric(key, properties.getProperty(key));
					}
				}
				mqttClient.publish(textTopic.getText(), payload);
				createSubscriptionHint();
			}

			private void createSubscriptionHint() {
				textSubscribeTopicHint.setText(PayloadUtil
						.generateHintSubscriptionTopic(textTopic.getText(),
								textRequestId.getText(),
								textRequesterClientId.getText()));
			}

		});

		GridDataUtil.applyGridData(buttonPublish).horizontalSpan(2)
				.horizontalAlignment(GridData.END);

		form.getToolBarManager().add(new Action("Configure Connection") {
			@Override
			public void run() {
				ConnectionSettingsDialog.openDialogBox(parent.getShell(),
						application, mqttClient, broker);
			}
		});

		form.updateToolBar();

	}

	private void defaultSetImage(Form form) {
		if (mqttClient.isConnected())
			form.setImage(bundleResourceService.loadImage(getClass(),
					"icons/online.png"));
		form.setImage(bundleResourceService.loadImage(getClass(),
				"icons/offline.png"));
	}

	@Inject
	@Optional
	public void updateUIWithClientIdAndConnectionStatus(
			@UIEventTopic(ConnectionSettingsDialog.EVENT_TOPIC) final Object message) {
		if (message instanceof String[]) {
			uiSynchronize.asyncExec(new Runnable() {

				@Override
				public void run() {
					textRequesterClientId.setText(((String[]) message)[1]);
					textRequestId.setText(PayloadUtil.generateRequestId());
					form.setImage(bundleResourceService.loadImage(getClass(),
							"icons/online.png"));
				}
			});
		}
	}
}