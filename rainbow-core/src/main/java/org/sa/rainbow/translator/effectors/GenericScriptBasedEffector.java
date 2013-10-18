package org.sa.rainbow.translator.effectors;

import java.io.File;
import java.io.IOException;
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
        Outcome r = Outcome.UNKNOWN;
        String[] cmds = new String[3];

        // do param substitution
        String params = m_params;
        for (int i = 0; i < args.size (); ++i) {
            String a = args.get (i);
            if (a.indexOf ("=") > -1) { // a key=val argument, split
                String[] kv = a.split ("\\s*=\\s*");
                params = params.replace("{"+kv[0]+"}", kv[1]);
            } else {  // replace by position
                params = params.replace ("{" + i + "}", a);
            }
        }

        switch (Rainbow.environment()) {
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
            try {
                p.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                log(" = exit status: " + p.exitValue());
                if (p.exitValue() == 0) {
                    r = Outcome.SUCCESS;
                } else {
                    r = Outcome.FAILURE;
                }
            }
            log(" --STDOUT+STDERR-----\n" + Util.getProcessOutput(p));
            log("Done!");
        } catch (IOException e) {
            LOGGER.error ("Process I/O failed!", e);
            r = Outcome.CONFOUNDED;
        }
        reportExecuted (r, args);
        return r;
    }

}
