package incubator.rmissl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

/*
 * Code copied from http://www.howardism.org/Technical/Java/SelfSignedCerts.html
 * with some minor changes.
 */

/**
 * This Trust Manager is "naive" because it trusts everyone.
 **/
public class NaiveTrustManager implements X509TrustManager {
	@Override
	public void checkClientTrusted(X509Certificate[] cert, String authType)
			throws CertificateException {
		/*
		 * Doesn't throw an exception, so this is how it approves a certificate.
		 */ 
	}

	@Override
	public void checkServerTrusted(X509Certificate[] cert, String authType)
			throws CertificateException {
		/*
		 * Doesn't throw an exception, so this is how it approves a certificate.
		 */
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return null; // I've seen someone return new X509Certificate[ 0 ];
	}
}
