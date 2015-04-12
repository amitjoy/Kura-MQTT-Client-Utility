package com.amitinside.mqtt.client.kura.parts;

import static com.amitinside.mqtt.client.kura.dialog.ConnectionSettingsDialog.openDialogBox;
import static com.amitinside.mqtt.client.kura.events.KuraClientEventConstants.CONNECTED_EVENT_TOPIC;
import static com.amitinside.mqtt.client.kura.events.KuraClientEventConstants.DISCONNECTED_EVENT_TOPIC;
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
import org.eclipse.jface.action.Action;
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
import com.amitinside.mqtt.client.kura.message.KuraPayload;

public final class PublishPart {

	private Label label;
	private Text textTopic;
	private Text textRequestId;
	private Text textRequesterClientId;
	private Text textPublishMetric;
	private Text textSubscribeTopicHint;
	private Form form;
	private final KuraMQTTClient mqttClient;
	private final IEventBroker broker;
	private final UISynchronize uiSynchronize;
	private final IBundleResourceLoader bundleResourceService;
	private final MWindow window;
	private Button buttonPublish;

	@Inject
	public PublishPart(MApplication application, IEclipseContext context,
			IEventBroker broker, UISynchronize uiSynchronize,
			@Optional IBundleResourceLoader bundleResourceService,
			MWindow window) {
		this.broker = broker;
		this.uiSynchronize = uiSynchronize;
		this.window = window;
		this.mqttClient = context.get(KuraMQTTClient.class);
		this.bundleResourceService = context.get(IBundleResourceLoader.class);
	}

	@PostConstruct
	public void createContents(final Composite parent) {

		centerShell(parent.getShell());

		final Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new FillLayout());

		final FormToolkit toolkit = new FormToolkit(parent.getDisplay());

		form = toolkit.createForm(composite);
		applyGridData(form).withFill();

		form.setText("Publishing with KuraPayload");
		defaultSetImage(form);

		form.getBody().setLayout(new GridLayout(2, false));
		label = toolkit.createLabel(form.getBody(), "Topic*", SWT.NULL);
		textTopic = toolkit.createText(form.getBody(), "");
		textTopic.setMessage("TOPIC/NAMESPACE/EXAMPLE");
		applyGridData(textTopic).withHorizontalFill();

		label = toolkit.createLabel(form.getBody(), "Request ID* ", SWT.NULL);
		textRequestId = toolkit.createText(form.getBody(), "");
		applyGridData(textRequestId).withHorizontalFill();

		label = toolkit.createLabel(form.getBody(), "Requester Client ID* ",
				SWT.NULL);
		textRequesterClientId = toolkit.createText(form.getBody(), "");
		applyGridData(textRequesterClientId).withHorizontalFill();

		label = toolkit.createLabel(form.getBody(), "Request Payload ",
				SWT.NULL);
		textPublishMetric = toolkit.createText(form.getBody(), "", SWT.WRAP
				| SWT.V_SCROLL);
		applyGridData(textPublishMetric).withFill();

		label = toolkit.createLabel(form.getBody(), "Subscribe Topic Hint ",
				SWT.NULL);
		textSubscribeTopicHint = toolkit.createText(form.getBody(), "",
				SWT.READ_ONLY);
		applyGridData(textSubscribeTopicHint).withHorizontalFill();

		buttonPublish = toolkit.createButton(form.getBody(), "Publish",
				SWT.PUSH);

		buttonPublish.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!mqttClient.isConnected()) {
					openDialogBox(parent.getShell(), mqttClient, broker,
							uiSynchronize, window);
					return;
				}

				if (textTopic == null || "".equals(textTopic.getText())) {
					openError(parent.getShell(), "Error in Publishing",
							"Topic can not be left blank");
					return;
				}

				if (textRequestId == null || "".equals(textRequestId.getText())) {
					openError(parent.getShell(), "Error in Publishing",
							"Request ID can not be left blank");
					return;
				}

				if (textRequesterClientId == null
						|| "".equals(textRequesterClientId.getText())) {
					openError(parent.getShell(), "Error in Publishing",
							"Requester Client ID can not be left blank");
					return;
				}

				final KuraPayload payload = new KuraPayload();
				payload.addMetric("request.id", textRequestId.getText());
				payload.addMetric("requester.client.id",
						textRequesterClientId.getText());

				if (textPublishMetric != null
						&& !"".equals(textPublishMetric.getText())) {
					final String inputText = textPublishMetric.getText();
					try {
						payload.setBody(inputText.getBytes("UTF-8"));
					} catch (final UnsupportedEncodingException e1) {
						e1.printStackTrace();
					}
				}
				mqttClient.publish(textTopic.getText(), payload);
				createSubscriptionHint();
			}

			private void createSubscriptionHint() {
				String hint = "";
				try {
					hint = generateHintSubscriptionTopic(textTopic.getText(),
							textRequestId.getText(),
							textRequesterClientId.getText());
				} catch (final ArrayIndexOutOfBoundsException e) {
					openError(parent.getShell(), "Error in Topic",
							"Kura Specific Topic is invalid");
				}
				textSubscribeTopicHint.setText(hint);
			}

		});

		applyGridData(buttonPublish).horizontalSpan(2).horizontalAlignment(
				GridData.END);

		form.getToolBarManager().add(new Action("Connection") {
			@Override
			public void run() {
				openDialogBox(parent.getShell(), mqttClient, broker,
						uiSynchronize, window);
			}
		});

		form.updateToolBar();

	}

	private void defaultSetImage(Form form) {
		if (mqttClient.isConnected()) {
			safelySetToolbarImage(form, uiSynchronize, bundleResourceService,
					ONLINE_STATUS_IMAGE);
		} else {
			safelySetToolbarImage(form, uiSynchronize, bundleResourceService,
					OFFLINE_STATUS_IMAGE);
		}
	}

	@Inject
	@Optional
	public void updateUIWithClientIdAndConnectionStatus(
			@UIEventTopic(CONNECTED_EVENT_TOPIC) final Object message) {
		if (message instanceof String[]) {
			uiSynchronize.asyncExec(new Runnable() {
				@Override
				public void run() {
					textRequesterClientId.setText(((String[]) message)[1]);
					textRequestId.setText(generateRequestId());
					form.setImage(bundleResourceService.loadImage(getClass(),
							ONLINE_STATUS_IMAGE));
					setTootipConnectionStatus(uiSynchronize, buttonPublish,
							((String[]) message)[0], true);
				}
			});
		}
	}

	@Inject
	@Optional
	public void updateUIWithConnectionStatus(
			@UIEventTopic(DISCONNECTED_EVENT_TOPIC) final Object message) {
		safelySetToolbarImage(form, uiSynchronize, bundleResourceService,
				OFFLINE_STATUS_IMAGE);
		setTootipConnectionStatus(uiSynchronize, buttonPublish, null, false);
	}
}