package org.sa.rainbow.core.test;

import auxtestlib.BooleanEvaluation;
import auxtestlib.DefaultTCase;
import auxtestlib.TestHelper;
import auxtestlib.TestPropertiesDefinition;
import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sa.rainbow.core.DelegateTestHelper;
import org.sa.rainbow.core.IRainbowRunnable.State;
import org.sa.rainbow.core.MasterTestHelper;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class RainbowESEBProbeAndEffectorTest extends DefaultTCase {
    @TestHelper
    public MasterTestHelper   mth;

    @TestHelper
    public DelegateTestHelper dth;

    private RainbowDelegate   m_delegate;

    private RainbowMaster     m_master;

    private static String     s_currentDirectory;

    @BeforeClass
    public static void rememberUserDir () {
        s_currentDirectory = System.getProperty ("user.dir");
    }

    @Before
    public void setupMasterAndDelegate () throws Exception {
        configureTestProperties ();

        m_master = new RainbowMaster ();
        m_delegate = new RainbowDelegate ();
    }

    protected void configureTestProperties () throws IOException {
        File basePath = new File (System.getProperty ("user.dir"));
        File testMasterDir = new File (basePath, "src/test/resources/RainbowTest/probe-test");
        System.setProperty ("user.dir", testMasterDir.getCanonicalPath ());
    }

    @After
    public void terminateMasterAndDelegate () throws Exception {
        if (m_master != null) {
            m_master.terminate ();
        }
        if (m_delegate != null) {
            m_delegate.terminate ();
        }
        wait_for_true (new BooleanEvaluation () {

            @Override
            public boolean evaluate () throws Exception {
                return (m_master != null ? m_master.state () == State.TERMINATED : true)
                        && (m_delegate != null ? m_delegate.state () == State.TERMINATED : true);
            }
        }, 5000);
    }

    @Test
    public void testReceivedEffectorAndProbeInformation () throws Exception {
        BasicConfigurator.configure ();

        int wait = TestPropertiesDefinition.getInt ("delegate.connection.time");
        m_master.initialize ();
        m_master.start ();

        m_delegate.initialize ();
        m_delegate.start ();

        dth.setDelegate (m_delegate);

        wait_for_true (new BooleanEvaluation () {

            @Override
            public boolean evaluate () throws Exception {
                return dth.isConfigured ();
            }
        }, wait);

        Set<ProbeAttributes> probes = dth.getConfiguredProbes ();
        assertTrue (!probes.isEmpty ());
        assertEquals (probes.size (), 2);

    }
}
