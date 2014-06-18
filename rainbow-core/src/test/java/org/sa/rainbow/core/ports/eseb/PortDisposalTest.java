package org.sa.rainbow.core.ports.eseb;

import java.io.File;

import org.junit.Test;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.error.RainbowAbortException;

import auxtestlib.AbstractTestHelper;

public class PortDisposalTest extends AbstractTestHelper {

    public PortDisposalTest () throws Exception {
        super ();
    }

    protected void configureTestProperties () throws Exception {
        File basePath = new File (System.getProperty ("user.dir"));
        File testMasterDir = new File (basePath, "src/test/resources/RainbowTest/eseb");
        System.setProperty ("user.dir", testMasterDir.getCanonicalPath ());
    }

    @Test
    public void testOnePort () throws Exception {
        configureTestProperties ();
        ESEBMasterReportingPort port = new ESEBMasterReportingPort ();
        port.info (RainbowComponentT.MASTER, "Should not be an error");
        port.dispose ();
        try {
            port.error (RainbowComponentT.MASTER, "Should be an error");
            fail ("Last port report should have thrown an exception because the connection was closed");
        }
        catch (RainbowAbortException e) {
        }
    }

    @Test
    public void testTwoPorts () throws Exception {
        configureTestProperties ();
        ESEBMasterReportingPort port1 = new ESEBMasterReportingPort ();
        ESEBMasterReportingPort port2 = new ESEBMasterReportingPort ();

        port1.info (RainbowComponentT.MASTER, "port1");
        port2.info (RainbowComponentT.MASTER, "port2");

        port1.dispose ();
        port2.info (RainbowComponentT.MASTER, "port2 second message");
        try {
            port1.error (RainbowComponentT.MASTER, "port1 should give error");
            fail ("port1 should be closed");
        }
        catch (RainbowAbortException e) {
        }
        port2.dispose ();
        try {
            port2.error (RainbowComponentT.MASTER, "port 2 should give an exception");
            fail ("port2 should be closed");
        }
        catch (RainbowAbortException e) {
        }

    }

    @Test
    public void testTwoPortsOfDifferentType () throws Exception {
        configureTestProperties ();
        ESEBMasterReportingPort port1 = new ESEBMasterReportingPort ();
        ESEBChangeBusAnnouncePort port2 = new ESEBChangeBusAnnouncePort ();

        port2.announce (port2.createMessage ());
        port1.dispose ();

        // This should still work
        port2.announce (port2.createMessage ());

        port2.dispose ();

    }

    @Override
    protected void mySetUp () throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    protected void myTearDown () throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    protected void myCleanUp () throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    protected void myPrepareFixture () throws Exception {
        // TODO Auto-generated method stub

    }

}
