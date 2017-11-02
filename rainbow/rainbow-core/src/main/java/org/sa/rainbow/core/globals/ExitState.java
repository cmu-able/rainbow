package org.sa.rainbow.core.globals;

import org.sa.rainbow.core.RainbowConstants;

/**
 * States used to help the Rainbow daemon process determine what to do after this Rainbow component exits.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public enum ExitState {
    /**
     * Completely clear out (daemon dies) after the Rainbow component exits (default).
     */
    DESTRUCT,
    /** Restart the Rainbow component after exits. */
    RESTART,
    /**
     * After the Rainbow component exits, sleep and await awake command to restart Rainbow.
     */
    SLEEP,
    /** Abort of operation. */
    ABORT;


    public static ExitState parseState (int val) {
        ExitState st = ExitState.DESTRUCT;
        switch (val) {
        case RainbowConstants.EXIT_VALUE_DESTRUCT:
            st = ExitState.DESTRUCT;
            break;
        case RainbowConstants.EXIT_VALUE_RESTART:
            st = ExitState.RESTART;
            break;
        case RainbowConstants.EXIT_VALUE_SLEEP:
            st = ExitState.SLEEP;
            break;
        case RainbowConstants.EXIT_VALUE_ABORT:
            st = ExitState.ABORT;
            break;
        }
        return st;
    }

    public int exitValue () {
        int ev = 0;
        switch (this) {
        case DESTRUCT:
            ev = RainbowConstants.EXIT_VALUE_DESTRUCT;
            break;
        case RESTART:
            ev = RainbowConstants.EXIT_VALUE_RESTART;
            break;
        case SLEEP:
            ev = RainbowConstants.EXIT_VALUE_SLEEP;
            break;
        case ABORT:
            ev = RainbowConstants.EXIT_VALUE_ABORT;
            break;
        }
        return ev;
    }
}
