package org.sa.rainbow.gauges;

import java.util.List;

import org.sa.rainbow.core.util.TypedAttributeWithValue;

public interface IRainbowGaugeLifecycleBusPort {

    public void reportCreated (IGaugeIdentifier gauge);

    public void reportDeleted (IGaugeIdentifier gauge);

    public void reportConfigured (IGaugeIdentifier gauge, List<TypedAttributeWithValue> configParams);

    public void sendBeacon (IGaugeIdentifier gauge);
}
