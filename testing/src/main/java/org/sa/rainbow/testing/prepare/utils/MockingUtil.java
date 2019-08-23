package org.sa.rainbow.testing.prepare.utils;

import org.apache.log4j.Logger;
import org.mockito.stubbing.Answer;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.translator.probes.IProbeIdentifier;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 *
 */
public class MockingUtil {
    private MockingUtil() {

    }

    public static IProbeIdentifier mockProbeIdentifier(String name, String location) {
        return mockProbeIdentifier(name, location, "JAVA");
    }

    /**
     * Create a mocked IProbeIdentifier instance.
     *
     * @param name     name of mocked probe
     * @param location location of mocked probe
     * @param type     type of mocked type
     * @return mocked IProbeIdentifier instance
     */
    public static IProbeIdentifier mockProbeIdentifier(String name, String location, String type) {
        IProbeIdentifier probeIdentifier = mock(IProbeIdentifier.class);
        when(probeIdentifier.name()).thenReturn(name);
        when(probeIdentifier.location()).thenReturn(location);
        when(probeIdentifier.type()).thenReturn(type);
        return probeIdentifier;
    }

    /**
     * Creates a mocked IRainbowReportingPort instance.
     *
     * @return mocked IRainbowReportingPort instance
     */
    public static IRainbowReportingPort mockReportPort() {
        IRainbowReportingPort mockedReportingPort = mock(IRainbowReportingPort.class);
        final Logger logger = Logger.getLogger(IRainbowReportingPort.class);
        final Answer infoAnswer = invocation -> {
            logger.info(invocation.getArgument(1));
            return null;
        };

        doAnswer(infoAnswer).when(mockedReportingPort).info(any(), any());
        doAnswer(infoAnswer).when(mockedReportingPort).info(any(), any(), any(Logger.class));

        doAnswer(infoAnswer).when(mockedReportingPort).trace(any(), any());

        doThrow(NotImplementedException.class).when(mockedReportingPort).fatal(any(), any());
        doThrow(NotImplementedException.class).when(mockedReportingPort).fatal(any(), any(), any(Throwable.class));
        doThrow(NotImplementedException.class).when(mockedReportingPort).fatal(any(), any(), any(Logger.class));
        doThrow(NotImplementedException.class).when(mockedReportingPort).fatal(any(), any(), any(Throwable.class), any(Logger.class));

        doThrow(NotImplementedException.class).when(mockedReportingPort).error(any(), any());
        doThrow(NotImplementedException.class).when(mockedReportingPort).error(any(), any(), any(Throwable.class));
        doThrow(NotImplementedException.class).when(mockedReportingPort).error(any(), any(), any(Logger.class));
        doThrow(NotImplementedException.class).when(mockedReportingPort).error(any(), any(), any(Throwable.class), any(Logger.class));

        doNothing().when(mockedReportingPort).warn(any(), any());
        doNothing().when(mockedReportingPort).warn(any(), any(), any(Throwable.class));
        doNothing().when(mockedReportingPort).warn(any(), any(), any(Logger.class));
        doNothing().when(mockedReportingPort).warn(any(), any(), any(Throwable.class), any(Logger.class));

        return mockedReportingPort;
    }
}
