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
package org.sa.rainbow.translator.znn.probes;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.util.RainbowLogger;
import org.sa.rainbow.translator.probes.AbstractRunnableProbe;
import org.sa.rainbow.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThrottleProbe extends AbstractRunnableProbe {

    public final static String PROBE_TYPE         = "throttle";

    private String             m_throttleConfFile = null;

    public ThrottleProbe (String id, long sleepTime) {
        super (id, PROBE_TYPE, Kind.JAVA, sleepTime);
        LOGGER = Logger.getLogger (this.getClass ());
    }

    /**
     * Constructor to supply with array of target hosts.
     * 
     * @param id
     *            the unique name@location identifier of the IProbe
     * @param sleepTime
     *            milliseconds to sleep per cycle
     * @param args
     *            the log file that contains a list of black-holed clients
     */
    public ThrottleProbe (String id, long sleepTime, String[] args) {
        this (id, sleepTime);
        if (args.length == 1) {
            m_throttleConfFile = args[0];
        }
    }

    @Override
    public void run () {
        Pattern pattern = Pattern
                .compile ("SecRule REMOTE_ADDR \\\"\\^(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\\"");
        String line = null;
        Thread currentThread = Thread.currentThread ();

        if (m_throttleConfFile == null) {
            LOGGER.error ("No throttle conf file specified");
            tallyError ();
        }
        while (thread () == currentThread && isActive ()) {
            try {
                Thread.sleep (sleepTime ());
            }
            catch (InterruptedException e) {

            }
            StringBuilder rpt = new StringBuilder ();
            boolean reportingThrottle = false;
            if (new File (m_throttleConfFile).exists ()) {
                try (BufferedReader in = new BufferedReader (new InputStreamReader (new FileInputStream (
                        m_throttleConfFile)))) {
                    StringBuilder clients = new StringBuilder ();
                    while ((line = in.readLine ()) != null) {
                        Matcher m = pattern.matcher (line);
                        if (m.find ()) {
                            reportingThrottle = true;
                            if (clients.length () != 0) {
                                clients.append (", ");
                            }
                            clients.append (m.group (1));
                        }
                    }
                    rpt.append (clients);
                }
                catch (Exception e) {
                    RainbowLogger.error (RainbowComponentT.PROBE, "Process execution error!", e, getLoggingPort (),
                            LOGGER);
                    tallyError ();
                }
            }
            if (!reportingThrottle) {
                reportData ("none");
            }
            else {
                reportData (rpt.toString ());
            }
            Util.dataLogger ().info (rpt.toString ());
        }
    }

}
