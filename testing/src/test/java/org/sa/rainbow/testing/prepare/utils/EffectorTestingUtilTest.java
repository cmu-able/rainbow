package org.sa.rainbow.testing.prepare.utils;

import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.ports.IModelChangeBusPort;

import static org.hamcrest.core.IsSame.theInstance;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class EffectorTestingUtilTest {

    @Test
    public void mockAnnouncePort() {
        IModelChangeBusPort mockPort = EffectorTestingUtil.mockAnnouncePort();
        IRainbowMessage message = mockPort.createMessage();
        assertNotNull(message);
    }
}