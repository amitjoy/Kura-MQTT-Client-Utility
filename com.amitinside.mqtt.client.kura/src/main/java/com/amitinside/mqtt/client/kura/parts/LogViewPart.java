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

import static com.amitinside.mqtt.client.kura.events.KuraClientEventConstants.LOG_EVENT_TOPIC;
import static com.amitinside.mqtt.client.kura.util.FormUtil.LOG_IMAGE;
import static com.amitinside.mqtt.client.kura.util.FormUtil.safelySetToolbarImage;
import static com.amitinside.swt.layout.grid.GridDataUtil.applyGridData;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.amitinside.e4.bundleresourceloader.IBundleResourceLoader;
import com.amitinside.mqtt.client.kura.log.LogTracker;

public final class LogViewPart {

	private final IBundleResourceLoader bundleResourceLoader;
	private Form form;
	private final LogTracker logTracker;
	private final UISynchronize synchronize;
	private Text textLog;

	@Inject
	public LogViewPart(final EPartService partService, final IEclipseContext context, final UISynchronize synchronize,
			@Optional final IBundleResourceLoader bundleResourceService) {
		this.synchronize = synchronize;
		this.logTracker = context.get(LogTracker.class);
		this.bundleResourceLoader = bundleResourceService;
	}

	@PostConstruct
	public void createContents(final Composite parent) {

		final Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new FillLayout());

		final FormToolkit toolkit = new FormToolkit(parent.getDisplay());

		this.form = toolkit.createForm(composite);
		applyGridData(this.form).withFill();

		this.form.setText("MQTT Client Logs");

		this.form.getBody().setLayout(new GridLayout(1, false));

		this.textLog = toolkit.createText(this.form.getBody(), "", SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		applyGridData(this.textLog).withFill();

		this.form.getToolBarManager().add(new Action("Clear") {
			@Override
			public void run() {
				LogViewPart.this.textLog.setText("");
			}
		});

		this.form.updateToolBar();
		safelySetToolbarImage(this.form, this.synchronize, this.bundleResourceLoader, LOG_IMAGE);
	}

	@Inject
	@Optional
	public void updateForm(@UIEventTopic(LOG_EVENT_TOPIC) final Object obj) {
		this.synchronize.asyncExec(new Runnable() {
			@Override
			public void run() {
				if (LogViewPart.this.textLog != null) {
					LogViewPart.this.textLog.append("\n " + LogViewPart.this.logTracker.getLastLog());
				}
			}
		});
	}
}