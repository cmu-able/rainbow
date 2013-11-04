package org.sa.rainbow.translator.znn.probes;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.sa.rainbow.translator.probes.AbstractRunnableProbe;
import org.sa.rainbow.util.Util;

public class FidelityProbe extends AbstractRunnableProbe {

    public static final String PROBE_TYPE     = "fidelity";

    private String             m_fidelityFile = null;

    public FidelityProbe (String id, long sleepTime) {
        super (id, PROBE_TYPE, Kind.JAVA, sleepTime);

    }

    public FidelityProbe (String id, long sleepTime, String[] args) {
        this (id, sleepTime);
        if (args.length == 1) {
            m_fidelityFile = args[0];
        }
    }

    @Override
    public void run () {
        String line = null;

        Thread currentThread = Thread.currentThread ();
        if (m_fidelityFile == null) {
            LOGGER.error ("No fidelity file specified");
            tallyError ();
        }
        while (thread () == currentThread && isActive ()) {
            try {
                Thread.sleep (sleepTime ());
            }
            catch (InterruptedException e) {
            }

            StringBuffer rpt = new StringBuffer ();
            String fidelity = "high";
            try (BufferedReader in = new BufferedReader (new InputStreamReader (new FileInputStream (m_fidelityFile)))) {
                line = in.readLine ();
                if (line != null) {
                    fidelity = line.trim ();
                }
            }
            catch (IOException e) {
            }
            reportData (fidelity);
            Util.dataLogger ().info (fidelity);
        }
    }

}
