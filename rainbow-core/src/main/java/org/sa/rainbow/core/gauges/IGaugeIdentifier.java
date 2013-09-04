package org.sa.rainbow.core.gauges;

import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.util.TypedAttribute;

public interface IGaugeIdentifier extends Identifiable {

    /**
     * Returns the Gauge's type-name description.
     * 
     * @return TypedAttribute the Gauge description as a type-name pair
     */
    public TypedAttribute gaugeDesc ();

    /**
     * Returns the Gauge's model type-name description.
     * 
     * @return TypedAttribute the Model description as a type-name pair
     */
    public TypedAttribute modelDesc ();
}
