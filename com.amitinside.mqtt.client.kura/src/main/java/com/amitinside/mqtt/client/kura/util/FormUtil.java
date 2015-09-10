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
