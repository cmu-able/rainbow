package org.sa.rainbow.core.ports.eseb;

import java.io.IOException;

import auxtestlib.AbstractTestHelper;

public class ESEBProbeSubscriberPortHelper extends AbstractTestHelper {

    public ESEBProbeSubscriberPortHelper () throws Exception {
        super ();
        // TODO Auto-generated constructor stub
    }

    public ESEBProbeReportSubscriberPort disconnectedPort () {
        try {
            return new ESEBProbeReportSubscriberPort ();
        }
        catch (IOException e) {
            // Should never happen
        }
        return null;
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
