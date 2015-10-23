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
package org.sa.rainbow.core.ports;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.error.RainbowConnectionException;

import java.text.MessageFormat;
import java.util.Properties;

/**
 * Represetns a connection port that is not connected to anything. Any calls will be logged as an error.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public class DisconnectedRainbowMasterConnectionPort implements IMasterConnectionPort {

    private static final DisconnectedRainbowMasterConnectionPort m_instance = new DisconnectedRainbowMasterConnectionPort ();


    public static IMasterConnectionPort instance () {
        return m_instance;
    }

    private DisconnectedRainbowMasterConnectionPort () {
    }

    private final Logger LOGGER = Logger.getLogger (DisconnectedRainbowMasterConnectionPort.class);


    @Override
    public IDelegateManagementPort connectDelegate (String delegateID, Properties connectionProperties)
            throws RainbowConnectionException {
        LOGGER.error ("Attempt to connect through a disconnected port!");
        return DisconnectedRainbowManagementPort.instance ();
    }

    @Override
    public void disconnectDelegate (String delegateId) {
        LOGGER.error ("Attempt to disconnect through a disconnected port!");

    }

    @Override
    public void dispose () {
    }

    @Override
    public void report (String delegateID, ReportType type, RainbowComponentT compT, String msg) {
        String log = MessageFormat.format ("Delegate[{3}]: {0}: {1}", delegateID, msg, compT.name ());
        switch (type) {
        case INFO:
            LOGGER.info (log);
            break;
        case WARNING:
            LOGGER.warn (log);
            break;
        case ERROR:
            LOGGER.error (log);
            break;
        case FATAL:
            LOGGER.fatal (log);
            break;
        }
    }

}
