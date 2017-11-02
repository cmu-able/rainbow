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
/**
 * Created March 19, 2007, copied from GenericScriptBasedEffector.
 */
package org.sa.rainbow.translator.probes;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.util.RainbowLogger;
import org.sa.rainbow.util.Util;

/**
 * This class defines a probe that's implemented in a shell/Perl script.
 * It facilitates probe reuse for the Cygwin and Linux environments.
 * We assume presence of the popular BASH shell.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class GenericScriptBasedProbe extends AbstractProbe implements IBashBasedScript {

    public class StreamGobbler extends Thread {

        private final InputStream m_inputStream;

        public StreamGobbler (InputStream inputStream) {
            m_inputStream = inputStream;
        }

        @Override
        public void run () {
            try {
                InputStreamReader isr = new InputStreamReader (m_inputStream);
                BufferedReader br = new BufferedReader (isr);
                String line;
                while ((line = br.readLine ()) != null) {
                    reportData (line);
                }
            }
            catch (IOException ioe) {
                log (ioe.getMessage ());
            }
        }

    }


    private String m_path = null;

    private String m_params = null;

    private Process m_process = null;
    private boolean m_continual;

    private boolean m_cleanup = false;

    /**
     * Main Constructor.
     * @param refID  the location-unique reference identifier to match this IProbe
     * @param alias  the alias used to label this IProbe, aka its type
     * @param path   the path to the script
     * @param paramStr  the parameters to supply as arguments to the script
     */
    public GenericScriptBasedProbe (String refID, String alias, String path, String paramStr) {
        super(refID, alias, Kind.SCRIPT);

        m_path = path;
        m_params = paramStr;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.probes.AbstractProbe#activate()
     */
    @Override
    public synchronized void activate() {
        super.activate();

        String[] cmds = new String[3];
        switch (Rainbow.instance ().environment()) {
        case CYGWIN:
            cmds[0] = CYGWIN_BASH;
            break;
        case LINUX:
            cmds[0] = LINUX_BASH;
            // also set the executable permission of path
            Util.setExecutablePermission(m_path);
            break;
        }
        if (cmds[0] == null) {
            deactivate();
        } else {
            cmds[1] = BASH_OPT;
            cmds[2] = m_path + " " + m_params;
            // create a process and execute it
            ProcessBuilder pb = new ProcessBuilder(cmds);
            File workDir = new File(m_path).getParentFile();
            pb.directory(workDir);
            pb.redirectErrorStream(true);
            try {
                m_cleanup = false;
                m_process = pb.start();
                if (m_continual) {
                    StreamGobbler outputProcessor = new StreamGobbler (m_process.getInputStream ());
                    outputProcessor.start ();
                    m_cleanup = true;
                }
                else {

                    m_process.waitFor ();
//                    int exitValue = m_process.exitValue ();
                    reportData (Util.getProcessOutput (m_process));
//                    dumpOutput();
                    m_cleanup = true;
                }
            } catch (IOException e) {
                RainbowLogger.error (RainbowComponentT.PROBE, "Process I/O failed!", e, getLoggingPort (), LOGGER);
            }
            catch (InterruptedException e) {
            }
        }
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.probes.AbstractProbe#deactivate()
     */
    @Override
    public synchronized void deactivate() {
        if (m_process != null) {
            // exhaust output to make sure process completes
            dumpOutput();

            m_process.destroy();
            log("- Process destroyed.");
            m_process = null;
        }
        super.deactivate();
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.probes.AbstractProbe#isAlive()
     */
    @Override
    public boolean isAlive() {
        boolean alive = true;
        if (m_process != null && m_cleanup) {
            try {
                if (m_process.exitValue() == 0) {  // done, cleanup
                    m_process = null;
                } else {
                    alive = false;
                    dumpOutput();
                }
            } catch (IllegalThreadStateException e) {
                // ignore! this is actually good, meaning process is still running
            }
        }
        return alive;
    }

    private void dumpOutput () {
        log("- STDOUT+STDERR: ----\n" + Util.getProcessOutput(m_process));
    }

    public void setContinual (boolean b) {
        m_continual = b;
    }

}
