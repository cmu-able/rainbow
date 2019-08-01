package org.sa.rainbow.testing.prepare.utils;

import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IRainbowConnectionPortFactory;
import org.sa.rainbow.testing.prepare.stub.ports.LoggerGaugeLifecycleBusPort;
import org.sa.rainbow.testing.prepare.stub.ports.OperationCollectingModelUSBusPortStub;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class GaugeTestingUtil {
    private static OperationCollectingModelUSBusPortStub operationCollectingModelUSBusPortStub;

    /**
     * Tweak the mocked IRainbowConnectionPortFactory instance for testing gauges.
     *
     * @param mockedPortFactory mocked IRainbowConnectionPortFactory instance
     */
    public static void stubPortFactoryForGauge(IRainbowConnectionPortFactory mockedPortFactory) {
        try {
            operationCollectingModelUSBusPortStub = new OperationCollectingModelUSBusPortStub();
            when(mockedPortFactory.createModelsManagerClientUSPort(any())).thenReturn(operationCollectingModelUSBusPortStub);
            when(mockedPortFactory.createGaugeSideLifecyclePort()).thenReturn(new LoggerGaugeLifecycleBusPort());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Wait for the next operation from the gauge
     *
     * @return the next operation
     */
    public static IRainbowOperation waitForNextOperation() {
        return operationCollectingModelUSBusPortStub.takeOperation();
    }

    /**
     * Wait for the next operation from the gauge, with timeout
     *
     * @return the next operation, or null if timed-out
     */
    public static IRainbowOperation waitForNextOperation(long timeoutMilliseconds) {
        return operationCollectingModelUSBusPortStub.takeOperation(timeoutMilliseconds);
    }
}
