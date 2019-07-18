package org.sa.rainbow.testing.prepare.utils;

import org.sa.rainbow.translator.probes.IProbeIdentifier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class MockingUtil {

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
}
