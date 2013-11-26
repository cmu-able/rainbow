package incubator;

import incubator.pval.Ensure;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Class providing utility methods to deal with java manifest files.
 */
public class ManifestUtil {
	/**
	 * Finds the manifest in the classpath that contains the given property
	 * with the given value.
	 * 
	 * @param property the property to search for
	 * @param value the property value used to select the manifest
	 * @param baseClass the class whose class loader will be used to search
	 * for the manifest
	 * @return the manifest or <code>null</code> if none is found
	 * 
	 * @throws IOException failed to read manifest file
	 */
	public static Manifest findManifest(String property, String value,
			Class<?> baseClass) throws IOException {
		if (property == null) {
			throw new IllegalArgumentException("property == null");
		}
		
		if (value == null) {
			throw new IllegalArgumentException("value == null");
		}
		
		if (baseClass == null) {
			throw new IllegalArgumentException("baseClass == null"); 
		}
		
		/*
		 * Iterate over all manifests.
		 */
		ClassLoader cldr = baseClass.getClassLoader();
		Enumeration<URL> resources = null;
		resources = cldr.getResources("META-INF/MANIFEST.MF");
		
		while (resources.hasMoreElements()) {
			URL url = resources.nextElement();
			try (InputStream is = url.openStream()) {
				Manifest mf = new Manifest(is);
				if (value.equals(getManifestMainProperty(mf, property))) {
					return mf;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Obtains the value of a main property in a manifest.
	 * @param mf the manifest
	 * @param property the property
	 * @return the value or <code>null</code> if the property is not defined
	 */
	public static String getManifestMainProperty(Manifest mf,
			String property) {
		Ensure.not_null(mf, "mf == null");
		Ensure.not_null(property, "property == null");
		
		Attributes attr = mf.getMainAttributes();
		for (Object k : attr.keySet()) {
			if (k.toString().equals(property)) {
				return attr.getValue(property).toString();
			}
		}
		
		return null;
	}
}
