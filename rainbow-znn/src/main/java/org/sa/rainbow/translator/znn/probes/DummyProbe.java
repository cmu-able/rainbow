package org.sa.rainbow.translator.znn.probes;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import org.sa.rainbow.translator.probes.AbstractRunnableProbe;
import org.sa.rainbow.util.Util;

/**
 * This probe takes a file as an argument and every cycle reads the file, announcing each line on the probe bus
 * 
 * @author schmerl
 * 
 */
public class DummyProbe extends AbstractRunnableProbe {

    private static final String PROBE_TYPE = "dummymaliciousness";
    private String              m_probeFile;

    public DummyProbe (String id, long sleepTime) {
        super (id, PROBE_TYPE, Kind.JAVA, sleepTime);
    }

    public DummyProbe (String id, long sleepTime, String[] args) {
        this (id, sleepTime);
        if (args.length == 1) {
            m_probeFile = args[0];
        }
    }

    @Override
    public void run () {
        Thread currentThread = Thread.currentThread ();
        if (m_probeFile == null) {
            LOGGER.error ("No probe file specified");
            tallyError ();
        }
        while (thread () == currentThread && isActive ()) {
            try {
                Thread.sleep (sleepTime ());
            }
            catch (InterruptedException e) {
            }
            File f = new File (m_probeFile);
            if (!f.exists ()) {
                continue;
            }
            BufferedReader reader = null;
            try {
                reader = Files.newBufferedReader (f.toPath (), Charset.forName ("US-ASCII"));
                String line = null;
                while ((line = reader.readLine ()) != null) {
                    reportData (line);
                    Util.dataLogger ().info (line);
                }
            }
            catch (IOException e) {
            }
        }
    }

}
