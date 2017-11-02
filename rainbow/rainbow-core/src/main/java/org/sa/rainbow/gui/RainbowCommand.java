package org.sa.rainbow.gui;

import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.globals.ExitState;
import org.sa.rainbow.core.ports.IMasterCommandPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;

public class RainbowCommand {

    private IMasterCommandPort m_master = null;

    public RainbowCommand () throws RainbowConnectionException {
        int numTries = 3;
        RainbowConnectionException ex = null;
        while (m_master == null && numTries > 0) {
            try {
                m_master = RainbowPortFactory.createMasterCommandPort ();
            }
            catch (RainbowConnectionException e) {
                ex = e;
                numTries--;
                try {
                    Thread.sleep (5000);
                }
                catch (InterruptedException e1) {
                }
            }
        }
        if (m_master == null) throw ex;
    }

    public void waitForDelegates () {
        while (!m_master.allDelegatesOK ()) {
            try {
                Thread.sleep (1000);
            }
            catch (InterruptedException e) {
            }
        }
    }

    public void startProbes () {
        waitForDelegates ();
        m_master.startProbes ();
    }

    public void quit () {
        m_master.destroyDelegates ();
        m_master.terminate (ExitState.DESTRUCT);
    }


    public static void main (String[] args) {
        if (args.length != 1) {
            usage (args);
            System.exit (1);
        }
        try {
            RainbowCommand rc = new RainbowCommand ();
            if ("wait".equals (args[0])) {
                rc.waitForDelegates ();
            }
            else if ("start".equals (args[0])) {
                rc.startProbes ();
            }
            else if ("quit".equals (args[0])) {
                rc.quit ();
            }
            System.exit (0);
        }
        catch (RainbowConnectionException e) {
            System.err.println ("Could not connect to Rainbow");
            System.exit (1);
        }
    }

    private static void usage (String[] args) {
        System.err.println ("Usage: wait | start | quit");
    }

}
