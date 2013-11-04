package org.sa.rainbow.model.acme;

import java.util.LinkedList;
import java.util.List;

import org.acmestudio.acme.core.extension.IAcmeElementExtension;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmeUserDataCommand;
import org.sa.rainbow.core.error.RainbowModelException;

public class AcmeTypecheckSetCmd extends AcmeModelOperation<IAcmeElementExtension> {

    private Boolean m_typechecks;

    public AcmeTypecheckSetCmd (String commandName, AcmeModelInstance model, String target, String typechecks) {
        super (commandName, model, target, typechecks);
        m_typechecks = Boolean.valueOf (typechecks);
    }

    @Override
    public IAcmeElementExtension getResult () throws IllegalStateException {
        return ((IAcmeUserDataCommand )m_command).getValue ();
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        m_command = getModel ().getCommandFactory ().setElementUserData (getModel (), "TYPECHECKS",
                new RainbowModelTypecheckExtension (m_typechecks));
        List<IAcmeCommand<?>> cmds = new LinkedList<> ();
        cmds.add (m_command);
        return cmds;
    }


    @Override
    protected boolean checkModelValidForCommand (IAcmeSystem model) {
        return true;
    }

}
