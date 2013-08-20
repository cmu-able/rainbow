package org.sa.rainbow.core;

import java.util.Map;

import org.sa.rainbow.util.Beacon;

import auxtestlib.AbstractTestHelper;

public class MasterTestHelper extends AbstractTestHelper {

    public MasterTestHelper () throws Exception {
        super ();
    }

    private RainbowMaster m_master;

    public void setMaster (RainbowMaster m) {
        m_master = m;

    }

    public Beacon getBeaconFor (String delegateId) {
        Map<? extends String, ? extends Beacon> heartbeatInfo = m_master.getHeartbeatInfo ();
        return heartbeatInfo.get (delegateId);
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
