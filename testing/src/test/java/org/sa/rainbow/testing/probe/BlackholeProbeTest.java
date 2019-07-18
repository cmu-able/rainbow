package org.sa.rainbow.testing.probe;

import org.junit.Before;
import org.junit.Test;
import org.sa.rainbow.core.ports.IRainbowConnectionPortFactory;
import org.sa.rainbow.testing.implementation.BlackholeProbe;
import org.sa.rainbow.testing.prepare.RainbowMocker;
import org.sa.rainbow.testing.prepare.utils.ProbeTestingUtil;
import org.sa.rainbow.translator.probes.AbstractProbe;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.sa.rainbow.testing.prepare.RainbowMocker.mockConnectionPortFactory;
import static org.sa.rainbow.testing.prepare.utils.ProbeTestingUtil.stubPortFactoryForProbe;
import static org.sa.rainbow.testing.prepare.utils.ResourceUtil.extractResource;

public class BlackholeProbeTest {

    private File tempInput = extractResource("/blackhole/probe-input.txt");

    public BlackholeProbeTest() throws IOException {
    }

    @Before
    public void setUp() {
        RainbowMocker.injectRainbow();
        IRainbowConnectionPortFactory mockedPortFactory = mockConnectionPortFactory();
        stubPortFactoryForProbe(mockedPortFactory);
        RainbowMocker.injectPortFactory(mockedPortFactory);
    }

    @Test
    public void goodPath() {
        AbstractProbe probe = new BlackholeProbe("blackhole", 0L, new String[]{tempInput.toString()});
        probe.create();
        probe.activate();
        assertEquals("127.0.0.1, 1.0.0.1, 192.168.0.1", ProbeTestingUtil.waitForOutput());
    }

}
