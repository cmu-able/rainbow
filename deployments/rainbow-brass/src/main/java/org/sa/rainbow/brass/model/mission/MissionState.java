package org.sa.rainbow.brass.model.mission;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.TimeZone;

import org.sa.rainbow.brass.model.instructions.SetLocalizationFidelityInstruction.LocalizationFidelity;
import org.sa.rainbow.core.models.ModelReference;

/**
 * Created by schmerl on 12/27/2016.
 */
public class MissionState {

    // This should be in UTC
    public static final SimpleDateFormat BRASS_DATE_FORMAT = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss'Z'");
    static {
        BRASS_DATE_FORMAT.setTimeZone (TimeZone.getTimeZone ("UTC"));
    }

    public static enum Heading {
        NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST;

        public static Heading convertFromRadians (double w) {
            if (w < 0) {
                w = w + 2 * Math.PI;
            }
            if (w < Math.PI / 8 && w >= 7 * Math.PI / 8) return EAST;
            if (w >= Math.PI / 8 && w < 3 * Math.PI / 8) return NORTHEAST;
            if (w >= 3 * Math.PI / 8 && w < 5 * Math.PI / 8) return NORTH;
            if (w >= 5 * Math.PI / 8 && w < 7 * Math.PI / 8) return NORTHWEST;
            if (w >= 7 * Math.PI / 8 && w < 9 * Math.PI / 8) return WEST;
            if (w >= 9 * Math.PI / 8 && w < 11 * Math.PI / 8) return Heading.SOUTHWEST;
            if (w >= 11 * Math.PI / 8 && w < 13 * Math.PI / 8) return SOUTH;
            if (w >= 13 * Math.PI / 8 && w < 15 * Math.PI / 8) return SOUTHEAST;
            return EAST;

        }

        public static double convertToRadians (Heading h) {
            if (h == EAST) return 0;
            if (h == NORTHEAST) return Math.PI / 4;
            if (h == NORTH) return Math.PI / 2;
            if (h == NORTHWEST) return 3 * Math.PI / 4;
            if (h == WEST) return Math.PI;
            if (h == SOUTHWEST) return 5 * Math.PI / 4;
            if (h == SOUTH) return 3 * Math.PI / 2;
            if (h == SOUTHEAST) return 7 * Math.PI / 4;
            return 0;
        }
    };

    public static class LocationRecording {
        protected double  m_x;
        protected double  m_y;
        protected double  m_w;
        protected Heading m_heading;
        protected long    m_time;

        public double getX () {
            return m_x;
        }

        public double getY () {
            return m_y;
        }

        public long getTime () {
            return m_time;
        }

        public Heading getHeading () {
            return m_heading;
        }

        public double getRotation () {
            return m_w;
        }

        public LocationRecording copy () {
            LocationRecording l = new LocationRecording ();
            l.m_x = m_x;
            l.m_y = m_y;
            l.m_w = m_w;
            l.m_heading = m_heading;
            l.m_time = m_time;
            return l;
        }
    }


    private final ModelReference m_model;

    //private List<String>         m_instructionHistory       = new ArrayList<> ();
    private Deque<Long>          m_predictedTimeHistory     = new ArrayDeque<> ();
    private Deque<Long>          m_predictedAccuracyHistory = new ArrayDeque<> ();
    private Deque<Double>        m_timeScore                = new ArrayDeque<> ();
    private Deque<Double>        m_accuracyScore            = new ArrayDeque<> ();
    private Deque<Double>        m_safetyScore              = new ArrayDeque<> ();

    Deque<LocationRecording>     m_locationHistory          = new ArrayDeque<> ();
    Deque<Double>                m_chargeHistory            = new ArrayDeque<> ();
    Deque<Long>              m_deadlineHistory = new ArrayDeque<> ();
    Deque<LocalizationFidelity> m_localizationFidelityHistory = new ArrayDeque<> ();

