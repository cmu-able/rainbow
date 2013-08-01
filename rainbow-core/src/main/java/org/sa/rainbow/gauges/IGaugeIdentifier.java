package org.sa.rainbow.gauges;

import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.util.TypedAttribute;

public interface IGaugeIdentifier extends Identifiable {

    /**
     * Returns the Gauge's type-name description.
     * 
     * @return TypeNamePair the Gauge description as a type-name pair
     */
    public TypedAttribute gaugeDesc ();

    /**
     * Returns the Gauge's model type-name description.
     * 
     * @return TypeNamePair the Model description as a type-name pair
     */
    public TypedAttribute modelDesc ();
}
