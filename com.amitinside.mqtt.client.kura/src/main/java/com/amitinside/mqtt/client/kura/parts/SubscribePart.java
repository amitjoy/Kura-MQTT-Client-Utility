package com.amitinside.mqtt.client.kura.parts;

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
import com.amitinside.mqtt.client.MessageListener;
import com.amitinside.mqtt.client.kura.dialog.ConnectionSettingsDialog;
import com.amitinside.mqtt.client.kura.events.KuraClientEventConstants;
import com.amitinside.mqtt.client.kura.message.KuraPayload;
import com.amitinside.mqtt.client.kura.util.PayloadUtil;
import com.amitinside.swt.layout.grid.GridDataUtil;

public final class SubscribePart {

	private final EPartService partService;
	private final MApplication application;
	private Label label;
	private Text textTopic;
	private Text textResponseMetrics;
	private MPart part;
	private Form form;
	private final KuraMQTTClient mqttClient;
	private final UISynchronize uiSynchronize;
	private final IEventBroker broker;
	private final IBundleResourceLoader bundleResourceService;

	@Inject
	public SubscribePart(EPartService partService, MApplication application,
			IEclipseContext context, UISynchronize uiSynchronize,
			IEventBroker broker,
			@Optional IBundleResourceLoader bundleResourceService) {
		this.partService = partService;
		this.application = application;
		this.uiSynchronize = uiSynchronize;
		this.broker = broker;
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

		form.setText("Subscribing for KuraPayload");
		defaultSetImage(form);

		form.getBody().setLayout(new GridLayout(2, false));
		Label label = toolkit.createLabel(form.getBody(), "Topic* ", SWT.NULL);
		textTopic = toolkit.createText(form.getBody(), "");
		textTopic.setMessage("TOPIC/NAMESPACE/EXAMPLE");
		GridDataUtil.applyGridData(textTopic).withHorizontalFill();

		label = toolkit.createLabel(form.getBody(), "Response Metrics ",
				SWT.NULL);
		textResponseMetrics = toolkit.createText(form.getBody(), "",
				SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
		GridDataUtil.applyGridData(textResponseMetrics).withFill();

		final Button subscribeButton = toolkit.createButton(form.getBody(),
				"Subscribe", SWT.PUSH);
		subscribeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!mqttClient.isConnected()) {
					ConnectionSettingsDialog.openDialogBox(parent.getShell(),
							application, mqttClient, broker, uiSynchronize);
					return;
				}

				if ((mqttClient.isConnected())
						&& (textTopic == null || "".equals(textTopic.getText()))) {
					MessageDialog.openError(parent.getShell(),
							"Error in Subscribing",
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

		GridDataUtil.applyGridData(subscribeButton).horizontalSpan(2)
				.horizontalAlignment(GridData.END);

		form.getToolBarManager().add(new Action("Configure Connection") {

			@Override
			public void run() {
				ConnectionSettingsDialog.openDialogBox(parent.getShell(),
						application, mqttClient, broker, uiSynchronize);
			}
		});

		form.getToolBarManager().add(new Action("Unsubscribe") {
			@Override
			public void run() {
				if (!mqttClient.isConnected()) {
					ConnectionSettingsDialog.openDialogBox(parent.getShell(),
							application, mqttClient, broker, uiSynchronize);
					return;
				}

				if ((mqttClient.isConnected())
						&& (textTopic.getText() == null || "".equals(textTopic
								.getText()))) {
					MessageDialog.openError(parent.getShell(),
							"Error while Unsubscribing",
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
			safelySetToolbarImage("icons/online.png");
		} else
			safelySetToolbarImage("icons/offline.png");
	}

	private void updateForm(final KuraPayload payload) {
		if (textResponseMetrics != null) {
			uiSynchronize.asyncExec(new Runnable() {

				@Override
				public void run() {
					textResponseMetrics.setText(PayloadUtil
							.parsePayloadFromProto(payload.metrics()));
				}
			});
		}
	}

	@Inject
	@Optional
	public void updateUIWithClientIdAndConnectionStatus(
			@UIEventTopic(KuraClientEventConstants.CONNECTED_EVENT_TOPIC) final Object message) {
		safelySetToolbarImage("icons/online.png");
	}

	@Inject
	@Optional
	public void updateUIWithConnectionStatus(
			@UIEventTopic(KuraClientEventConstants.DISCONNECTED_EVENT_TOPIC) final Object message) {
		safelySetToolbarImage("icons/offline.png");
	}

	private void safelySetToolbarImage(final String path) {
		uiSynchronize.asyncExec(new Runnable() {
			@Override
			public void run() {
				form.setImage(bundleResourceService.loadImage(getClass(), path));
			}
		});

	}
}