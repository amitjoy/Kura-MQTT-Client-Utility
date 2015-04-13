package com.amitinside.mqtt.client.kura.handlers;

import static com.amitinside.mqtt.client.kura.events.KuraClientEventConstants.FILE_CONTENT_RETRIVAL_EVENT_TOPIC;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class SelectFileHandler {
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell,
			IEventBroker broker) {
		final FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		dialog.setText("Select File to retrieve contents for Request Payload");
		dialog.setFilterExtensions(new String[] { "*.txt", "*.xml" });
		final String result = dialog.open();
		if (result != null) {
			String s;
			try {
				final FileReader fr = new FileReader(new File(result));
				final BufferedReader br = new BufferedReader(fr);
				final StringBuilder builder = new StringBuilder();
				while ((s = br.readLine()) != null) {
					builder.append(s);
				}
				broker.send(FILE_CONTENT_RETRIVAL_EVENT_TOPIC,
						builder.toString());
				fr.close();
			} catch (final FileNotFoundException e) {
				e.printStackTrace();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}
}