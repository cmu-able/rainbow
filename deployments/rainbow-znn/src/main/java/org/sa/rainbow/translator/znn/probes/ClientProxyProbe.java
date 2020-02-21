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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.IRainbowRunnable;
import org.sa.rainbow.translator.probes.AbstractRunnableProbe;
import org.sa.rainbow.util.Beacon;
import org.sa.rainbow.util.Util;

/**
 * A probe that acts like an end-user client connecting to the newssite, to help determine client's experienced response
 * time.
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class ClientProxyProbe extends AbstractRunnableProbe {

    public static final String PROBE_TYPE = "clientproxy";

    private String[]           m_tgtUrls  = {};

    /**
     * Default Constructor, setting ID and sleep time
     * 
     * @param id
     *            the unique name@location identifier of the IProbe
     * @param sleepTime
     *            milliseconds to sleep per cycle
     */
    public ClientProxyProbe (String id, long sleepTime) {
        super (id, PROBE_TYPE, Kind.JAVA, sleepTime);
        LOGGER = Logger.getLogger (getClass ());

    }

    /**
     * Constructor to supply with array of target URLs.
     * 
     * @param id
     *            the unique name@location identifier of the IProbe
     * @param sleepTime
     *            milliseconds to sleep per cycle
     * @param args
     *            String array of target URLs against which to check time
     */
    public ClientProxyProbe (String id, long sleepTime, String[] args) {
        this (id, sleepTime);
        m_tgtUrls = args;
    }

    @Override
    public void run () {
        byte[] bytes = new byte[Util.MAX_BYTES];

        /* Repeatedly execute until probe is deactivated (which means it may
         * still be reactivated later) or destroyed by unsetting the thread.
         */
        Beacon timer = new Beacon (IRainbowRunnable.LONG_SLEEP_TIME); // use this to determine to report
        Thread currentThread = Thread.currentThread ();
        while (thread () == currentThread && isActive ()) {
            for (String urlStr : m_tgtUrls) {
                timer.mark ();
                try {
                    URL url = new URL (urlStr);
                    HttpURLConnection httpConn = (HttpURLConnection )url.openConnection ();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
                    int cnter = 0;
                    long startTime = System.currentTimeMillis ();
                    int length = httpConn.getContentLength ();
                    BufferedInputStream in = new BufferedInputStream (httpConn.getInputStream ());
                    while (in.available () > 0 || cnter < length) {
                        int cnt = in.read (bytes);
                        baos.write (bytes, 0, cnt);
                        cnter += cnt;
                        if (cnter < length) {
                            if (timer.periodElapsed ()) { // make an intermediate report
                                String rpt = "[" + Util.probeLogTimestamp () + "] " + url.getHost () + ":"
                                        + (System.currentTimeMillis () - startTime) + "ms";
                                reportData (rpt);
                                Util.dataLogger ().info (rpt);
                                timer.mark (); // reset timer
                            }
                            try { // sleep just a little as to not saturate CPU
                                Thread.sleep (IRainbowRunnable.SHORT_SLEEP_TIME);
                            }
                            catch (InterruptedException e) {
                                // intentional ignore
                            }
                        }
                    }
                    long endTime = System.currentTimeMillis ();
                    String rpt = "[" + Util.probeLogTimestamp () + "]<" + id () + "> " + url.getHost () + ":"
                            + (endTime - startTime) + "ms";
                    reportData (rpt);
                    Util.dataLogger ().info (rpt);
                    if (LOGGER.isTraceEnabled ()) {
                        LOGGER.trace ("Content-type: " + httpConn.getContentType ());
                        LOGGER.trace ("Content-length: " + httpConn.getContentLength ());
                        LOGGER.trace (baos.toString ());
                        LOGGER.trace ("ClientProxyProbe " + id () + " queues \"" + rpt + "\"");
                    }
                    httpConn.disconnect ();
                }
                catch (MalformedURLException e) {
                    LOGGER.error ("Bad URL provided in Probe Spec?", e);
                    tallyError ();
                }
                catch (IOException e) {
                    LOGGER.error ("HTTP connection error!", e);
                    tallyError ();
                }
            }
            try {
                Thread.sleep (sleepTime ());
            }
            catch (InterruptedException e) {
                // intentional ignore
            }
        }
    }

}
