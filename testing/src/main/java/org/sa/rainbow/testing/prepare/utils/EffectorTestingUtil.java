package org.sa.rainbow.testing.prepare.utils;

import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.eseb.RainbowESEBMessage;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EffectorTestingUtil {
    private EffectorTestingUtil() {

    }

    /**
     * Creates a mocked announce port that can create RainbowESEBMessage.
     *
     * @return mocked IModelChangeBusPort
     */
    public static IModelChangeBusPort mockAnnouncePort() {
        IModelChangeBusPort announcePort = mock(IModelChangeBusPort.class);
        when(announcePort.createMessage()).thenAnswer(invocation -> new RainbowESEBMessage());
        return announcePort;
    }
}