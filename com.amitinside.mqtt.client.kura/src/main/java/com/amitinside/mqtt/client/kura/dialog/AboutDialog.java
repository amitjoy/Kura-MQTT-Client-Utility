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

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.amitinside.swt.layout.grid.GridDataUtil;

public final class AboutDialog extends TitleAreaDialog {

	public static void openDialogBox(final Shell parent) {
		final AboutDialog dialog = new AboutDialog(parent);
		dialog.create();
		if (dialog.open() == Window.OK) {
		}
	}

	private Text txtAboutInformation;

	private AboutDialog(final Shell parentShell) {
		super(parentShell);
	}

	@Override
	public void create() {
		super.create();
		this.setTitle("About Eclipse Kura MQTT Client Utility");
		this.setMessage("Developed By Amit Kumar Mondal (admin@amitinside.com)", IMessageProvider.INFORMATION);
	}

	private void createAboutInformation(final Composite container) {
		final Label lbtMQTTServerName = new Label(container, SWT.WRAP | SWT.BORDER | SWT.LEFT);

		final String information = "This MQTT Client Application facilitates \n "
				+ "with all the MQTT related operations while using Eclipse \n "
				+ "Kura for IoT Application development";

		lbtMQTTServerName.setText(information);

		GridDataUtil.applyGridData(lbtMQTTServerName).grabExcessVerticalSpace(true).horizontalAlignment(GridData.FILL);
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite area = (Composite) super.createDialogArea(parent);
		final Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		final GridLayout layout = new GridLayout(2, false);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setLayout(layout);
		this.createAboutInformation(container);
		return area;
	}

	@Override
	protected Point getInitialSize() {
		return new Point(400, 200);
	}

	@Override
	protected void okPressed() {
		super.okPressed();
	}

}
