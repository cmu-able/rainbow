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
        m_commandMap.put ("setRobotOnTime".toLowerCase (), SetRobotOnTimeCmd.class);
        m_commandMap.put ("setBatteryCharge".toLowerCase (), SetBatteryChargeCmd.class);
        m_commandMap.put ("setDeadlineCmd".toLowerCase (), SetDeadlineCmd.class);
        m_commandMap.put ("setTargetWaypoint".toLowerCase (), SetTargetWaypointCmd.class);
        m_commandMap.put ("setCurrentTime".toLowerCase (), SetCurrentTimeCmd.class);
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

    public SetCurrentLocationCmd setCurrentLocationCmd (double x, double y, double w) {
        return new SetCurrentLocationCmd ((MissionStateModelInstance) m_modelInstance, "", Double.toString (x),
                Double.toString (y), Double.toString (w));
    }

    public SetRobotObstructedCmd setRobotObstructedCmd (boolean robotObstructed) {
        return new SetRobotObstructedCmd ((MissionStateModelInstance) m_modelInstance, "",
                Boolean.toString (robotObstructed));
    }

    public SetRobotOnTimeCmd setRobotOnTimeCmd (boolean robotOnTime) {
        return new SetRobotOnTimeCmd ((MissionStateModelInstance) m_modelInstance, "",
                Boolean.toString (robotOnTime));
    }

    public SetBatteryChargeCmd setBatteryChargeCmd (double charge) {
        return new SetBatteryChargeCmd ((MissionStateModelInstance) m_modelInstance, "", Double.toString (charge));
    }

    public SetDeadlineCmd setDeadlineCmd (long d) {
        return new SetDeadlineCmd ((MissionStateModelInstance) m_modelInstance, "",
                Double.toString (d));
    }

    public SetTargetWaypointCmd setTargetWaypointCmd (String t) {
        return new SetTargetWaypointCmd ((MissionStateModelInstance )m_modelInstance, "", t);
    }

    public SetCurrentTimeCmd setCurrentTime (double t) {
        return new SetCurrentTimeCmd ((MissionStateModelInstance )m_modelInstance, "", Double.toString (t));
    }
}
