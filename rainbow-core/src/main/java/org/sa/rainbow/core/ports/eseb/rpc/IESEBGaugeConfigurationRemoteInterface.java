package org.sa.rainbow.core.ports.eseb.rpc;

import java.util.List;

import org.sa.rainbow.core.ports.IGaugeConfigurationPort;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

import edu.cmu.cs.able.eseb.rpc.ParametersTypeMapping;
import edu.cmu.cs.able.eseb.rpc.ReturnTypeMapping;

public interface IESEBGaugeConfigurationRemoteInterface extends IGaugeConfigurationPort {
    public static final int ID = 2;

    @ReturnTypeMapping ("bool")
    @Override
    public boolean reconfigureGauge ();

    @Override
    @ReturnTypeMapping ("bool")
    @ParametersTypeMapping ({ "list<typed_attribute_with_value>" })
    public boolean configureGauge (List<TypedAttributeWithValue> configParams);

}