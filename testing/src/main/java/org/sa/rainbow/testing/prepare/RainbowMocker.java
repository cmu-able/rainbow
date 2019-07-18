package org.sa.rainbow.testing.prepare;

import org.sa.rainbow.core.IRainbowEnvironment;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.ports.IRainbowConnectionPortFactory;
import org.sa.rainbow.core.ports.RainbowPortFactory;

import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;

public class RainbowMocker {
    /**
     * Replace the singleton instance of RainbowPortFactory with the given mocked one.
     *
     * @param portFactory the mocked IRainbowConnectionPortFactory instance
     */
    public static void injectPortFactory(IRainbowConnectionPortFactory portFactory) {
        Class<RainbowPortFactory> rainbowPortFactoryClass = RainbowPortFactory.class;
        try {
            Field instanceField = rainbowPortFactoryClass.getDeclaredField("m_instance");
            instanceField.setAccessible(true);
            instanceField.set(null, portFactory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a mocked instance of IRainbowConnectionPortFactory.
     *
     * @return mocked instance of IRainbowConnectionPortFactory
     */
    public static IRainbowConnectionPortFactory mockConnectionPortFactory() {
        return mock(IRainbowConnectionPortFactory.class);
    }

    /**
     * Replace the singleton instance of Rainbow with a mocked one.
     */
    public static void injectRainbow() {
        Class<Rainbow> rainbowClass = Rainbow.class;
        try {
            Field instanceField = rainbowClass.getDeclaredField("_instance");
            instanceField.setAccessible(true);
            instanceField.set(null, mock(IRainbowEnvironment.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