    private boolean              m_robotObstructed          = false;
    private boolean m_robotOnTime     = true;
    private boolean m_robotAccurate   = true;
    private String  m_targetWaypoint  = "";

    private double m_currentTime = 0;

    public MissionState (ModelReference model) {
        m_model = model;
    }

    public ModelReference getModelReference () {
        return m_model;
    }

    public void setCurrentPose (double x, double y, double w) {
        LocationRecording l = new LocationRecording ();
        l.m_x = x;
        l.m_y = y;
        l.m_w = w;
        l.m_heading = Heading.convertFromRadians (w);
        l.m_time = new Date ().getTime ();
        synchronized (m_locationHistory) {
            m_locationHistory.push (l);
        }
    }

    public LocationRecording getCurrentPose () {
        if (m_locationHistory.isEmpty ()) return null;
        synchronized (m_locationHistory) {
            return m_locationHistory.peek ().copy ();
        }
    }

    public LocationRecording getInitialPose () {
        synchronized (m_locationHistory) {
            if (m_locationHistory.isEmpty ()) return null;
            return m_locationHistory.getLast ().copy ();
        }
    }

    public void setRobotObstructed (boolean robotObstructed) {
        m_robotObstructed = robotObstructed;
    }

    public boolean isRobotObstructed () {
        return m_robotObstructed;
    }

    public void setRobotOnTime (boolean isOnTime) {
        m_robotOnTime = isOnTime;
    }

    public boolean isRobotOnTime () {
        return m_robotOnTime;
    }

    public void setRobotAccurate (boolean isAccurate) {
        m_robotAccurate = isAccurate;
    }

    public boolean isRobotAccurate () {
        return m_robotAccurate;
    }

    public void setBatteryCharge (Double charge) {
        synchronized (m_chargeHistory) {
            m_chargeHistory.push (charge);
        }
    }

    public Double getBatteryCharge () {
        synchronized (m_chargeHistory) {

            Double charge = m_chargeHistory.peek ();
            if (charge == null) return null;
            return charge;
        }
    }

    public void setDeadline (long d) {
        synchronized (m_deadlineHistory) {

            m_deadlineHistory.push (d);
        }
    }

    public Long getDeadline () {
        synchronized (m_deadlineHistory) {

            return m_deadlineHistory.peek ();
        }
    }

    public void setLocalizationFidelity (LocalizationFidelity fidelity) {
        m_localizationFidelityHistory.push (fidelity);
    }

    public LocalizationFidelity getLocalizationFidelity () {
        return m_localizationFidelityHistory.peek ();
    }

    public void setTargetWaypoint (String waypoint) {
        m_targetWaypoint = waypoint;
    }

    public String getTargetWaypoint () {
        return m_targetWaypoint;
    }

    public double getCurrentTime () {
        return m_currentTime;
    }

    public void setCurrentTime (double time) {
        m_currentTime = time;
    }

//    public double getSpeed() {
//        LocationRecording recent, next;
//        synchronized (m_locationHistory) {
//            if (m_locationHistory.size () > 2) {
//                recent = m_locationHistory.pop ();
//                next = m_locationHistory.pop ();
//                m_locationHistory.push (next);
//                m_locationHistory.push (recent);
//            }
//            else return 0;
//        }
//        double dist = Math.sqrt ()
//        
//    }
    /**
     * 
     * @return True iff the robot encounters (or expects to encounter) problems
     */
    public boolean isAdaptationNeeded () {
        return isRobotObstructed () || !isRobotOnTime () || !isRobotAccurate () || isBadlyCalibrated ();
    }

    public MissionState copy () {
        MissionState s = new MissionState (m_model);
        s.m_locationHistory = new ArrayDeque<> (m_locationHistory);
        s.m_chargeHistory = new ArrayDeque<> (m_chargeHistory);
        s.m_deadlineHistory = new ArrayDeque<> (m_deadlineHistory);
        return s;
    }

