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
package org.sa.rainbow.translator.effectors;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.translator.probes.IBashBasedScript;
import org.sa.rainbow.util.Util;

/**
 * This class defines an effector that depends on a shell/Perl script for
 * system-level changes.  It facilitates effector implementation reuse for the
 * Cygwin and Linux environments.  We assume presence of the popular BASH shell.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class GenericScriptBasedEffector extends AbstractEffector implements IBashBasedScript {
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
                    log (line);
                }
            }
            catch (IOException ioe) {
                log (ioe.getMessage ());
            }
        }

    }

    private String m_path = null;

    private String m_params = null;

    /**
     * Main Constructor.
     * @param refID  the location-unique reference identifier to match this IEffector
     * @param name   the name used to label this IEffector
     * @param path   the path to the script
     * @param paramStr  the parameters to supply as arguments to the script
     */
    public GenericScriptBasedEffector (String refID, String name, String path, String paramStr) {
        super(refID, name, Kind.SCRIPT);

        m_path = path;
        m_params = paramStr;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.effectors.IEffector#execute(java.lang.String[])
     */

    @Override
    public Outcome execute (List<String> args) {
        Outcome r;
        String[] cmds = new String[3];

        // do param substitution
        String params = m_params != null ? m_params : "";
        for (int i = 0; i < args.size (); ++i) {
            String a = args.get (i);
            a = a.replaceAll (System.getProperty ("line.separator"), "");
            a = a.replaceAll ("\n", "");
            if (a.contains ("=")) { // a key=val argument, split
                String[] kv = a.split ("\\s*=\\s*");
                params = params.replace ("{" + kv[0] + "}", "\"" + kv[1] + "\"");
            } else {  // replace by position
                params = params.replace ("{" + i + "}", "\"" + a + "\"");
            }
        }
        // Replace all remaining parameters with ""
        while (params.contains ("{")) {
            params = params.replaceAll ("\\{\\d*\\}", "''");
        }
        switch (Rainbow.instance ().environment()) {
        case CYGWIN:
            cmds[0] = CYGWIN_BASH;
            cmds[1] = BASH_OPT;
            cmds[2] = "\"" + m_path + " " + params + "\"";
            break;
        case LINUX:
            cmds[0] = LINUX_BASH;
            cmds[1] = BASH_OPT;
            cmds[2] = m_path + " " + params;
            // also set the executable permission of path
            Util.setExecutablePermission(m_path);
            break;
        default:
            r = Outcome.CONFOUNDED;
        }

        // create a process and execute it
        log("executing " + Arrays.toString(cmds) + "...");
        ProcessBuilder pb = new ProcessBuilder(cmds);
        File workDir = new File(m_path);
        pb.directory(workDir.getParentFile());
        pb.redirectErrorStream(true);
        try {
            Process p = pb.start();
//            StreamGobbler outputProcessor = new StreamGobbler (p.getInputStream ());
//            outputProcessor.start ();
            try {
//                p.waitFor (5, TimeUnit.SECONDS);
                Thread.sleep (5000);
                p.destroyForcibly ();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
//                log(" = exit status: " + p.exitValue());
//                if (p.exitValue() == 0) {
                r = Outcome.SUCCESS;
//                } else {
//                    r = Outcome.FAILURE;
//                }
            }
//            log(" --STDOUT+STDERR-----\n" + Util.getProcessOutput(p));
            log("Done!");
        } catch (IOException e) {
            LOGGER.error ("Process I/O failed!", e);
            r = Outcome.CONFOUNDED;
        }
        catch (IllegalThreadStateException e) {
            LOGGER.warn ("Process took too long - can be ignored");
            r = Outcome.TIMEOUT;
        }
        reportExecuted (r, args);
        return r;
    }

}
