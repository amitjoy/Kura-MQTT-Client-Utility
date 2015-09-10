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

import com.amitinside.mqtt.client.kura.log.LogTracker;

public final class LogViewPart {

	private final UISynchronize synchronize;
	private Text textLog;
	private final LogTracker logTracker;

	@Inject
	public LogViewPart(EPartService partService, IEclipseContext context,
			UISynchronize synchronize) {
		this.synchronize = synchronize;
		logTracker = context.get(LogTracker.class);
	}

	@PostConstruct
	public void createContents(final Composite parent) {

		final Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new FillLayout());

		final FormToolkit toolkit = new FormToolkit(parent.getDisplay());

		final Form form = toolkit.createForm(composite);
		applyGridData(form).withFill();

		form.setText("MQTT Client Logs");

		form.getBody().setLayout(new GridLayout(1, false));

		textLog = toolkit.createText(form.getBody(), "", SWT.READ_ONLY
				| SWT.V_SCROLL | SWT.WRAP);
		applyGridData(textLog).withFill();

		form.getToolBarManager().add(new Action("Clear") {
			@Override
			public void run() {
				textLog.setText("");
			}
		});

		form.updateToolBar();
	}

	@Inject
	@Optional
	public void updateForm(@UIEventTopic(LOG_EVENT_TOPIC) Object obj) {
		synchronize.asyncExec(new Runnable() {
			@Override
			public void run() {
				if (textLog != null)
					textLog.append("\n " + logTracker.getLastLog());
			}
		});
	}
}