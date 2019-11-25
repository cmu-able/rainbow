package org.sa.rainbow.brass.model.p2_cp3.mission;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.sa.rainbow.brass.model.p2_cp3.mission.MissionState.UtilityPreference;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowStateModelInstance;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

/**
 * Created by schmerl on 12/27/2016.
 */
public class MissionCommandFactory extends ModelCommandFactory<MissionState> {

    private static final String SET_UTILITY_PREFERENCE_CMD = "setUtilityPreference";
	private static final String SET_RECONFIGURING_CMD = "setReconfiguring";
	private static final String SET_TARGET_WAYPOINT_CMD = "setTargetWaypoint";
	private static final String SET_DEADLINE_CMD = "setDeadline";
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
        return new SetCurrentLocationCmd (SET_CURRENT_LOCATION_CMD, (MissionStateModelInstance) m_modelInstance, "", Double.toString (x),
                Double.toString (y), Double.toString (w));
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
    
    @Operation(name=SET_RECONFIGURING_CMD)
    public SetReconfiguringCmd setReconfiguringCmd(boolean r) {
    	return new SetReconfiguringCmd(SET_RECONFIGURING_CMD, (MissionStateModelInstance )m_modelInstance, "", Boolean.toString(r));
    }

    @Operation(name=SET_UTILITY_PREFERENCE_CMD)
	public SetUtilityPreferenceCmd setUtilityPreference(UtilityPreference preference) {
		return new SetUtilityPreferenceCmd(SET_UTILITY_PREFERENCE_CMD, (MissionStateModelInstance )m_modelInstance, "", preference.toString());
	}
}
