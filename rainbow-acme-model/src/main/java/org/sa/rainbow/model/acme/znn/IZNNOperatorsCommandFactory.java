package org.sa.rainbow.model.acme.znn;

import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.sa.rainbow.model.acme.AcmeType;
import org.sa.rainbow.models.commands.IRainbowModelCommand;

public interface IZNNOperatorsCommandFactory {

    public abstract IRainbowModelCommand<IAcmeProperty, IAcmeSystem>
    setResponseTimeCmd (@AcmeType ("ClientT") IAcmeComponent client, float rt);

    public abstract IRainbowModelCommand<IAcmeProperty, IAcmeSystem>
    setLoadCmd (@AcmeType ("ServerT | ProxyT") IAcmeComponent server, float load);

    public abstract IRainbowModelCommand<IAcmeComponent, IAcmeSystem>
    connectNewServer (@AcmeType ("ProxyT") IAcmeComponent proxy, String name);

}
