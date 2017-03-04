package org.sa.rainbow.brass.analyses;

import java.text.MessageFormat;
import java.util.List;

import org.sa.rainbow.brass.das.BRASSHttpConnector;
import org.sa.rainbow.brass.das.IBRASSConnector.DASStatusT;
import org.sa.rainbow.brass.model.instructions.IInstruction;
import org.sa.rainbow.brass.model.instructions.InstructionGraphModelInstance;
import org.sa.rainbow.brass.model.instructions.InstructionGraphProgress;
import org.sa.rainbow.brass.model.instructions.MoveAbsHInstruction;
import org.sa.rainbow.brass.model.instructions.SetExecutionFailedCmd;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.EnvMapModelInstance;
import org.sa.rainbow.brass.model.map.InsertNodeCmd;
import org.sa.rainbow.brass.model.mission.MissionState;
import org.sa.rainbow.brass.model.mission.MissionState.CalibrationError;
import org.sa.rainbow.brass.model.mission.MissionState.GroundPlaneError;
import org.sa.rainbow.brass.model.mission.MissionState.LocationRecording;
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

    public static final String  NAME                          = "BRASS Calibration Analyzer";
    private static final double MINIMUM_VEL                   = 0.1;
    private static final double ROTATIONAL_ERROR_THRESHOLD    = 1.5;
    private static final double ROTATIONAL_SCALE_THRESHOLD    = 1.5;
    private static final double TRANSLATIONAL_ERROR_THRESHOLD = 0.01;
    private static final double TRANSLATIONAL_SCALE_THRESHOLD = Math.toRadians (1);
    private IModelsManagerPort  m_modelsManagerPort;
    private IModelUSBusPort     m_modelUSPort;

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
        log ("Calibration Analyzer up and running");
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

    private MoveAbsHInstruction getPreviousMoveAbsH (MoveAbsHInstruction currentMoveAbsH,
            InstructionGraphProgress igProgress) {
        int j = Integer.valueOf (currentMoveAbsH.getInstructionLabel ()) - 1;
        for (int i = j; i > 0; i--) {
            String label = String.valueOf (i);
            IInstruction instruction = igProgress.getInstruction (label);

            if (instruction instanceof MoveAbsHInstruction) return (MoveAbsHInstruction )instruction;
        }

        // No previous MoveAbsH instruction
        return null;
    }

    protected boolean m_wasBad      = false;
    protected boolean m_detectedBad = false;

    @Override
    protected void runAction () {
        ModelReference missionStateRef = new ModelReference ("RobotAndEnvironmentState",
                MissionStateModelInstance.MISSION_STATE_TYPE);
        MissionStateModelInstance missionStateModel = (MissionStateModelInstance )m_modelsManagerPort
                .<MissionState> getModelInstance (missionStateRef);
        ModelReference igRef = new ModelReference ("ExecutingInstructionGraph",
                InstructionGraphModelInstance.INSTRUCTION_GRAPH_TYPE);
        InstructionGraphModelInstance igModel = (InstructionGraphModelInstance )m_modelsManagerPort
                .<InstructionGraphProgress> getModelInstance (igRef);
        ModelReference emRef = new ModelReference ("Map", EnvMapModelInstance.ENV_MAP_TYPE);
        EnvMapModelInstance envModel = (EnvMapModelInstance )m_modelsManagerPort.<EnvMap> getModelInstance (emRef);
        InstructionGraphProgress igProgress = igModel.getModelInstance ();

        if (missionStateModel != null && !missionStateModel.getModelInstance ().isAdaptationNeeded ()) {
            MissionState missionState = missionStateModel.getModelInstance ();

            List<GroundPlaneError> gpes = missionState.getGroundPlaneSample (5);
            List<CalibrationError> ces = missionState.getCallibrationErrorSample (2);
            if (!m_detectedBad) { // TODO: THis is a hack but they are only perturbing us once
                if (/*badCalibrationError (ces, missionState.rErrAvg (), missionState.tErrAvg ())
                        || badGroundPlaneError (gpes)*/newbadCalibrationError (missionState)
                        ) {

                    BRASSHttpConnector.instance ().reportStatus (DASStatusT.PERTURBATION_DETECTED,
                            "Detected a calibration error");
                    log ("Detected a calibration error");
                    insertCurrentLocationInMap (missionStateModel, igModel, envModel, igProgress, missionState);
                    RecalibrateCmd cmd = missionStateModel.getCommandFactory ().recalibrate (true);
                    m_modelUSPort.updateModel (cmd);
                    m_wasBad = true;
                    m_detectedBad = true;
                }
                else if ((!igProgress.getInstructions ().isEmpty () && !igProgress.getCurrentOK ())) {
                    BRASSHttpConnector.instance ().reportStatus (DASStatusT.PERTURBATION_DETECTED,
                            "Could not continue path");
                    m_reportingPort.info (getComponentType (), "Instruction graph failed...updating map model");
                    insertCurrentLocationInMap (missionStateModel, igModel, envModel, igProgress, missionState);
                }
                else if (m_wasBad) {
                    RecalibrateCmd cmd = missionStateModel.getCommandFactory ().recalibrate (false);
                    m_modelUSPort.updateModel (cmd);
                    m_wasBad = true;
                }
            }

        }
    }

    void insertCurrentLocationInMap (MissionStateModelInstance missionStateModel,
            InstructionGraphModelInstance igModel,
            EnvMapModelInstance envModel,
            InstructionGraphProgress igProgress,
            MissionState missionState) {
        EnvMap envMap = envModel.getModelInstance ();
        // Get current robot position
        LocationRecording pose = missionState.getCurrentPose ();

        // Get source and target positions of the failing instruction
        IInstruction currentInst = igProgress.getCurrentInstruction ();

        // The current instruction is of type MoveAbsH
        if (currentInst instanceof MoveAbsHInstruction) {
            MoveAbsHInstruction currentMoveAbsH = (MoveAbsHInstruction )currentInst;
            MoveAbsHInstruction prevMoveAbsH = getPreviousMoveAbsH (currentMoveAbsH, igProgress);

            double sourceX;
            double sourceY;
            double targetX = currentMoveAbsH.getTargetX ();
            double targetY = currentMoveAbsH.getTargetY ();

            if (prevMoveAbsH != null) {
                sourceX = prevMoveAbsH.getTargetX ();
                sourceY = prevMoveAbsH.getTargetY ();
            }
            else {
                // The current instruction is the first MoveAbsH instruction in IG
                // Use the initial pose as the source pose
                sourceX = missionState.getInitialPose ().getX ();
                sourceY = missionState.getInitialPose ().getY ();
            }

            // Find the corresponding environment map nodes of the source and target positions
            // Node naming assumption: node's label is lX where X is the order in which the node is added
            int numNodes = envMap.getNodeCount () + 1;
            String n = "l" + numNodes;
            String na = envMap.getNode (sourceX, sourceY).getLabel ();
            String nb = envMap.getNode (targetX, targetY).getLabel ();

            // Update the environment map
            String rx = Double.toString (pose.getX ());
            String ry = Double.toString (pose.getY ());
            InsertNodeCmd insertNodeCmd = envModel.getCommandFactory ().insertNodeCmd (n, na, nb, rx, ry, "false");
            log ("Inserting node '" + n + "' at (" + rx + ", " + ry + ") between " + na + " and " + nb);

            SetExecutionFailedCmd resetFailedCmd = igModel.getCommandFactory ().setExecutionFailedCmd ("false");
            RecalibrateCmd cmd = missionStateModel.getCommandFactory ().recalibrate (true);
            m_modelUSPort.updateModel (cmd);
            // Send the commands -- different models, so can't bundle them
            m_modelUSPort.updateModel (resetFailedCmd);
            m_modelUSPort.updateModel (insertNodeCmd);
        }
    }

    private boolean badGroundPlaneError (List<GroundPlaneError> gpes) {
        double avgTrans = 0.0;
        double avgRot = 0.0;

        for (GroundPlaneError gpe : gpes) {
            avgTrans += gpe.translational_error;
            avgRot += gpe.rotational_error;
        }
        avgTrans /= gpes.size ();
        avgRot /= gpes.size ();

        return Math.abs (avgRot) > Math.toRadians (5) || Math.abs (avgTrans) > .04;
    }

    private static int SIZE_FOR_AVERAGE = 2;

    private boolean newbadCalibrationError (MissionState ms) {
        // Get the last 6 observations
        List<CalibrationError> ces = ms.getCallibrationErrorSample (2 + SIZE_FOR_AVERAGE);

        if (!ces.isEmpty ()) { // Have requisite samples
            double r_sum = 0, t_sum = 0;
            for (int i = 2; i < ces.size (); i++) {
                r_sum += ces.get (i).rotational_error;
                t_sum += ces.get (i).translational_error;
            }
            double rAvg = r_sum / SIZE_FOR_AVERAGE;
            double tAvg = t_sum / SIZE_FOR_AVERAGE;
            CalibrationError mostRecent = ces.get (0);
            CalibrationError nextRecent = ces.get (1);
            log (MessageFormat.format ("MostRecent(rot): {0,number,#.#####}; /rAvg: {1,number,#.#####}",
                    mostRecent.rotational_error, Math.abs (mostRecent.rotational_error / rAvg)));
            log (MessageFormat.format ("NextRecent(rot): {0,number,#.#####}; /rAvg: {1,number,#.#####}",
                    nextRecent.rotational_error, Math.abs (nextRecent.rotational_error / rAvg)));
            log (MessageFormat.format ("MostRecent(trn): {0,number,#.#####}; /tAvg: {1,number,#.#####}",
                    mostRecent.translational_error, Math.abs (mostRecent.translational_error / tAvg)));
            log (MessageFormat.format ("MostRecent(trn): {0,number,#.#####}; /tAvg: {1,number,#.#####}",
                    nextRecent.translational_error, Math.abs (nextRecent.translational_error / tAvg)));
            log (
                    MessageFormat.format ("RotAvg: {0,number,#.#####}; TrnAvg: {1,number,#.#####}", rAvg, tAvg));
            log (MessageFormat.format ("RScale {0,number,#.#####}; TScale: {1,number,#.#####}",
                    mostRecent.rotational_scale, mostRecent.translational_scale));
            if (mostRecent.rotational_velocity_at_time_of_error > MINIMUM_VEL)
                if ((Math.abs (mostRecent.rotational_error / rAvg) > ROTATIONAL_ERROR_THRESHOLD
                        && Math.abs (nextRecent.rotational_error / rAvg) > ROTATIONAL_ERROR_THRESHOLD
                        && mostRecent.rotational_scale > ROTATIONAL_SCALE_THRESHOLD))

                    return true;
            if (mostRecent.translational_velocity_at_time_of_error > Math.toRadians (1))
                if (Math.abs (mostRecent.translational_error / tAvg) > TRANSLATIONAL_ERROR_THRESHOLD
                        && Math.abs (nextRecent.translational_error / tAvg) > TRANSLATIONAL_ERROR_THRESHOLD
                        && mostRecent.translational_scale > TRANSLATIONAL_SCALE_THRESHOLD)
                    return true;
        }
        return false;
    }

//    private boolean badCalibrationError (List<CalibrationError> ces, double rAvg, double tAvg) {
//        CalibrationError sample1 = ces.get (1);
//        CalibrationError sample2 = ces.get (0);
//
//        if (sample2.velocity_at_time_of_error > MINIMUM_VEL)
//            if ((Math.abs (sample2.rotational_error - rAvg) > ROTATIONAL_ERROR_THRESHOLD
//                    && Math.abs (sample1.rotational_error - rAvg) > ROTATIONAL_ERROR_THRESHOLD
//                    && sample2.rotational_scale > ROTATIONAL_SCALE_THRESHOLD)
//                    || (sample2.translational_error - sample1.translational_error > TRANSLATIONAL_ERROR_THRESHOLD
//                            && sample2.translational_scale > TRANSLATIONAL_SCALE_THRESHOLD))
//                return true;
//        return false;
//    }

    @Override
    public RainbowComponentT getComponentType () {
        return RainbowComponentT.ANALYSIS;
    }

}
