package acmetests;

import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.models.commands.ModelCommandFactory;

public class BareAcmeModelInstance extends AcmeModelInstance {

    public BareAcmeModelInstance (IAcmeSystem system) {
        super (system);
    }

    @Override
    public ModelCommandFactory<IAcmeSystem> getCommandFactory () {
        return null;
    }

    @Override
    protected AcmeModelInstance generateInstance (IAcmeSystem sys) {
        return new BareAcmeModelInstance (sys);
    }

}
