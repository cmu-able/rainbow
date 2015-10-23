/*
 * The MIT License
 *
 * Copyright 2014 CMU ABLE Group.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.sa.rainbow.core.ports.eseb.rpc;

import edu.cmu.cs.able.eseb.participant.ParticipantException;
import edu.cmu.cs.able.typelib.jconv.TypelibJavaConversionRule;
import org.sa.rainbow.core.ConfigHelper;
import org.sa.rainbow.core.ports.IDisposablePort;
import org.sa.rainbow.core.ports.eseb.ESEBProvider;
import org.sa.rainbow.core.ports.eseb.ESEBRPCConnector;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

public class AbstractESEBDisposableRPCPort implements IDisposablePort {


    private ESEBRPCConnector m_connectionRole;
    private final String m_host;
    private final short m_port;
    private final String m_id;

    AbstractESEBDisposableRPCPort (String host, short port, String id) {
        m_host = host;
        m_port = port;
        m_id = id;
    }


    @Override
    public void dispose () {
        try {
            getConnectionRole().close ();
            m_connectionRole = null;
        } catch (IOException | ParticipantException e) {
            ESEBRPCConnector.LOGGER.error (e);
        }
    }


    ESEBRPCConnector getConnectionRole () throws IOException, ParticipantException {
        if (m_connectionRole == null) {
            initializeConnectionRole ();
        }
        return m_connectionRole;
    }

    private void initializeConnectionRole () throws IOException, ParticipantException {
        m_connectionRole = new ESEBRPCConnector (m_host, m_port, m_id);

    }

    void setupModelConverters (String key) {
        List<String> converters = ConfigHelper.getProperties (key);
        if (converters.isEmpty ()) {
            ESEBRPCConnector.LOGGER
            .warn ("No model converters were found. Errors could ensue when using this connector.");
        }
        else {
            for (String cvt : converters) {
                try {
                    Class<?> cls = Class.forName (cvt);
                    ESEBProvider.registerConverter ((Class<TypelibJavaConversionRule> )cls);
                }
                catch (ClassNotFoundException e) {
                    ESEBRPCConnector.LOGGER.error (MessageFormat
                            .format ("Could not find converter class ''{0}''.", cvt));
                }
            }
        }
    }


}
