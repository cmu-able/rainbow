package org.sa.rainbow.translator.probes;

/**
 * Created to house common properties for BASH-based scripts.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public interface IBashBasedScript {

    /* We're using UNIX path */
    public static final String FILESEP = "/";
    public static final String CYGWIN_BASH = "c:/server/cygwin/bin/bash.exe";
    public static final String LINUX_BASH = "/bin/bash";
    public static final String BASH_OPT = "-c";
    public static final String LINUX_CHMOD = "/bin/chmod";
    public static final String CHMOD_OPT = "u+x";

}
