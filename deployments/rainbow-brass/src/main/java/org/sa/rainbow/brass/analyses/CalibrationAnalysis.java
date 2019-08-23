package org.sa.rainbow.brass.analyses;

import java.text.MessageFormat;
import java.util.List;
import java.util.Vector;

import org.sa.rainbow.brass.das.BRASSHttpConnector;
import org.sa.rainbow.brass.das.IBRASSConnector.DASPhase1StatusT;
import org.sa.rainbow.brass.das.IBRASSConnector.Phases;
import org.sa.rainbow.brass.model.instructions.IInstruction;
import org.sa.rainbow.brass.model.instructions.InstructionGraphModelInstance;
import org.sa.rainbow.brass.model.instructions.InstructionGraphProgress;
import org.sa.rainbow.brass.model.instructions.InstructionGraphProgress.IGExecutionStateT;
import org.sa.rainbow.brass.model.instructions.MoveAbsHInstruction;
import org.sa.rainbow.brass.model.instructions.SetExecutionFailedCmd;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.EnvMapModelInstance;
import org.sa.rainbow.brass.model.map.EnvMapNode;
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

    private static final double MIN_ROT                       = Math.toRadians (0.5);
    public static final String  NAME                          = "BRASS Calibration Analyzer";
    private static final double MINIMUM_VEL                   = 0.001;
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

    protected boolean m_wasBad                  = false;
    protected boolean m_detectedBad             = false;
    protected int     m_calibrationErrorObsSize = 0;

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
            if (igProgress.getInstructionGraphState () == IGExecutionStateT.FINISHED_SUCCESS) {
                BRASSHttpConnector.instance (Phases.Phase1).reportDone (false, "Successfully reached the goal");
                m_rainbowEnvironment.signalTerminate ();
                return;
            }
            if (!m_detectedBad) { // TODO: THis is a hack but they are only perturbing us once
//                Deque<CalibrationError> calibrationErrorObservations = missionState.getCalibrationErrorObservations ();
//                int size = calibrationErrorObservations.size ();
//                if (size <= m_calibrationErrorObsSize) return;
//                if (size >= 2) {
//                    double r_sum = 0.0;
//                    double t_sum = 0.0;
//                    double tp_sum = 0.0;
//                    double rp_sum = 0.0;
//                    for (CalibrationError ce : calibrationErrorObservations) {
//                        r_sum += ce.rotational_error;
//                        t_sum += ce.translational_error;
//                        rp_sum += ce.rotational_scale;
//                        tp_sum += ce.translational_scale;
//                    }
//                    log (MessageFormat.format ("RotAvg: {0,number,#.######}; TAvg: {1,number, #.######}", r_sum / size,
//                            t_sum / size));
//                    log (MessageFormat.format ("RotPerAvg: {0,number,#.######}; TPerAvg: {1,number, #.######}",
//                            rp_sum / size, tp_sum / size));
//                }
//                m_calibrationErrorObsSize = size;
                if (/*badCalibrationError (ces, missionState.rErrAvg (), missionState.tErrAvg ())*/
                        (groundPlaneApplicable (missionState, envModel) && badGroundPlaneError (gpes))
                        || newerbadCalibrationError (missionState)) {

                    BRASSHttpConnector.instance (Phases.Phase1).reportStatus (DASPhase1StatusT.PERTURBATION_DETECTED.name(),
                            "Detected a calibration error");
                    log ("Detected a calibration error");
                    insertCurrentLocationInMap (missionStateModel, igModel, envModel, igProgress, missionState);
                    RecalibrateCmd cmd = missionStateModel.getCommandFactory ().recalibrate (true);
                    m_modelUSPort.updateModel (cmd);
                    m_wasBad = true;
                    m_detectedBad = true;
                    try {
                        Thread.sleep (4000);
                    }
                    catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace ();
                    }
                    RecalibrateCmd cmd2 = missionStateModel.getCommandFactory ().recalibrate (false);
                    m_modelUSPort.updateModel (cmd2);
                    m_wasBad = true;
                }
                else if ((!igProgress.getInstructions ().isEmpty () && !igProgress.getCurrentOK ())) {
                    BRASSHttpConnector.instance (Phases.Phase1).reportStatus (DASPhase1StatusT.PERTURBATION_DETECTED.name(),
                            "Could not continue path");
                    m_reportingPort.info (getComponentType (), "Instruction graph failed...updating map model");
                    insertCurrentLocationInMap (missionStateModel, igModel, envModel, igProgress, missionState);
                    RecalibrateCmd cmd = missionStateModel.getCommandFactory ().recalibrate (true);
                    m_modelUSPort.updateModel (cmd);
                    m_wasBad = true;
                    m_detectedBad = true;
                    try {
                        Thread.sleep (5);
                    }
                    catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace ();
                    }
                    RecalibrateCmd cmd2 = missionStateModel.getCommandFactory ().recalibrate (false);
                    m_modelUSPort.updateModel (cmd2);
                    m_wasBad = true;
                }
            }
            else if (m_wasBad) {
                RecalibrateCmd cmd = missionStateModel.getCommandFactory ().recalibrate (false);
                m_modelUSPort.updateModel (cmd);
                m_wasBad = true;
            }

        }
    }

    private boolean groundPlaneApplicable (MissionState missionState, EnvMapModelInstance envModel) {
        LocationRecording currentPose = missionState.getCurrentPose ();
        if (currentPose == null) return false;

        EnvMapNode l = envModel.getModelInstance ().getNode (currentPose.getX (), currentPose.getY ());
        if (l == null) {
            log ("Not near a waypoint");
        }
        return (l == null);
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

        boolean groundError = Math.abs (avgRot) > Math.toRadians (5) || Math.abs (avgTrans) > .04;
        if (groundError) {
            log ("Calibration error detected by ground plane error");
        }
        return groundError;
    }

    private static int SIZE_FOR_AVERAGE = 2;

    Vector<Double> m_last_errors_rot   = new Vector<> ();
    Vector<Double> m_last_errors_trans = new Vector<> ();
    double         average_error_trans = 0.7;
    double         average_error_rot   = 0.7;

    int num_samples_trans = 0;
    int num_samples_rot   = 0;

    int m_obs_size = 0;

    private boolean newerbadCalibrationError (MissionState ms) {

        CalibrationError latestError = null;
        if (!ms.getCallibrationErrorSample (1).isEmpty ()) {
            latestError = ms.getCallibrationErrorSample (1).iterator ().next ();
        }

        if (ms.getCalibrationErrorObservations ().size () == m_obs_size) return false; // Haven't had a new observation
        m_obs_size = ms.getCalibrationErrorObservations ().size ();
        if (latestError != null) {
            if (latestError.rotational_velocity_at_time_of_error > 0.01
                    && latestError.rotational_velocity_at_time_of_error < 0.3) {

                if (latestError.rotational_scale > 0.01) {
                    m_last_errors_rot.add (latestError.rotational_scale);
                }
                if (m_last_errors_rot.size () > 3) {
                    double oldestError = m_last_errors_rot.get (0);
                    m_last_errors_rot.remove (0);
                    average_error_rot = updateAverageError (average_error_rot, oldestError, num_samples_rot);
                    num_samples_rot++;
                }
            }
            if (latestError.translational_velocity_at_time_of_error > 0.01
                    && latestError.translational_velocity_at_time_of_error < .1) {
                if (latestError.translational_scale > 0.01) {
                    m_last_errors_trans.add (latestError.translational_scale);
                }
                if (m_last_errors_trans.size () > 3) {
                    double oldestError = m_last_errors_trans.get (0);
                    m_last_errors_trans.remove (0);
                    average_error_trans = updateAverageError (average_error_trans, oldestError, num_samples_trans);
                    num_samples_trans++;
                }
            }
            boolean rot_error = true;
            boolean rot_error_constant = true;
            ;
            if (m_last_errors_rot.size () == 3) {
                for (int i = 0; i < m_last_errors_rot.size (); i++) {
                    double percent_error = m_last_errors_rot.get (i) / average_error_rot;
                    if (percent_error < 1.5) {
                        rot_error = false;
                    }
                    double percent_error_constant = m_last_errors_rot.get (i) / 0.7;
                    if (percent_error_constant < 1.5) {
                        rot_error_constant = false;
                    }
                }
            }
            else {
                rot_error = false;
                rot_error_constant = false;
            }
            boolean trans_error = true;
            boolean trans_error_constant = true;
            if (m_last_errors_trans.size () == 3) {
                for (int i = 0; i < m_last_errors_trans.size (); i++) {
                    double percent_error = m_last_errors_trans.get (i) / average_error_trans;
                    if (percent_error < 1.5) {
                        trans_error = false;
                    }
                    double percent_error_constant = m_last_errors_trans.get (i) / 0.7;
                    if (percent_error_constant < 1.5) {
                        trans_error_constant = false;
                    }
                }
            }
            else {
                trans_error = false;
                trans_error_constant = false;
            }
            if (rot_error || trans_error) {

                log ("Calibration error detected by delta calibration with moving average: info below");
            }
            if (rot_error_constant || trans_error_constant) {
                log ("Calibration error detected by delta calibration with 0.7 average: info below");
            }
            StringBuffer tranStr = new StringBuffer ();
            tranStr.append ("[");
            for (Double t : m_last_errors_trans) {
                tranStr.append (MessageFormat.format ("{0,number,##.######}", t));
                tranStr.append (", ");
            }
            tranStr.delete (tranStr.length () - 1, tranStr.length ());
            tranStr.append ("]");
            StringBuffer rotStr = new StringBuffer ();
            rotStr.append ("[");
            for (Double t : m_last_errors_rot) {
                rotStr.append (MessageFormat.format ("{0,number,##.######}", t));
                rotStr.append (", ");
            }
            rotStr.delete (rotStr.length () - 1, rotStr.length ());
            rotStr.append ("]");
            log (MessageFormat.format ("AvgErrorTrans: {0,number,##.#####}, {1}", average_error_trans,
                    tranStr.toString ()));
            log (MessageFormat.format ("AvgErrorRot: {0,number,##.#####}, {1}", average_error_rot, rotStr.toString ()));

            return false; /*&& (rot_error || trans_error);*/
        }
        else
            return false;

    }

    private double updateAverageError (double ae, double newSample, int num) {
        if (num == 0) return ae;
        double avg = (ae * (num - 1) + newSample) / num;
        return avg;
    }

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
            log (MessageFormat.format ("RotAvg: {0,number,#.#####}; TrnAvg: {1,number,#.#####}", rAvg, tAvg));
            log (MessageFormat.format ("RScale {0,number,#.#####}; TScale: {1,number,#.#####}",
                    mostRecent.rotational_scale, mostRecent.translational_scale));

            if (mostRecent.rotational_velocity_at_time_of_error > MINIMUM_VEL)
                if ((Math.abs (mostRecent.rotational_error / rAvg) > ROTATIONAL_ERROR_THRESHOLD
                        && Math.abs (nextRecent.rotational_error / rAvg) > ROTATIONAL_ERROR_THRESHOLD
                        && mostRecent.rotational_scale > ROTATIONAL_SCALE_THRESHOLD))
                    return true;
            if (mostRecent.translational_velocity_at_time_of_error > MIN_ROT)
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
