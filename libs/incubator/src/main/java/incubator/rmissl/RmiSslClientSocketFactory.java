package incubator.rmissl;

/*
 * File modified from RMISSLServerSocketFactory in JSSE examples.
 */

/*
 * @(#)RMISSLClientSocketFactory.java	1.3 01/05/10
 *
 * Copyright 1995-2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or 
 * without modification, are permitted provided that the following 
 * conditions are met:
 * 
 * -Redistributions of source code must retain the above copyright  
 * notice, this  list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduct the above copyright 
 * notice, this list of conditions and the following disclaimer in 
 * the documentation and/or other materials provided with the 
 * distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of 
 * contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any 
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND 
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY 
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY 
 * DAMAGES OR LIABILITIES  SUFFERED BY LICENSEE AS A RESULT OF  OR 
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE SOFTWARE OR 
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE 
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, 
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER 
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF 
 * THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that Software is not designed, licensed or 
 * intended for use in the design, construction, operation or 
 * maintenance of any nuclear facility. 
 */

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * Class that implements an RMI socket factory creating client sockets over SSL.
 * This class will accept connections to servers using self-signed certificates.
 */
public class RmiSslClientSocketFactory implements RMIClientSocketFactory,
		Serializable {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The SSL socket factory.
	 */
	private transient SSLSocketFactory factory;

	/**
	 * Creates an RMI SSL client socket factory.
	 * 
	 * @throws Exception failed to create the client socket factory
	 */
	public RmiSslClientSocketFactory() throws Exception {
		/*
		 * Nothing to do.
		 */
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException {
		synchronized (this) {
			if (factory == null) {
				TrustManager[] tm = new TrustManager[] { new NaiveTrustManager() };
				SSLContext context = null;
				try {
					context = SSLContext.getInstance("SSL");
					context.init(new KeyManager[0], tm, new SecureRandom());
				} catch (NoSuchAlgorithmException e) {
					throw new IOException(
							"Failed to initialize socket factory.", e);
				} catch (KeyManagementException e) {
					throw new IOException(
							"Failed to initialize socket factory.", e);
				}

				factory = context.getSocketFactory();
			}
		}

		SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
		return socket;
	}
}
