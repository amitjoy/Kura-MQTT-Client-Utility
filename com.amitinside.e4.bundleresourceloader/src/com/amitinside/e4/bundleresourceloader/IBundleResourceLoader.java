package com.amitinside.e4.bundleresourceloader;

import java.io.IOException;

import org.eclipse.swt.graphics.Image;

public interface IBundleResourceLoader {
	public Image loadImage(Class<?> clazz, String path);

	/**
	 * Loads Specific Files from the Path provided
	 * 
	 * @param bundleClazz
	 *            The Bundle Class
	 * @param resourceTypeclazz
	 *            The Resource Type
	 * @param pathToFile
	 *            The location of file
	 * @return
	 * @throws IOException
	 */
	public <T> T loadResource(Class<?> bundleClazz, Class<T> resourceTypeclazz,
			String pathToFile) throws IOException;
}
