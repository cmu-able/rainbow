package org.sa.rainbow.translator.probes.ports;

import org.sa.rainbow.translator.probes.IProbe;

public interface IProbeReportPort {
    public abstract void reportData (IProbe probe, String data);
}
