package org.sa.rainbow.core;

import auxtestlib.AbstractTestHelper;
import org.sa.rainbow.core.RainbowDelegate.ConnectionState;
import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;

import java.util.Collections;
import java.util.Set;

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

    public Set<ProbeAttributes> getConfiguredProbes () {
        if (m_delegate == null)
            return Collections.emptySet ();
        else
            return m_delegate.getProbeConfiguration ();
    }

    public Set<EffectorAttributes> getConfiguredEffectors () {
        if (m_delegate == null)
            return Collections.emptySet ();
        else
            return m_delegate.getEffectorConfiguration ();
    }

}
