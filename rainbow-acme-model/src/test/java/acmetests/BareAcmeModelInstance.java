package acmetests;

import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.model.acme.AcmeModelCommandFactory;
import org.sa.rainbow.model.acme.AcmeModelInstance;

public class BareAcmeModelInstance extends AcmeModelInstance {

    public BareAcmeModelInstance (IAcmeSystem system) {
        super (system, "");
    }

    @Override
    public AcmeModelCommandFactory getCommandFactory () {
        return null;
    }

    @Override
    protected AcmeModelInstance generateInstance (IAcmeSystem sys) {
        return new BareAcmeModelInstance (sys);
    }

}
