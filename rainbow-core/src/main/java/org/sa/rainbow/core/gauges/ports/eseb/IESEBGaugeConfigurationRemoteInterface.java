package org.sa.rainbow.core.gauges.ports.eseb;

import java.util.List;

import org.sa.rainbow.core.gauges.IGaugeConfigurationInterface;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

import edu.cmu.cs.able.eseb.rpc.ParametersTypeMapping;
import edu.cmu.cs.able.eseb.rpc.ReturnTypeMapping;

public interface IESEBGaugeConfigurationRemoteInterface extends IGaugeConfigurationInterface {
    public static final int ID = 2;

    @ReturnTypeMapping ("bool")
    @Override
    public boolean reconfigureGauge ();

    @Override
    @ReturnTypeMapping ("bool")
    @ParametersTypeMapping ({ "list<typed_attribute_with_value>" })
    public boolean configureGauge (List<TypedAttributeWithValue> configParams);

}
