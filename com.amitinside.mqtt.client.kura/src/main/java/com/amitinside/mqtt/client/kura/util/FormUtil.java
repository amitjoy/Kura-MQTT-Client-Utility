package com.amitinside.mqtt.client.kura.util;

import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.Form;

import com.amitinside.e4.bundleresourceloader.IBundleResourceLoader;

public final class FormUtil {

	public static final String ONLINE_STATUS_IMAGE = "icons/online.png";
	public static final String OFFLINE_STATUS_IMAGE = "icons/offline.png";

	public static void safelySetToolbarImage(final Form form,
			UISynchronize uiSynchronize,
			final IBundleResourceLoader bundleResourceService, final String path) {
		uiSynchronize.asyncExec(new Runnable() {
			@Override
			public void run() {
				form.setImage(bundleResourceService.loadImage(getClass(), path));
			}
		});

	}

	public static void setTootipConnectionStatus(UISynchronize uiSynchronize,
			Control control, final String host, final boolean connected) {
		final DefaultToolTip toolTip = new DefaultToolTip(control);
		toolTip.setShift(new Point(5, 5));
		uiSynchronize.asyncExec(new Runnable() {

			@Override
			public void run() {
				if (connected)
					toolTip.setText("Connected to " + host);
				else
					toolTip.setText("Disconnected");
			}
		});
	}

}
