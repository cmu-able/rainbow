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
import org.sa.rainbow.core.*;
import org.sa.rainbow.core.IRainbowRunnable.State;
import org.sa.rainbow.util.Beacon;

import java.io.IOException;
import java.util.Properties;

/**
 * Base test class for testing lifecycle information. For each kind of connector (ESEB, Local, RMI), a test should
 * inherit this class and override the configureTestProperties to ensure that the right property for constructing the
 * connectors is used.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public abstract class RainbowConnectionAndLifecycleTest extends DefaultTCase {

    @TestHelper
    public MasterTestHelper mth;

    @TestHelper
    public DelegateTestHelper dth;

    private RainbowDelegate m_delegate;

    private RainbowMaster   m_master;

    private static String   s_currentDirectory;

    @BeforeClass
    public static void rememberUserDir () {
        s_currentDirectory = System.getProperty ("user.dir");
    }

    /**
     * Test that a delegate receives configuration information after connecting.
     * 
     * @throws Exception
     */
    @Test
    public void testReceivedConfigurationInfo () throws Exception {
        BasicConfigurator.configure ();

        int wait = TestPropertiesDefinition.getInt ("delegate.connection.time");
        m_master.initialize ();
        m_master.start ();
        m_delegate.initialize ();

        m_delegate.start ();
        wait_for_true (new BooleanEvaluation () {

            @Override
            public boolean evaluate () throws Exception {
                return m_delegate.getConfigurationInformation () != null
                        && m_delegate.getConfigurationInformation ().getProperty ("test.configuration.property") != null;
            }
        }, wait);
        Properties props = m_delegate.getConfigurationInformation ();
        assertNotNull (props);
        assertEquals ("xxx", props.getProperty ("test.configuration.property"));
    }


    /**
     * Tests that a master receives a heartbeat after a delegate connects and is configured
     * 
     * @throws Exception
     */
    @Test
    public void testHeartbeatSetup () throws Exception {
        BasicConfigurator.configure ();

        // Start a delegate and a master
        m_master.initialize ();

        mth.setMaster (m_master);
        m_master.start ();

        m_delegate.initialize ();

        m_delegate.start ();

        final int extra = TestPropertiesDefinition.getInt ("heartbeat.extra.time");

        // Wait for the heartbeat to arrive
        Thread.sleep (Integer.valueOf (Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_DELEGATE_BEACONPERIOD))
                + extra);
        Beacon b = mth.getBeaconFor (m_delegate.getId ());
        assertTrue (b != null);
        assertFalse (b.periodElapsed ());


    }

    protected abstract void configureTestProperties () throws IOException;

    @Before
    public void setupMasterAndDelegate () throws Exception {
        configureTestProperties ();

        m_master = new RainbowMaster ();
        m_delegate = new RainbowDelegate ();
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

    /**
     * Tests that lifecycle operations work correctly. E.g., hearbeats are not sent if a delegate is paused, but are
     * sent after it is restarted.
     * 
     * @throws Exception
     */
    @Test
    public void testPauseAndRestart () throws Exception {
        BasicConfigurator.configure ();

        m_master.initialize ();
        mth.setMaster (m_master);
        m_master.start ();
        dth.setDelegate (m_delegate);

        m_delegate.initialize ();
        m_delegate.start ();


        // Wait for things to get connected
        wait_for_true (new BooleanEvaluation () {

            @Override
            public boolean evaluate () throws Exception {
                return dth.isConfigured ();
            }
        }, 6000);


        m_delegate.stop ();
        wait_for_true (new BooleanEvaluation () {

            @Override
            public boolean evaluate () throws Exception {
                return m_delegate.state () == State.STOPPED;
            }
        }, 5000);
        // Wait for heartbeat period to ensure that a heartbeat isn't received, so the delegate is truly paused
        final int extra = TestPropertiesDefinition.getInt ("heartbeat.extra.time");
        int heartbeatTime = Integer.valueOf (Rainbow.instance ().getProperty (
                RainbowConstants.PROPKEY_DELEGATE_BEACONPERIOD))
                + extra;

        final Beacon b = mth.getBeaconFor (m_delegate.getId ());
        assertTrue (b != null);
        wait_for_true (new BooleanEvaluation () {

            @Override
            public boolean evaluate () throws Exception {
                return b.periodElapsed ();
            }
        }, heartbeatTime * 2);

        m_delegate.start ();
        wait_for_true (new BooleanEvaluation () {

            @Override
            public boolean evaluate () throws Exception {
                return m_delegate.state () == State.STARTED;
            }
        }, 5000);

        Thread.sleep (heartbeatTime);
        Beacon b2 = mth.getBeaconFor (m_delegate.getId ());
        assertTrue (b2 != null);
        assertFalse (b2.periodElapsed ());
    }

}
