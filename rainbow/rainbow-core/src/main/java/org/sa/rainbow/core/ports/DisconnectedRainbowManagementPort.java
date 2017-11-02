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

import java.util.Properties;

/**
 * Represents a deployment port that is not connected to either a master or a delegate. Any calls will log an error.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public class DisconnectedRainbowManagementPort implements IDelegateManagementPort {

    private final Logger
            LOGGER = Logger.getLogger (DisconnectedRainbowManagementPort.class);

    private static final DisconnectedRainbowManagementPort m_instance = new DisconnectedRainbowManagementPort ();


    public static IDelegateManagementPort instance () {
        return m_instance;
    }

    private DisconnectedRainbowManagementPort () {
    }


    @Override
    public String getDelegateId () {
        LOGGER.error ("Attempt to get the delegate of a disconnected deployment port");
        return "";
    }

    @Override
    public void sendConfigurationInformation (Properties configuration) {
        LOGGER.error ("Attempt to send configuration information to a disconnected deployment port");

    }

    @Override
    public void heartbeat () {
        LOGGER.error ("Attempt to receive heartbeat from a disconnected deployment port");

    }

    @Override
    public void requestConfigurationInformation () {
        LOGGER.error ("Attempt to request configuration information from a disconnected deployment port");

    }

    @Override
    public boolean startDelegate () throws IllegalStateException {
        LOGGER.error ("Attempt to start a delegate from a disconnected deployment port");
        return false;
    }

    @Override
    public boolean pauseDelegate () throws IllegalStateException {
        LOGGER.error ("Attempt to pause a delegate from a disconnected deployment port");
        return false;
    }

    @Override
    public boolean terminateDelegate () throws IllegalStateException {
        LOGGER.error ("Attempt to terminate a delegate from a disconnected deployment port");
        return false;
    }

    @Override
    public void dispose () {
    }

    @Override
    public void startProbes () throws IllegalStateException {
        LOGGER.error ("Attempt to start probes on a delegate from a disconnected deployment port");

    }

    @Override
    public void killProbes () throws IllegalStateException {
        LOGGER.error ("Attempt to kill probes on a delegate from a disconnected deployment port");

    }

}
