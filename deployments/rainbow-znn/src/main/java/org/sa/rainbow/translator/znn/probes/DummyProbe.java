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
