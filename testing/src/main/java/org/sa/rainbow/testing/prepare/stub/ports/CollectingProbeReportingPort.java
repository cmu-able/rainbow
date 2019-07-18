package org.sa.rainbow.testing.prepare.stub.ports;

import org.sa.rainbow.core.ports.IProbeReportPort;
import org.sa.rainbow.translator.probes.IProbeIdentifier;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * This is the IProbeReportPort that stores all reported data from a probe.
 */
public class CollectingProbeReportingPort implements IProbeReportPort {
    private BlockingDeque<String> reportedData = new LinkedBlockingDeque<>();

    public String takeOutput() {
        try {
            return reportedData.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reportData(IProbeIdentifier probe, String data) {
        try {
            reportedData.put(data);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Should be called when this port is no longer required. Implementors should dispose of all resources.
     */
    @Override
    public void dispose() {

    }
}