    // Below are for challenge problem 2

    public static class GroundPlaneError {
        public double translational_error;
        public double rotational_error;
    }

    public static class CalibrationError {
        public double rotational_error;
        public double rotational_scale;
        public double translational_error;
        public double translational_scale;
        public double rotational_velocity_at_time_of_error;
        public double translational_velocity_at_time_of_error;
    }

    Deque<GroundPlaneError> m_groundPlaneErrorHistory  = new ArrayDeque<> ();
    Deque<CalibrationError> m_calibarationErrorHistory = new ArrayDeque<> ();
    private boolean         m_isBadlyCalibrated        = false;

    public List<GroundPlaneError> getGroundPlaneSample (int sampleSize) {
        if (m_groundPlaneErrorHistory.size () < sampleSize) return Collections.<GroundPlaneError> emptyList ();
        List<GroundPlaneError> ret = new ArrayList<> (sampleSize);
        synchronized (m_groundPlaneErrorHistory) {
            for (int i = 0; i < sampleSize; i++) {
                ret.add (m_groundPlaneErrorHistory.pop ());
            }
            // Now put them back
            for (int i = ret.size (); i > 0; i--) {
                m_groundPlaneErrorHistory.push (ret.get (i - 1));
            }
        }
        return ret;
    }

    public boolean isBadlyCalibrated () {
        return m_isBadlyCalibrated;
    }

    public void setBadlyCalibrated (boolean bad) {
        System.out.println (
                "Setting calibration state to " + bad + ". isAdapationNeeded() is now " + isAdaptationNeeded ());
        m_isBadlyCalibrated = bad;
    }

    public void addGroundPlaneSample (GroundPlaneError error) {
        synchronized (m_groundPlaneErrorHistory) {
            m_groundPlaneErrorHistory.push (error);
        }
    }

    protected static final int NUM_SAMPLES = 4;
    protected double[]         r_sample    = new double[NUM_SAMPLES]; // Rolling window
    protected int              r_num       = 0;                       // Number of samples
    protected int              r_idx       = 0;                       // Index of oldest sample

    public double rErrAvg () {
        double sum = 0.0;
        for (int i = 0; i < r_num; i++) {
            sum += r_sample[i];
        }
        return sum / r_num;
    }

    public void addCalibrationErrorSample (CalibrationError c) {
        synchronized (m_calibarationErrorHistory) {
//            if (r_num < 3) { // Need at least 3 samples for an average
//                // Add this to the winddow
//                r_sample[r_idx] = c.rotational_error;
//                r_num++;
//                r_idx = (r_idx + 1) % NUM_SAMPLES;
//            }
//            if (r_num > 3) {
//                // discount outliers
//                r_num = Math.min (++r_num, NUM_SAMPLES);
//                r_idx = (r_idx + 1) % NUM_SAMPLES;
//                if (Math.abs (c.rotational_error - rErrAvg ()) < ROTATIONAL_ERROR_THRESHOLD) {
//                    r_sample[r_idx] = c.rotational_error;
//                }
//            }
//            // Record each outlier
            m_calibarationErrorHistory.push (c);
        }
    }

    public List<CalibrationError> getCallibrationErrorSample (int sampleSize) {
        if (m_calibarationErrorHistory.size () < sampleSize) return Collections.<CalibrationError> emptyList ();
        List<CalibrationError> ret = new ArrayList<> ();
        synchronized (m_calibarationErrorHistory) {
            for (int i = 0; i < sampleSize; i++) {
                ret.add (m_calibarationErrorHistory.pop ());
            }
            // Now put them back
            for (int i = ret.size (); i > 0; i--) {
                m_calibarationErrorHistory.push (ret.get (i - 1));
            }
        }
        return ret;
    }

    public Deque<CalibrationError> getCalibrationErrorObservations () {
        return m_calibarationErrorHistory;
    }

}
