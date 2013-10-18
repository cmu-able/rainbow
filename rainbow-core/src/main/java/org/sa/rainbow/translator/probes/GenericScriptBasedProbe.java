/**
 * Created March 19, 2007, copied from GenericScriptBasedEffector.
 */
package org.sa.rainbow.translator.probes;

import java.io.File;
import java.io.IOException;

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

    private String m_path = null;
    private String m_params = null;
    private Process m_process = null;

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
        switch (Rainbow.environment()) {
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
                m_process = pb.start();
                dumpOutput();
            } catch (IOException e) {
                RainbowLogger.error (RainbowComponentT.PROBE, "Process I/O failed!", e, getLoggingPort (), LOGGER);
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
        if (m_process != null) {
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

}
