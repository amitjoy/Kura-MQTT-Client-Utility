package com.amitinside.mqtt.client.kura.handlers;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.amitinside.mqtt.client.KuraMQTTClient;

public class QuitHandler {
	@Execute
	public void execute(IWorkbench workbench, Shell shell,
			MApplication application, IEclipseContext context) {
		if (MessageDialog.openConfirm(shell, "Confirmation",
				"Do you want to exit?")) {
			final KuraMQTTClient client = context.get(KuraMQTTClient.class);
			client.disconnect();
			workbench.close();
		}
	}
}
