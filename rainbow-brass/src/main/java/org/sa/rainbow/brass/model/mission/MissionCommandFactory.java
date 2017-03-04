package org.sa.rainbow.brass.model.mission;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.sa.rainbow.brass.model.instructions.SetLocalizationFidelityInstruction.LocalizationFidelity;
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
        m_commandMap.put ("setRobotAccurate".toLowerCase (), SetRobotAccurateCmd.class);
        m_commandMap.put ("setBatteryCharge".toLowerCase (), SetBatteryChargeCmd.class);
        m_commandMap.put ("setDeadlineCmd".toLowerCase (), SetDeadlineCmd.class);
        m_commandMap.put ("setTargetWaypoint".toLowerCase (), SetTargetWaypointCmd.class);
        m_commandMap.put ("setCurrentTime".toLowerCase (), SetCurrentTimeCmd.class);
        m_commandMap.put ("setGroundPlanError".toLowerCase (), SetGroundPlaneErrorCmd.class);
        m_commandMap.put ("setCalibrationError".toLowerCase (), SetCalibrationErrorCmd.class);
        m_commandMap.put ("recalibrate", RecalibrateCmd.class);
        // TODO: This may be a hack
        m_commandMap.put ("setRobotLocalizationFidelity".toLowerCase (), SetRobotLocalizationFidelityCmd.class);
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

    public SetRobotAccurateCmd setRobotAccurateCmd (boolean robotAccurate) {
        return new SetRobotAccurateCmd ((MissionStateModelInstance) m_modelInstance, "",
                Boolean.toString (robotAccurate));
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

    public SetCurrentTimeCmd setCurrentTimeCmd (double t) {
        return new SetCurrentTimeCmd ((MissionStateModelInstance )m_modelInstance, "", Double.toString (t));
    }

    public SetGroundPlaneErrorCmd setGroundPlaneErrorCmd (double t, double r) {
        return new SetGroundPlaneErrorCmd ((MissionStateModelInstance )m_modelInstance, "", Double.toString (t),
                Double.toString (r));
    }

    public SetCalibrationErrorCmd setCalibrationError (double r, double r_scale, double t, double t_scale, double v) {
        return new SetCalibrationErrorCmd ((MissionStateModelInstance )m_modelInstance, "", Double.toString (r),
                Double.toString (r_scale), Double.toString (t), Double.toString (t_scale), Double.toString (v));
    }

    public RecalibrateCmd recalibrate (boolean bad) {
        return new RecalibrateCmd ((MissionStateModelInstance )m_modelInstance, "", Boolean.toString (bad));
    }
    
    public SetRobotLocalizationFidelityCmd setRobotLocalizationFidelityCmd (LocalizationFidelity fidelity) {
    	return new SetRobotLocalizationFidelityCmd((MissionStateModelInstance) m_modelInstance, "", fidelity.toString());
    }
}
