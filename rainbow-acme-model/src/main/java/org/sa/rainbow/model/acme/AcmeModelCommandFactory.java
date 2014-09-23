package org.sa.rainbow.model.acme;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

public abstract class AcmeModelCommandFactory extends ModelCommandFactory<IAcmeSystem> {

    public AcmeModelCommandFactory (AcmeModelInstance model) {
        super (AcmeModelInstance.class, model);
    }

    @Override
    protected void fillInCommandMap () {
        m_commandMap.put ("setTypecheckResult".toLowerCase (), AcmeTypecheckSetCmd.class);
    }

    public AcmeTypecheckSetCmd setTypecheckResultCmd (boolean typechecks) {
        return new AcmeTypecheckSetCmd ("setTypecheckResult", (AcmeModelInstance )m_modelInstance, "self",
                Boolean.toString (typechecks));
    }



    @Override
    public AcmeSaveModelCommand saveCommand (String location) throws RainbowModelException {
        try {
            FileOutputStream stream = new FileOutputStream (location);
            AcmeSaveModelCommand cmd = new AcmeSaveModelCommand (m_modelInstance.getModelName (),
                    (AcmeModelInstance )m_modelInstance, stream);
            return cmd;
        }
        catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

}
