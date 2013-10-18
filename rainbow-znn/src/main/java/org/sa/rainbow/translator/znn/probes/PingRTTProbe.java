package org.sa.rainbow.translator.znn.probes;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.util.RainbowLogger;
import org.sa.rainbow.translator.probes.AbstractRunnableProbe;
import org.sa.rainbow.util.Util;

/**
 * A Java Probe that uses the available external "ping" program to compute
 * Roundtrip-Time to a target host, using 3 requests and (Linux-only) preloading
 * all 3 requests.  The default target host is where the Rainbow Master resides.
 * The reported values are minimum, average, and maximum rtt in milliseconds.
 * Target hosts can be provided in a String array using the 2-parameter contructor.   
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class PingRTTProbe extends AbstractRunnableProbe {

    public static final String PROBE_TYPE = "pingrtt";
    public static final int PING_REQ_SIZE = 64;

    /** Invoke Ping on Linux with 56 (64) bytes of data, 3 preloaded requests */
    public static final String[] PING_LINUX = { "ping", "-l", "3", "-c", "3", "-s", String.valueOf(PING_REQ_SIZE-8), "" };
    /** Invoke Ping on Windows with 64 bytes of data, 3 requests */
    public static final String[] PING_WIN32 = { "ping", "-n", "3", "-l", String.valueOf(PING_REQ_SIZE), "" };

    private String[]             m_tgtHosts    = { Rainbow.getProperty (RainbowConstants.PROPKEY_MASTER_LOCATION) };

    /**
     * Default Constructor, setting ID and sleep time
     * @param id  the unique name@location identifier of the IProbe
     * @param sleepTime  milliseconds to sleep per cycle
     */
    public PingRTTProbe (String id, long sleepTime) {
        super(id, PROBE_TYPE, Kind.JAVA, sleepTime);
        LOGGER = Logger.getLogger (this.getClass ());
    }

    /**
     * Constructor to supply with array of target hosts.
     * @param id  the unique name@location identifier of the IProbe
     * @param sleepTime  milliseconds to sleep per cycle
     * @param args  String array of target host against which to check roundtrip latency
     */
    public PingRTTProbe (String id, long sleepTime, String[] args) {
        this(id, sleepTime);
        m_tgtHosts = args;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run () {
        // compose the ping command
        boolean isWin = false;
        String[] pingCmd = null;
        if (System.getProperty("os.name").indexOf("Window") > -1) {
            pingCmd = PING_WIN32;
            isWin = true;
        } else {
            pingCmd = PING_LINUX;
        }
        // build pattern
        Pattern pattern = null;
        if (isWin) {
            pattern = Pattern.compile("Minimum = ([0-9.]+)ms, Maximum = ([0-9.]+)ms, Average = ([0-9.]+)ms", Pattern.CASE_INSENSITIVE);
        } else {
            pattern = Pattern.compile("rtt min/avg/max/mdev = ([0-9.]+)/([0-9.]+)/([0-9.]+)", Pattern.CASE_INSENSITIVE);
        }
        String min, max, avg;

        /* Repeatedly execute until probe is deactivated (which means it may
         * still be reactivated later) or destroyed by unsetting the thread.
         */
        Thread currentThread = Thread.currentThread();
        while (thread() == currentThread && isActive()) {
            try {
                Thread.sleep(sleepTime());
            } catch (InterruptedException e) {
                // intentional ignore
            }
            for (String target : m_tgtHosts) {
                pingCmd[pingCmd.length-1] = target;
                // create a process and execute it
                ProcessBuilder pb = new ProcessBuilder(pingCmd);
                pb.redirectErrorStream(true);
                try {
                    Process p = pb.start();
                    try {
                        p.waitFor();
                    } catch (InterruptedException e) {
                        // intentional ignore
                    }
                    if (p.exitValue() > 1) {  // NOT GOOD
                        RainbowLogger.error (RainbowComponentT.PROBE, MessageFormat.format (
                                "PING returned a bad error code {0}, aborting! Check Probe Spec?", p.exitValue ()),
                                getLoggingPort (), LOGGER);
                        RainbowLogger.info (RainbowComponentT.PROBE,
                                MessageFormat.format ("- STDOUT+STDERR: ----\n{0}", Util.getProcessOutput (p)),
                                getLoggingPort (),
                                LOGGER);
                        tallyError();
                        continue;
                    }
                    StringBuffer buf = new StringBuffer();
                    byte[] bytes = new byte[Util.MAX_BYTES];
                    BufferedInputStream bis = new BufferedInputStream(p.getInputStream());
                    while (bis.available() > 0) {
                        int cnt = bis.read(bytes);
                        buf.append(new String(bytes, 0, cnt));
                    }
                    Matcher m = pattern.matcher(buf.toString());
                    if (m.find()) {  // report finding!
                        if (isWin) {
                            min = m.group(1);
                            max = m.group(2);
                            avg = m.group(3);
                        } else {
                            min = m.group(1);
                            avg = m.group(2);
                            max = m.group(3);
                        }
                        String rpt = "[" + Util.probeLogTimestamp() + "] " + target + ":" + min + "/" + avg + "/" + max;
                        if (LOGGER.isTraceEnabled ()) {
                            LOGGER.trace ("PingRTTProbe " + id () + " queues \"" + rpt + "\"");
                        }
                        reportData(rpt);
                        Util.dataLogger().info(rpt);
                    }
                } catch (IOException e) {
                    RainbowLogger.error (RainbowComponentT.PROBE, "Process execution error!", e, getLoggingPort (),
                            LOGGER);
                    tallyError();
                }
            }
        }
    }

}
