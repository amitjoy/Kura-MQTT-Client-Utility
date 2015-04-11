package com.amitinside.mqtt.client.kura.util;

import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.ui.forms.widgets.Form;

import com.amitinside.e4.bundleresourceloader.IBundleResourceLoader;

public final class FormUtil {

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

}
