package com.amitinside.mqtt.client.kura.parts;

import static com.amitinside.mqtt.client.kura.dialog.ConnectionSettingsDialog.openDialogBox;
import static com.amitinside.mqtt.client.kura.events.KuraClientEventConstants.CONNECTED_EVENT_TOPIC;
import static com.amitinside.mqtt.client.kura.events.KuraClientEventConstants.DISCONNECTED_EVENT_TOPIC;
import static com.amitinside.mqtt.client.kura.util.FormUtil.OFFLINE_STATUS_IMAGE;
import static com.amitinside.mqtt.client.kura.util.FormUtil.ONLINE_STATUS_IMAGE;
import static com.amitinside.mqtt.client.kura.util.FormUtil.safelySetToolbarImage;
import static com.amitinside.mqtt.client.kura.util.FormUtil.setTootipConnectionStatus;
import static com.amitinside.mqtt.client.kura.util.PayloadUtil.parsePayloadFromProto;
import static com.amitinside.swt.layout.grid.GridDataUtil.applyGridData;
import static org.eclipse.jface.dialogs.MessageDialog.openError;

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
import com.amitinside.mqtt.client.adapter.MessageListener;
import com.amitinside.mqtt.client.kura.message.KuraPayload;

public final class SubscribePart {

	private Label label;
	private Text textTopic;
	private Text textResponseMetrics;
	private Form form;
	private final KuraMQTTClient mqttClient;
	private final UISynchronize uiSynchronize;
	private final IEventBroker broker;
	private final MWindow window;
	private final IBundleResourceLoader bundleResourceService;
	private Button subscribeButton;

	@Inject
	public SubscribePart(MApplication application, IEclipseContext context,
			UISynchronize uiSynchronize, IEventBroker broker,
			@Optional IBundleResourceLoader bundleResourceService,
			MWindow window) {
		this.uiSynchronize = uiSynchronize;
		this.broker = broker;
		this.window = window;
		this.mqttClient = context.get(KuraMQTTClient.class);
		this.bundleResourceService = context.get(IBundleResourceLoader.class);
	}

	@PostConstruct
	public void createContents(final Composite parent) {

		final Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new FillLayout());

		final FormToolkit toolkit = new FormToolkit(parent.getDisplay());

		form = toolkit.createForm(composite);
		applyGridData(form).withFill();

		form.setText("Subscribing for KuraPayload");
		defaultSetImage(form);

		form.getBody().setLayout(new GridLayout(2, false));
		label = toolkit.createLabel(form.getBody(), "Topic* ", SWT.NULL);
		textTopic = toolkit.createText(form.getBody(), "");
		textTopic.setMessage("TOPIC/NAMESPACE/EXAMPLE");
		applyGridData(textTopic).withHorizontalFill();

		label = toolkit.createLabel(form.getBody(), "Response Metrics ",
				SWT.NULL);
		textResponseMetrics = toolkit.createText(form.getBody(), "",
				SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
		applyGridData(textResponseMetrics).withFill();

		subscribeButton = toolkit.createButton(form.getBody(), "Subscribe",
				SWT.PUSH);
		subscribeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!mqttClient.isConnected()) {
					openDialogBox(parent.getShell(), mqttClient, broker,
							uiSynchronize, window);
					return;
				}

				if ((mqttClient.isConnected())
						&& (textTopic == null || "".equals(textTopic.getText()))) {
					openError(parent.getShell(), "Error in Subscribing",
							"Topic can not be left blank");
					return;
				}
				if (mqttClient.isConnected())
					mqttClient.subscribe(textTopic.getText(),
							new MessageListener() {
								@Override
								public void processMessage(KuraPayload payload) {
									super.processMessage(payload);
									updateForm(payload);
								}
							});
			}
		});

		applyGridData(subscribeButton).horizontalSpan(2).horizontalAlignment(
				GridData.END);

		form.getToolBarManager().add(new Action("Connection") {

			@Override
			public void run() {
				openDialogBox(parent.getShell(), mqttClient, broker,
						uiSynchronize, window);
			}
		});

		form.getToolBarManager().add(new Action("Unsubscribe") {
			@Override
			public void run() {
				if (!mqttClient.isConnected()) {
					openDialogBox(parent.getShell(), mqttClient, broker,
							uiSynchronize, window);
					return;
				}

				if ((mqttClient.isConnected())
						&& (textTopic.getText() == null || "".equals(textTopic
								.getText()))) {
					openError(parent.getShell(), "Error while Unsubscribing",
							"Topic can not be left blank");
					return;
				}

				if (mqttClient.isConnected())
					mqttClient.unsubscribe(textTopic.getText());

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

	private void updateForm(final KuraPayload payload) {
		if (textResponseMetrics != null) {
			uiSynchronize.asyncExec(new Runnable() {

				@Override
				public void run() {
					textResponseMetrics.setText(parsePayloadFromProto(payload
							.metrics()));
				}
			});
		}
	}

	@Inject
	@Optional
	public void updateUIWithClientIdAndConnectionStatus(
			@UIEventTopic(CONNECTED_EVENT_TOPIC) final Object message) {
		safelySetToolbarImage(form, uiSynchronize, bundleResourceService,
				ONLINE_STATUS_IMAGE);
		setTootipConnectionStatus(uiSynchronize, subscribeButton,
				((String[]) message)[0], true);
	}

	@Inject
	@Optional
	public void updateUIWithConnectionStatus(
			@UIEventTopic(DISCONNECTED_EVENT_TOPIC) final Object message) {
		safelySetToolbarImage(form, uiSynchronize, bundleResourceService,
				OFFLINE_STATUS_IMAGE);
		setTootipConnectionStatus(uiSynchronize, subscribeButton, null, false);
	}
}