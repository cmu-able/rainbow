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
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Probe to report which clients are currently blackholed
 * 
 * @author jsg
 * @version 1.0
 */
public class BlackholeProbe extends AbstractRunnableProbe {

    public static final String PROBE_TYPE = "blackhole";

    /** the file that lists the blackholed clients */
    private String m_bhLogFile = null;

    /**
     * Default Constructor, setting ID and sleep time
     * 
     * @param id
     *            the unique name@location identifier of the IProbe
     * @param sleepTime
     *            milliseconds to sleep per cycle
     */
    public BlackholeProbe(String id, long sleepTime) {
        super(id, PROBE_TYPE, Kind.JAVA, sleepTime);
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
    public BlackholeProbe(String id, long sleepTime, String[] args) {
        this(id, sleepTime);
        if (args.length == 1) {
            m_bhLogFile = args[0];
        }
    }


    @Override
    public void run() {
        /*
         * build pattern to match log file. This will match the string:
         * "deny from [IP ADDRESS]". Note that this does not validate the IP
         */
        Pattern pattern = Pattern.compile(
                "deny from (\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})",
                Pattern.CASE_INSENSITIVE);

        String line = null;

        Thread currentThread = Thread.currentThread();
        if (m_bhLogFile == null) {
            LOGGER.error ("No blackhole file specified");
            tallyError();
        }
        while (thread() == currentThread && isActive()) {
            BufferedReader in = null;
            try {
                try {
                    Thread.sleep(sleepTime());
                } catch (InterruptedException e) {
                    // intentional ignore
                }
                /*
                 * assuming that the file is written periodically so re-opening
                 * it is not an issue. This is assumed to be a better approach
                 * than leaving the file open
                 */
                StringBuilder rpt = new StringBuilder ();

                in = new BufferedReader(new InputStreamReader(
                        new FileInputStream(m_bhLogFile)));

                StringBuilder clients = new StringBuilder ("");
                // Keep track of if there is anything to report
                boolean reportingBlackhole = false;
                // process the reported attackers
                while ((line = in.readLine()) != null) {

                    // parse the file, generate the report and
                    // put it on the probe bus
                    Matcher m = pattern.matcher(line);

                    if (m.matches()) { // report finding!
                        reportingBlackhole = true;
                        // append a comma + space if necessary
                        if (!clients.toString().equals("")) {
                            clients.append(", ");
                        }
                        // add the client to the list
                        clients.append(m.group(1));
                    }
                }

                // clients takes the form IP1, IP2, IP3 ... for each blackholed
                // client

                // generate the report. Note that this will result in one report
                // containing multiple clients. I am unsure how that will be
                // sent
                // to the gauge

                // rpt.append("[");
                // rpt.append(Util.probeLogTimestamp());
                // rpt.append("] ");
                rpt.append(clients);

                if (LOGGER.isTraceEnabled ()) {
                    LOGGER.trace ("BlackholeProbe " + id () + " queues \""
                            + rpt.toString() + "\"");
                }
                // send the report. If nothing to report, say this explicitly rather
                // than reporting an empty string
                String report = reportingBlackhole ? rpt.toString () : "none";
                reportData (report);

                Util.dataLogger ().info (report);

            } catch (Exception e) {
                RainbowLogger.error (RainbowComponentT.PROBE, "Process execution error!", e, getLoggingPort (), LOGGER);
                tallyError();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception e) {
                    }
                }
            }
        }
    }
}
