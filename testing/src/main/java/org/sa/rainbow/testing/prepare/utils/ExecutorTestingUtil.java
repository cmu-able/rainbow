package org.sa.rainbow.testing.prepare.utils;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.eseb.RainbowESEBMessage;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExecutorTestingUtil {
    /**
     * Creates a mocked announce port that can create RainbowESEBMessage.
     *
     * @return mocked IModelChangeBusPort
     */
    public static IModelChangeBusPort mockAnnouncePort() {
        IModelChangeBusPort announcePort = mock(IModelChangeBusPort.class);
        when(announcePort.createMessage()).thenAnswer(new Answer<IRainbowMessage>() {
            /**
             * @param invocation the invocation on the mock.
             * @return the value to be returned
             */
            @Override
            public IRainbowMessage answer(InvocationOnMock invocation) {
                return new RainbowESEBMessage();
            }
        });
        return announcePort;
    }
}

