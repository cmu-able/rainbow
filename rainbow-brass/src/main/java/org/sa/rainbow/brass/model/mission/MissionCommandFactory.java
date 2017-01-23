package org.sa.rainbow.brass.model.mission;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

/**
 * Created by schmerl on 12/27/2016.
 */
public class MissionCommandFactory extends ModelCommandFactory<MissionState> {

    public static MissionStateLoadCmd loadCommand (ModelsManager mm, String modelName, InputStream
            stream, String source) {
        return new MissionStateLoadCmd (mm, modelName, stream, source);
    }

    public MissionCommandFactory (
            IModelInstance<MissionState> model) {
        super (MissionStateModelInstance.class, model);
    }

    @Override
    protected void fillInCommandMap () {
        m_commandMap.put ("setCurrentLocation".toLowerCase (), SetCurrentLocationCmd.class);
        m_commandMap.put ("setRobotObstructed".toLowerCase (), SetRobotObstructedCmd.class);
    }

    @Override
    public SaveMissionCmd saveCommand (String location) throws RainbowModelException {
        try (FileOutputStream os = new FileOutputStream (location)) {
            return new SaveMissionCmd(null, location, os, m_modelInstance.getOriginalSource ());
        }
        catch (IOException e) {
            return null;
        }
    }

    public SetCurrentLocationCmd setCurrentLocationCmd (String x, String y, String w) {
        return new SetCurrentLocationCmd ((MissionStateModelInstance) m_modelInstance, "", x, y, w);
    }
    
    public SetRobotObstructedCmd setRobotObstructedCmd (String robotObstructed) {
    	return new SetRobotObstructedCmd ((MissionStateModelInstance) m_modelInstance, "", robotObstructed);
    }
}
