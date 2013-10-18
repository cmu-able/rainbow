package org.sa.rainbow.translator.znn.probes;

import java.io.File;

import org.sa.rainbow.translator.probes.AbstractRunnableProbe;
import org.sa.rainbow.util.Util;

public class CaptchaProbe extends AbstractRunnableProbe {

    private static final String PROBE_TYPE = "captchaprobe";
    private String              m_captchaFile;

    public CaptchaProbe (String id, long sleepTime) {
        super (id, PROBE_TYPE, Kind.JAVA, sleepTime);
    }

    public CaptchaProbe (String id, long sleepTime, String[] args) {
        this (id, sleepTime);
        if (args.length == 1) {
            m_captchaFile = args[0];
        }
    }

    @Override
    public void run () {
        Thread currentThread = Thread.currentThread ();
        if (m_captchaFile == null) {
            LOGGER.error ("No captcha file specified");
            tallyError ();
        }
        while (thread () == currentThread && isActive ()) {
            try {
                Thread.sleep (sleepTime ());
            }
            catch (InterruptedException e) {
            }
            File f = new File (m_captchaFile);
            boolean captchaOn = f.exists ();
            String rpt = captchaOn ? "on" : "off";
            reportData (rpt);
            Util.dataLogger ().info (rpt);
        }
    }

}
