package com.amitinside.e4.bundleresourceloader.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.amitinside.e4.bundleresourceloader.IBundleResourceLoader;

public class BundleResourceLoaderImpl implements IBundleResourceLoader {

	@Override
	public Image loadImage(Class<?> clazz, String path) {
		final Bundle bundle = FrameworkUtil.getBundle(clazz);
		final URL url = FileLocator.find(bundle, new Path(path), null);
		final ImageDescriptor imageDescr = ImageDescriptor.createFromURL(url);
		return imageDescr.createImage();
	}

	@Override
	public <T> T loadResource(Class<?> bundleClazz, Class<T> resourceTypeclazz,
			String pathToFile) throws IOException {
		final Bundle bundle = FrameworkUtil.getBundle(bundleClazz);
		final InputStream stream = FileLocator.openStream(bundle, new Path(
				pathToFile), false);

		if (resourceTypeclazz.isInstance(InputStream.class))
			return resourceTypeclazz.cast(stream);

		return null;
	}

}
