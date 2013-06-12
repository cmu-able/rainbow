package incubator.ui;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.io.InputStream;

import javax.swing.ImageIcon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class which is capable of loading icon resources.
 */
public final class IconResourceLoader {
	/**
	 * Logger.
	 */
	private static final Log LOG = LogFactory.getLog(IconResourceLoader.class);

	/**
	 * Utility class: no constructor.
	 */
	private IconResourceLoader() {
		/*
		 * Nothing to do.
		 */
	}

	/**
	 * Loads an icon resource.
	 * 
	 * @param locator class used to find the resource (assumes the resource is
	 * in the same package as the class)
	 * @param iconName the name of the icon resource (the package name will be
	 * prepended)
	 * 
	 * @return the icon or <code>null</code> if loading failed
	 */
	public static ImageIcon loadIcon(Class<?> locator, String iconName) {
		assert iconName != null;

		ImageIcon icn = null;
		byte[] data = null;
		try (InputStream iconIs = locator.getResourceAsStream(iconName)) {
			if (iconIs == null) {
				LOG.error(format("Resource '{0}' not found for class '{1}'.",
						iconName, locator.getName()));
			} else {
				int available = iconIs.available();
				assert available >= 0;
				data = new byte[available];
				int totalRead = 0;
				while (totalRead < available) {
					int read = iconIs.read(data, totalRead,
							available - totalRead);
					if (read == -1) {
						break;
					}
					
					assert totalRead + read <= available;
					totalRead += read;
				}
				
				if (totalRead == available) {
					/*
					 * We've read everything we wanted. Lets make sure there
					 * is no weird strange data in the end.
					 */
					byte extra[] = new byte[1];
					int r = iconIs.read(extra, 0, 1);
					if (r != -1) {
						LOG.error(format("Failed to read resource '{0}' of "
								+ "class '{1}'. After reading the expected "
								+ "{2} bytes another byte was still found.",
								iconName, locator.getName(), available));
						data = null;
					}
				} else {
					LOG.error(format("Failed to read resource '{0}' of "
							+ "class '{1}'. Should have read {2} bytes but "
							+ "{3} were read.", iconName, locator.getName(),
							available, totalRead));
					data = null;
				}
			}
		} catch (IOException e) {
			LOG.error(format("I/O exception during resource loading. "
					+ "Failed to load resource '{0}' of class '{1}' "
					+ "due to I/O exception.", iconName,
					locator.getName()), e);
		}

		if (data != null) {
			icn = new ImageIcon(data);
		}

		return icn;
	}
}
