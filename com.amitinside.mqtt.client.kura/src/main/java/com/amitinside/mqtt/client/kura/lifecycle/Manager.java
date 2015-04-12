package com.amitinside.mqtt.client.kura.lifecycle;

import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;

@SuppressWarnings("restriction")
public class Manager {

	@PostContextCreate
	public void postContextCreate(IApplicationContext appContext,
			Display display) {

		for (final String str : (String[]) appContext.getArguments().get(
				IApplicationContext.APPLICATION_ARGS)) {
			System.out.println(str);
		}

		appContext.applicationRunning();
	}
}