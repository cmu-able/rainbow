package org.sa.rainbow.translator.znn.gauges;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodCall;
import org.powermock.reflect.Whitebox;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.gauges.AbstractGauge;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.util.Beacon;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

public class TestingUtils {

    private final static Constructor<AbstractRainbowRunnable> runnableConstructor;
    private final static Method constructGaugeMethod;

    static {
        try {
            constructGaugeMethod = TestingUtils.class.getDeclaredMethod("constructGauge", AbstractGauge.class, String.class, String.class, long.class, TypedAttribute.class, TypedAttribute.class, List.class, Map.class);
            runnableConstructor = AbstractRainbowRunnable.class.getConstructor(String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static void rewriteGaugeConstructor() {
        ByteBuddyAgent.install();
        (new ByteBuddy()).redefine(AbstractGauge.class)
                .constructor(takesArguments(7))
                .intercept(
                        MethodCall.invoke(runnableConstructor).withArgument(0)
                                .andThen(MethodCall.invoke(constructGaugeMethod).withThis().withAllArguments())
                )
                .make()
                .load(TestingUtils.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
    }

    public static void constructGauge(AbstractGauge gauge, String threadName, String id, long beaconPeriod, TypedAttribute gaugeDesc,
                                      TypedAttribute modelDesc, List<TypedAttributeWithValue> setupParams,
                                      Map<String, IRainbowOperation> mappings) throws Exception {

        Whitebox.setInternalState(gauge, "m_gaugeBeacon", new Beacon(beaconPeriod));
        Whitebox.setInternalState(gauge, "m_gaugeDesc", gaugeDesc);
        Whitebox.setInternalState(gauge, "m_modelDesc", modelDesc);

        Map<String, TypedAttributeWithValue> mSetupParams = new HashMap<>();
        Map<String, IRainbowOperation> mCommands = new HashMap<>();

        Whitebox.setInternalState(gauge, "m_setupParams", mSetupParams);
        Whitebox.setInternalState(gauge, "m_configParams", new HashMap());
        Whitebox.setInternalState(gauge, "m_lastCommands", mCommands);

        Whitebox.setInternalState(gauge, "m_commands", new HashMap());

        for (TypedAttributeWithValue param : setupParams) {
            mSetupParams.put(param.getName(), param);
        }

        // Need to keep the list of commands, and also perhaps the commands by value (if they exist)
        assert mappings != null;
        for (Map.Entry<String, IRainbowOperation> cmd : mappings.entrySet()) {
            mCommands.put(cmd.getKey(), cmd.getValue());
            for (String param : cmd.getValue().getParameters()) {
                mCommands.put((String) Whitebox.invokeMethod(gauge, "pullOutParam", param), cmd.getValue());
            }
        }
    }
}
