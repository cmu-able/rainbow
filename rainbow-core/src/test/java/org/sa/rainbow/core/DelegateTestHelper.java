package org.sa.rainbow.core;

import org.sa.rainbow.core.RainbowDelegate.ConnectionState;

import auxtestlib.AbstractTestHelper;

public class DelegateTestHelper extends AbstractTestHelper {

    private RainbowDelegate m_delegate;

    public DelegateTestHelper () throws Exception {
        super ();
        // TODO Auto-generated constructor stub
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

    public void setDelegate (RainbowDelegate delegate) {
        m_delegate = delegate;
    }

    public boolean isConnected () {
        return m_delegate != null && m_delegate.getConnectionState () == ConnectionState.CONNECTED;
    }

    public boolean isConfigured () {
        return m_delegate != null && m_delegate.getConnectionState () == ConnectionState.CONFIGURED;
    }

}
