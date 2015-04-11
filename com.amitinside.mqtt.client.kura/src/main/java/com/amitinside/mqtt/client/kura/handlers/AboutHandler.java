package com.amitinside.mqtt.client.kura.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.widgets.Shell;

import com.amitinside.mqtt.client.kura.dialog.AboutDialog;

public class AboutHandler {
	@Execute
	public void execute(Shell shell) {
		AboutDialog.openDialogBox(shell);
	}
}
