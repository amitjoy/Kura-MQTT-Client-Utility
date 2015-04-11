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

public class AboutDialog extends TitleAreaDialog {

	private Text txtAboutInformation;

	private AboutDialog(Shell parentShell) {
		super(parentShell);
	}

	public static void openDialogBox(Shell parent) {
		final AboutDialog dialog = new AboutDialog(parent);
		dialog.create();
		if (dialog.open() == Window.OK) {
		}
	}

	@Override
	public void create() {
		super.create();
		setTitle("About Eclipse Kura MQTT Client Utility");
		setMessage("Developed By Amit Kumar Mondal (admin@amitinside.com)",
				IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite area = (Composite) super.createDialogArea(parent);
		final Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		final GridLayout layout = new GridLayout(2, false);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setLayout(layout);
		createAboutInformation(container);
		return area;
	}

	@Override
	protected Point getInitialSize() {
		return new Point(400, 200);
	}

	private void createAboutInformation(Composite container) {
		final Label lbtMQTTServerName = new Label(container, SWT.WRAP
				| SWT.BORDER | SWT.LEFT);

		final String information = "This MQTT Client Application facilitates \n "
				+ "with all the MQTT related operations while using Eclipse \n "
				+ "Kura for IoT Application development";

		lbtMQTTServerName.setText(information);

		GridDataUtil.applyGridData(lbtMQTTServerName)
				.grabExcessVerticalSpace(true)
				.horizontalAlignment(GridData.FILL);
	}

	@Override
	protected void okPressed() {
		super.okPressed();
	}

}
