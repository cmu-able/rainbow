package auxtestlib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Class that obtains data from the web. Code initially copied (and later
 * changed) from http://www.devdaily.com/blog/post/java/jget-something-like-wget
 */
public final class WebGetter {
	/**
	 * Utility class: no constructor.
	 */
	private WebGetter() {
		/*
		 * Nothing to do.
		 */
	}

	/**
	 * Obtains a URL as binary data.
	 * 
	 * @param url the URL
	 * 
	 * @return the data
	 * 
	 * @throws Exception failed to obtain the data
	 */
	public static byte[] getBinary(String url) throws Exception {
		URL u;
		int b;
		InputStream is = null;
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			u = new URL(url);
			is = u.openStream();
			while ((b = is.read()) != -1) {
				os.write(b);
			}

			is.close();
			is = null;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ioe) {
					/*
					 * Double exception: ignore.
					 */
				}
			}
		}

		return os.toByteArray();
	}

	/**
	 * Obtains the text at a given URL.
	 * 
	 * @param url the URL to retrieve
	 * 
	 * @return the URL's text
	 * 
	 * @throws Exception failed to obtain the text at the URL
	 */
	public static String get(String url) throws Exception {
		URL u;
		int c;
		InputStream is = null;
		InputStreamReader isr = null;
		StringBuffer sb = new StringBuffer();

		try {
			u = new URL(url);
			is = u.openStream();
			isr = new InputStreamReader(is);
			while ((c = isr.read()) != -1) {
				sb.append((char) c);
			}

			is.close();
			is = null;
			isr.close();
			isr = null;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ioe) {
					/*
					 * Double exception: ignore.
					 */
				}
			}

			if (isr != null) {
				try {
					isr.close();
				} catch (IOException ioe) {
					/*
					 * Double exception: ignore.
					 */
				}
			}
		}

		return sb.toString();
	}
}
