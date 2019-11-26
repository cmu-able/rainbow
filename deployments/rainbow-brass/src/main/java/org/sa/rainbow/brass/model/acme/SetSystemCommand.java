package org.sa.rainbow.brass.model.acme;

import java.util.LinkedList;
import java.util.List;

import org.acmestudio.acme.element.IAcmeElement;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.model.IAcmeCommandFactory;
import org.acmestudio.acme.model.IAcmeModel;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.standalone.resource.StandaloneLanguagePackHelper;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

public class SetSystemCommand extends RosAcmeModelCommand<IAcmeSystem> {

    private IAcmeSystem m_system;
    private String      m_systemAsString;

    public SetSystemCommand (String commandName, AcmeModelInstance model, String target, String newSystem) {
        super (commandName, model, target, newSystem);
        m_systemAsString = newSystem;
    }

    @Override
    public IAcmeSystem getResult () throws IllegalStateException {
        return m_system;
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        try {
            List<IAcmeCommand<?>> commands = new LinkedList<> ();
            IAcmeElement system = StandaloneLanguagePackHelper.defaultLanguageHelper ()
                    .elementFromString (m_systemAsString, null, getModel ().getContext ());
            IAcmeModel acmeModel = getModel ().getContext ().getModel ();
            IAcmeCommandFactory cf = acmeModel.getCommandFactory ();
            commands.add (cf.systemDeleteCommand (getModel ()));
            commands.add (cf.copyElementCommand (acmeModel, system));
            return commands;
        }
        catch (Exception e) {
            throw new RainbowModelException (e);
        }
    }


}
