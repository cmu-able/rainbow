package org.sa.rainbow.gauges;

import java.util.Collection;

import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.models.commands.IRainbowModelCommandRepresentation;

public interface IGaugeState {
    Collection<TypedAttributeWithValue> getSetupParams ();
    Collection<TypedAttributeWithValue> getConfigParams ();
    Collection<IRainbowModelCommandRepresentation> getGaugeReports ();
}
