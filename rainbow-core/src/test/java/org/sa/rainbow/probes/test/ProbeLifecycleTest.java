package org.sa.rainbow.probes.test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sa.rainbow.core.error.BadLifecycleStepException;
import org.sa.rainbow.translator.probes.AbstractProbe;
import org.sa.rainbow.translator.probes.IProbe.Kind;
import org.sa.rainbow.translator.probes.IProbe.State;

import auxtestlib.DefaultTCase;

public class ProbeLifecycleTest extends DefaultTCase {

    class Probe extends AbstractProbe {

        public Probe (String id, String type, Kind kind) {
            super (id, type, kind);
        }

    }

    private static String s_currentDirectory;

    @BeforeClass
    public static void rememberUserDir () {
        s_currentDirectory = System.getProperty ("user.dir");
    }

    private Probe m_probe;

    @After
    public void pullDownProbe () {
        if (m_probe != null) {
            if (m_probe.lcState () == State.ACTIVE) {
                m_probe.deactivate ();
            }
            if (m_probe.lcState () == State.INACTIVE) {
                m_probe.destroy ();
            }
        }
    }

    @Before
    public void createProbe () throws IOException {
        configureTestProperties ();
        m_probe = new Probe (UUID.randomUUID ().toString (), "test", Kind.SCRIPT);
    }

    protected void configureTestProperties () throws IOException {
        File basePath = new File (System.getProperty ("user.dir"));
        File testMasterDir = new File (basePath, "src/test/resources/RainbowTest/eseb");
        System.setProperty ("user.dir", testMasterDir.getCanonicalPath ());
    }

    @Test
    public void testProbeLifecycleCorrect () {
        assertTrue (m_probe.lcState () == State.NULL);
        m_probe.create ();
        assertTrue (m_probe.lcState () == State.INACTIVE);
        m_probe.activate ();
        assertTrue (m_probe.lcState () == State.ACTIVE);
        m_probe.deactivate ();
        assertTrue (m_probe.lcState () == State.INACTIVE);
    }

    @Test (expected = BadLifecycleStepException.class)
    public void testProbeLifecycleDeactivateNonActive () {
        assertTrue (m_probe.lcState () == State.NULL);
        m_probe.deactivate ();
    }

    @Test (expected = BadLifecycleStepException.class)
    public void testProbeLifecycleActivateNonCreated () {
        assertTrue (m_probe.lcState () == State.NULL);
        m_probe.activate ();
    }

    @Test (expected = BadLifecycleStepException.class)
    public void testProbeLifecycleActivateAlreadyActivated () {
        assertTrue (m_probe.lcState () == State.NULL);
        m_probe.create ();
        m_probe.activate ();
        m_probe.activate ();
    }

    @Test (expected = BadLifecycleStepException.class)
    public void testProbeLifecycleDeactivateAlreadyDeactivated () {
        assertTrue (m_probe.lcState () == State.NULL);
        m_probe.create ();
        m_probe.activate ();
        m_probe.deactivate ();
        m_probe.deactivate ();
    }
}
