package org.sa.rainbow.brass.model.mission;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.sa.rainbow.brass.model.instructions.SetLocalizationFidelityInstruction.LocalizationFidelity;
import org.sa.rainbow.brass.model.p2_cp3.clock.SetCurrentTimeCmd;
import org.sa.rainbow.brass.model.robot.SetBatteryChargeCmd;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

/**
 * Created by schmerl on 12/27/2016.
 */
public class MissionCommandFactory extends ModelCommandFactory<MissionState> {

    private static final String SET_ROBOT_LOCALIZATION_FIDELITY_CMD = "setRobotLocalizationFidelity";
	private static final String RECALIBRATE_CMD = "recalibrate";
	private static final String SET_CALIBRATION_ERROR_CMD = "setCalibrationError";
	private static final String SET_GROUND_PLAN_ERROR_CMD = "setGroundPlanError";
	private static final String SET_TARGET_WAYPOINT_CMD = "setTargetWaypoint";
	private static final String SET_DEADLINE_CMD = "setDeadlineCmd";
	private static final String SET_ROBOT_ACCURATE_CMD = "setRobotAccurate";
	private static final String SET_ROBOT_ON_TIME_CMD = "setRobotOnTime";
	private static final String SET_ROBOT_OBSTRUCTED_CMD = "setRobotObstructed";
	private static final String SET_CURRENT_LOCATION_CMD = "setCurrentLocation";

	@LoadOperation
	public static MissionStateLoadCmd loadCommand (ModelsManager mm, String modelName, InputStream
            stream, String source) {
        return new MissionStateLoadCmd (mm, modelName, stream, source);
    }

    public MissionCommandFactory (
            IModelInstance<MissionState> model) throws RainbowException {
        super (MissionStateModelInstance.class, model);
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

    @Operation(name=SET_CURRENT_LOCATION_CMD)
    public SetCurrentLocationCmd setCurrentLocationCmd (double x, double y, double w) {
        return new SetCurrentLocationCmd (SET_CURRENT_LOCATION_CMD,(MissionStateModelInstance) m_modelInstance, "", Double.toString (x),
                Double.toString (y), Double.toString (w));
    }

    @Operation(name=SET_ROBOT_OBSTRUCTED_CMD)
    public SetRobotObstructedCmd setRobotObstructedCmd (boolean robotObstructed) {
        return new SetRobotObstructedCmd (SET_ROBOT_OBSTRUCTED_CMD, (MissionStateModelInstance) m_modelInstance, "",
                Boolean.toString (robotObstructed));
    }

    @Operation(name=SET_ROBOT_ON_TIME_CMD)
    public SetRobotOnTimeCmd setRobotOnTimeCmd (boolean robotOnTime) {
        return new SetRobotOnTimeCmd (SET_ROBOT_ON_TIME_CMD, (MissionStateModelInstance) m_modelInstance, "",
                Boolean.toString (robotOnTime));
    }

    @Operation(name=SET_ROBOT_ACCURATE_CMD)
    public SetRobotAccurateCmd setRobotAccurateCmd (boolean robotAccurate) {
        return new SetRobotAccurateCmd (SET_ROBOT_ACCURATE_CMD, (MissionStateModelInstance) m_modelInstance, "",
                Boolean.toString (robotAccurate));
    }

    @Operation(name=SET_DEADLINE_CMD)
    public SetDeadlineCmd setDeadlineCmd (long d) {
        return new SetDeadlineCmd (SET_DEADLINE_CMD, (MissionStateModelInstance) m_modelInstance, "",
                Double.toString (d));
    }

    @Operation(name=SET_TARGET_WAYPOINT_CMD)
    public SetTargetWaypointCmd setTargetWaypointCmd (String t) {
        return new SetTargetWaypointCmd (SET_TARGET_WAYPOINT_CMD, (MissionStateModelInstance )m_modelInstance, "", t);
    }

    @Operation(name=SET_GROUND_PLAN_ERROR_CMD)
    public SetGroundPlaneErrorCmd setGroundPlaneErrorCmd (double t, double r) {
        return new SetGroundPlaneErrorCmd (SET_GROUND_PLAN_ERROR_CMD, (MissionStateModelInstance )m_modelInstance, "", Double.toString (t),
                Double.toString (r));
    }

    @Operation(name=SET_CALIBRATION_ERROR_CMD)
    public SetCalibrationErrorCmd setCalibrationError (double r,
            double r_scale,
            double t,
            double t_scale,
            double rv,
            double tv) {
        return new SetCalibrationErrorCmd (SET_CALIBRATION_ERROR_CMD, (MissionStateModelInstance )m_modelInstance, "", Double.toString (r),
                Double.toString (r_scale), Double.toString (t), Double.toString (t_scale), Double.toString (rv),
                Double.toString (tv));
    }

    @Operation(name=RECALIBRATE_CMD)
    public RecalibrateCmd recalibrate (boolean bad) {
        return new RecalibrateCmd (RECALIBRATE_CMD, (MissionStateModelInstance )m_modelInstance, "", Boolean.toString (bad));
    }

    @Operation(name=SET_ROBOT_LOCALIZATION_FIDELITY_CMD)
    public SetRobotLocalizationFidelityCmd setRobotLocalizationFidelityCmd (LocalizationFidelity fidelity) {
        return new SetRobotLocalizationFidelityCmd(SET_ROBOT_LOCALIZATION_FIDELITY_CMD, (MissionStateModelInstance) m_modelInstance, "", fidelity.toString());
    }
}
