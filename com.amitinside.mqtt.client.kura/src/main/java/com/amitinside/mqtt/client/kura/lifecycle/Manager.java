package com.amitinside.mqtt.client.kura.lifecycle;

import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Manager {

	@PostContextCreate
	public void postContextCreate(IApplicationContext appContext,
			Display display) {

		final Shell shell = new Shell(SWT.TOOL | SWT.NO_TRIM);

		System.out.println(appContext.getArguments()
				.get(IApplicationContext.APPLICATION_ARGS).getClass()
				.getCanonicalName());

		appContext.applicationRunning();
	}
}