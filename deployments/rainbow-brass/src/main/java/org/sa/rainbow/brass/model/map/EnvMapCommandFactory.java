package org.sa.rainbow.brass.model.map;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

public class EnvMapCommandFactory extends ModelCommandFactory<EnvMap> {


    public EnvMapCommandFactory (IModelInstance<EnvMap> model) {
        super (EnvMapModelInstance.class, model);
    }

    public static EnvMapLoadCmd loadCommand (ModelsManager mm, String modelName, InputStream stream, String source){
        return new EnvMapLoadCmd (mm, modelName, stream, source);
    }

    @Override
    public SaveEnvMapCmd saveCommand (String location) throws RainbowModelException {
        try (FileOutputStream os = new FileOutputStream (location)) {
            return new SaveEnvMapCmd(null, location, os, m_modelInstance.getOriginalSource());
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    protected void fillInCommandMap () {
        m_commandMap.put("insertNode".toLowerCase (), InsertNodeCmd.class);
    }

    public InsertNodeCmd insertNodeCmd (String n, String na, String nb, String x, String y, String blocked) {
        return new InsertNodeCmd ((EnvMapModelInstance )m_modelInstance, "", n, na, nb, x, y, blocked);
    }
}
