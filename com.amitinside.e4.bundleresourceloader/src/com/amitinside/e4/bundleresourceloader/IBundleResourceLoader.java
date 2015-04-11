package com.amitinside.e4.bundleresourceloader;

import java.io.IOException;

import org.eclipse.swt.graphics.Image;

public interface IBundleResourceLoader {
	public Image loadImage(Class<?> clazz, String path);

	public <T> T loadResource(Class<?> bundleClazz, Class<T> resourceTypeclazz,
			String pathToFile) throws IOException;
}
