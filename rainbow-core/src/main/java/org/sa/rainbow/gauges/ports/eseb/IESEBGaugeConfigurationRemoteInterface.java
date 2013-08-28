package org.sa.rainbow.gauges.ports.eseb;

import java.util.List;

import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.gauges.IGaugeConfigurationInterface;

import edu.cmu.cs.able.eseb.rpc.ParametersTypeMapping;
import edu.cmu.cs.able.eseb.rpc.ReturnTypeMapping;

public interface IESEBGaugeConfigurationRemoteInterface extends IGaugeConfigurationInterface {
    @ReturnTypeMapping ("bool")
    @Override
    public boolean reconfigureGauge ();

    @Override
    @ReturnTypeMapping ("bool")
    @ParametersTypeMapping ({ "list<typed_attribute_with_value>" })
    public boolean configureGauge (List<TypedAttributeWithValue> configParams);

}
