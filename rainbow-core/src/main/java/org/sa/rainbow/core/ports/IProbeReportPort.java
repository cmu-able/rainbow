package org.sa.rainbow.core.ports;

import org.sa.rainbow.translator.probes.IProbe;

public interface IProbeReportPort {
    public abstract void reportData (IProbe probe, String data);
}
