package org.sa.rainbow.brass.analyses;

import java.util.List;

import org.sa.rainbow.brass.model.mission.MissionState;
import org.sa.rainbow.brass.model.mission.MissionState.CalibrationError;
import org.sa.rainbow.brass.model.mission.MissionState.GroundPlaneError;
import org.sa.rainbow.brass.model.mission.MissionStateModelInstance;
import org.sa.rainbow.brass.model.mission.RecalibrateCmd;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.IRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.analysis.IRainbowAnalysis;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;

public class CalibrationAnalysis extends AbstractRainbowRunnable implements IRainbowAnalysis {

    public static final String NAME = "BRASS Calibration Analyzer";
    private static final double MINIMUM_VEL                   = 0.1;
    private static final double ROTATIONAL_ERROR_THRESHOLD    = 0.01;
    private static final double ROTATIONAL_SCALE_THRESHOLD    = 0.001;
    private static final double TRANSLATIONAL_ERROR_THRESHOLD = 0.01;
    private static final double TRANSLATIONAL_SCALE_THRESHOLD = 0.001;
    private IModelsManagerPort m_modelsManagerPort;
    private IModelUSBusPort    m_modelUSPort;

    public CalibrationAnalysis () {
        super (NAME);
        String per = Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_MODEL_EVAL_PERIOD);
        if (per != null) {
            setSleepTime (Long.parseLong (per));
        }
        else {
            setSleepTime (IRainbowRunnable.LONG_SLEEP_TIME);
        }
    }

    @Override
    public void initialize (IRainbowReportingPort port) throws RainbowConnectionException {
        super.initialize (port);
        initConnections ();
    }

    private void initConnections () throws RainbowConnectionException {
        // Create a port to query things about a model
        m_modelsManagerPort = RainbowPortFactory.createModelsManagerRequirerPort ();

        // Create a port to change a model (e.g., to trigger adaptation, to set predicted score, etc.)
        m_modelUSPort = RainbowPortFactory.createModelsManagerClientUSPort (this);

    }

    @Override
    public void dispose () {
        m_reportingPort.dispose ();
        m_modelUSPort.dispose ();
    }

    @Override
    public void setProperty (String key, String value) {
    }

    @Override
    public String getProperty (String key) {
        return null;
    }

    @Override
    protected void log (String txt) {
        m_reportingPort.info (getComponentType (), txt);
    }

    protected boolean m_wasBad = false;

    @Override
    protected void runAction () {
        ModelReference missionStateRef = new ModelReference ("RobotAndEnvironmentState",
                MissionStateModelInstance.MISSION_STATE_TYPE);
        MissionStateModelInstance missionStateModel = (MissionStateModelInstance )m_modelsManagerPort
                .<MissionState> getModelInstance (missionStateRef);

        if (missionStateModel != null && !missionStateModel.getModelInstance ().isAdaptationNeeded ()) {
            MissionState missionState = missionStateModel.getModelInstance ();

            List<GroundPlaneError> gpes = missionState.getGroundPlaneSample (5);
            List<CalibrationError> ces = missionState.getCallibrationErrorSample (2);

            if (badCalibrationError (ces)) {
                RecalibrateCmd cmd = missionStateModel.getCommandFactory ().recalibrate (true);
                m_modelUSPort.updateModel (cmd);
                m_wasBad = true;
            }
            else if (m_wasBad) {
                RecalibrateCmd cmd = missionStateModel.getCommandFactory ().recalibrate (false);
                m_modelUSPort.updateModel (cmd);
                m_wasBad = true;
            }

        }
    }

    private boolean badCalibrationError (List<CalibrationError> ces) {
        CalibrationError sample1 = ces.get (1);
        CalibrationError sample2 = ces.get (0);

        if (sample2.velocity_at_time_of_error > MINIMUM_VEL)
            if ((sample2.rotational_error - sample1.rotational_error > ROTATIONAL_ERROR_THRESHOLD
                    && sample2.rotational_scale > ROTATIONAL_SCALE_THRESHOLD)
                    || (sample2.translational_error - sample1.translational_error > TRANSLATIONAL_ERROR_THRESHOLD
                            && sample2.translational_scale > TRANSLATIONAL_SCALE_THRESHOLD))
                return true;
        return false;
    }

    @Override
    public RainbowComponentT getComponentType () {
        return RainbowComponentT.ANALYSIS;
    }

}
