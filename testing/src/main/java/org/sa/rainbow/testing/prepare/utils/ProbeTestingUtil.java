package org.sa.rainbow.testing.prepare.utils;

import org.sa.rainbow.core.ports.IProbeLifecyclePort;
import org.sa.rainbow.core.ports.IRainbowConnectionPortFactory;
import org.sa.rainbow.testing.prepare.stub.ports.CollectingProbeReportingPort;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProbeTestingUtil {
    private ProbeTestingUtil() {

    }

    private static CollectingProbeReportingPort probeReportingPort;

    /**
     * Tweak the mocked IRainbowConnectionPortFactory instance for testing probes.
     *
     * @param mockedPortFactory mocked IRainbowConnectionPortFactory instance
     */
    public static void stubPortFactoryForProbe(IRainbowConnectionPortFactory mockedPortFactory) {
        try {
            probeReportingPort = new CollectingProbeReportingPort();
            IProbeLifecyclePort probeLifecyclePort = mock(IProbeLifecyclePort.class);
            when(mockedPortFactory.createProbeReportingPortSender(any())).thenReturn(probeReportingPort);
            when(mockedPortFactory.createProbeManagementPort(any())).thenReturn(probeLifecyclePort);
        } catch (Exception ignored) {
        }
    }

    /**
     * Wait for the next output from the probe.
     *
     * @return the next output
     */
    public static String waitForOutput() throws InterruptedException {
        return probeReportingPort.takeOutput();
    }

    /**
     * Wait for the next output from the probe, with timeout.
     *
     * @return the next output, or null if timed-out
     */
    public static String waitForOutput(long timeoutMilliseconds) throws InterruptedException {
        return probeReportingPort.takeOutput(timeoutMilliseconds);
    }
}
