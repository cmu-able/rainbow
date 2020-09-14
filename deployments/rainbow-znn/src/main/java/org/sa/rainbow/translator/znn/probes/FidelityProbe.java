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

import org.sa.rainbow.translator.probes.AbstractRunnableProbe;
import org.sa.rainbow.util.Util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

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

            StringBuilder rpt = new StringBuilder ();
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
