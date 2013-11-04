package org.sa.rainbow.model.acme.znn;

import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.sa.rainbow.core.models.commands.IRainbowModelOperation;
import org.sa.rainbow.model.acme.AcmeType;

public interface IZNNOperatorsCommandFactory {

    public abstract IRainbowModelOperation<IAcmeProperty, IAcmeSystem>
    setResponseTimeCmd (@AcmeType ("ClientT") IAcmeComponent client, float rt);

    public abstract IRainbowModelOperation<IAcmeProperty, IAcmeSystem>
    setLoadCmd (@AcmeType ("ServerT | ProxyT") IAcmeComponent server, float load);

    public abstract IRainbowModelOperation<IAcmeComponent, IAcmeSystem>
    connectNewServer (@AcmeType ("ProxyT") IAcmeComponent proxy, String name);

}
