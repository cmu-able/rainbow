package org.sa.rainbow.testing.prepare.utils;

import org.sa.rainbow.core.gauges.GaugeDescription;
import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IProbeLifecyclePort;
import org.sa.rainbow.core.ports.IProbeReportSubscriberPort;
import org.sa.rainbow.core.ports.IRainbowConnectionPortFactory;
import org.sa.rainbow.testing.prepare.stub.ports.LoggerGaugeLifecycleBusPort;
import org.sa.rainbow.testing.prepare.stub.ports.OperationCollectingModelUSBusPortStub;
import org.sa.rainbow.util.YamlUtil;

import java.io.File;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sa.rainbow.testing.prepare.utils.ResourceUtil.extractResource;

public class GaugeTestingUtil {
    private GaugeTestingUtil() {

    }

    private static OperationCollectingModelUSBusPortStub operationCollectingModelUSBusPortStub;

    /**
     * Tweak the mocked IRainbowConnectionPortFactory instance for testing gauges.
     *
     * @param mockedPortFactory mocked IRainbowConnectionPortFactory instance
     */
    public static void stubPortFactoryForGauge(IRainbowConnectionPortFactory mockedPortFactory) {
        try {
            operationCollectingModelUSBusPortStub = new OperationCollectingModelUSBusPortStub();
            LoggerGaugeLifecycleBusPort loggerGaugeLifecycleBusPort = new LoggerGaugeLifecycleBusPort();
            IProbeReportSubscriberPort probeReportSubscriberPort = mock(IProbeReportSubscriberPort.class);
            IProbeLifecyclePort probeLifecyclePort = mock(IProbeLifecyclePort.class);

            when(mockedPortFactory.createModelsManagerClientUSPort(any())).thenReturn(operationCollectingModelUSBusPortStub);
            when(mockedPortFactory.createGaugeSideLifecyclePort()).thenReturn(loggerGaugeLifecycleBusPort);
            when(mockedPortFactory.createProbeReportingPortSubscriber(any())).thenReturn(probeReportSubscriberPort);
            when(mockedPortFactory.createProbeManagementPort(any())).thenReturn(probeLifecyclePort);
        } catch (Exception ignored) {

        }
    }

    /**
     * Wait for the next operation from the gauge
     *
     * @return the next operation
     */
    public static IRainbowOperation waitForNextOperation() throws InterruptedException {
        return operationCollectingModelUSBusPortStub.takeOperation();
    }

    /**
     * Wait for the next operation from the gauge, with timeout
     *
     * @return the next operation, or null if timed-out
     */
    public static IRainbowOperation waitForNextOperation(long timeoutMilliseconds) throws InterruptedException {
        return operationCollectingModelUSBusPortStub.takeOperation(timeoutMilliseconds);
    }

    public static GaugeDescription loadGaugeDescriptionFromResource(String resource) throws IOException {
        File yamlFile = extractResource(resource);
        return YamlUtil.loadGaugeSpecs(yamlFile);
    }

    public static GaugeInstanceDescription loadGaugeInstanceDescriptionFromResource(String resource, int index) throws IOException {
        return loadGaugeDescriptionFromResource(resource).instDescList().get(index);
    }

    public static GaugeInstanceDescription loadGaugeInstanceDescriptionFromResource(String resource) throws IOException {
        return loadGaugeInstanceDescriptionFromResource(resource, 0);
    }
}
