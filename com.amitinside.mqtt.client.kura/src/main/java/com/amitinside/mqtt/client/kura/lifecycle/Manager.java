package com.amitinside.mqtt.client.kura.lifecycle;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

public class Manager {

	@Inject
	@Preference(nodePath = "com.amitinside.e4.rcp.todo", value = "user")
	private String user;

	@PostContextCreate
	public void postContextCreate(@Preference IEclipsePreferences prefs,
			IApplicationContext appContext, Display display) {

		final Shell shell = new Shell(SWT.TOOL | SWT.NO_TRIM);

		// MessageDialog.openInformation(display.getActiveShell(), "Dummy",
		// "Dummy");
		System.out.println("DELLO");
		// System.out.println(appContext.getArguments()
		// .get(IApplicationContext.APPLICATION_ARGS).getClass()
		// .getCanonicalName());
		// appContext.getArguments().put("-Declipse.consoleLog", "true");

		appContext.applicationRunning();

	}

	private void setLocation(Display display, Shell shell) {
		final Monitor monitor = display.getPrimaryMonitor();
		final Rectangle monitorRect = monitor.getBounds();
		final Rectangle shellRect = shell.getBounds();
		final int x = monitorRect.x + (monitorRect.width - shellRect.width) / 2;
		final int y = monitorRect.y + (monitorRect.height - shellRect.height)
				/ 2;
		shell.setLocation(x, y);
	}
}