package org.sa.rainbow.testing.prepare;

import org.sa.rainbow.core.IRainbowEnvironment;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.ports.IRainbowConnectionPortFactory;
import org.sa.rainbow.core.ports.RainbowPortFactory;

import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RainbowMocker {
    private static final String RAINBOW_INSTANCE_FIELD_NAME = "_instance";
    private static final String PORT_FACTORY_INSTANCE_FIELD_NAME = "m_instance";

    private RainbowMocker() {

    }

    private static void injectSingletonInstance(Class<?> clazz, String fieldName, Object instance) {
        try {
            Field instanceField = clazz.getDeclaredField(fieldName);
            instanceField.setAccessible(true);
            instanceField.set(null, instance);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Replace the singleton instance of RainbowPortFactory with the given mocked one.
     *
     * @param portFactory the mocked IRainbowConnectionPortFactory instance
     */
    public static void injectPortFactory(IRainbowConnectionPortFactory portFactory) {
        Class<RainbowPortFactory> rainbowPortFactoryClass = RainbowPortFactory.class;
        injectSingletonInstance(rainbowPortFactoryClass, PORT_FACTORY_INSTANCE_FIELD_NAME, portFactory);
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
     * Creates a mocked instance of IRainbowEnvironment
     *
     * @return mocked instance of IRainbowEnvironment
     */
    private static IRainbowEnvironment mockRainbowEnvironment() {
        IRainbowEnvironment rainbowEnvironment = mock(IRainbowEnvironment.class);
        when(rainbowEnvironment.exitValue()).thenReturn(0);
        when(rainbowEnvironment.getRainbowMaster()).thenReturn(null);
        when(rainbowEnvironment.isMaster()).thenReturn(false);
        when(rainbowEnvironment.shouldTerminate()).thenReturn(false);
        return rainbowEnvironment;
    }

    /**
     * Replace the singleton instance of Rainbow with a mocked one.
     */
    public static void injectRainbow() {
        Class<Rainbow> rainbowClass = Rainbow.class;
        injectSingletonInstance(rainbowClass, RAINBOW_INSTANCE_FIELD_NAME, mockRainbowEnvironment());
    }
}
