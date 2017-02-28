package org.sa.rainbow.brass.model.mission;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.TimeZone;

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
    Deque<LocationRecording>     m_locationHistory          = new ArrayDeque<> ();
    private List<String>         m_instructionHistory       = new ArrayList<> ();
    private Deque<Long>          m_predictedTimeHistory     = new ArrayDeque<> ();
    private Deque<Long>          m_predictedAccuracyHistory = new ArrayDeque<> ();
    private Deque<Double>        m_timeScore                = new ArrayDeque<> ();
    private Deque<Double>        m_accuracyScore            = new ArrayDeque<> ();
    private Deque<Double>        m_safetyScore              = new ArrayDeque<> ();
    private boolean              m_robotObstructed          = false;
    Deque<Double>                m_chargeHistory            = new ArrayDeque<> ();
    Deque<Date>                  m_deadlineHistory          = new ArrayDeque<> ();

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
        m_locationHistory.push (l);
    }

    public LocationRecording getCurrentPose () {
        if (m_locationHistory.isEmpty ()) return null;
        return m_locationHistory.peek ().copy ();
    }

    public LocationRecording getInitialPose () {
        if (m_locationHistory.isEmpty ()) return null;
        return m_locationHistory.getLast ().copy ();
    }

    public void setRobotObstructed (boolean robotObstructed) {
        m_robotObstructed = robotObstructed;
    }

    public boolean isRobotObstructed () {
        return m_robotObstructed;
    }

    public void setCurrentInstruction (String instLabel) {
        m_instructionHistory.add (instLabel);
    }

    public String getCurrentInstruction () {
        return m_instructionHistory.get (m_instructionHistory.size () - 1);
    }

    public boolean hasPreviousInstruction () {
        return m_instructionHistory.size () > 1;
    }

    public String getPreviousInstruction () {
        return m_instructionHistory.get (m_instructionHistory.size () - 2);
    }

    public void setBatteryCharge (Double charge) {
        m_chargeHistory.push (charge);
    }

    public Double getBatteryCharge () {
        return m_chargeHistory.peek ();
    }

    public void setDeadline (Date d) {
        m_deadlineHistory.push (d);
    }

    public Date getDeadline () {
        return m_deadlineHistory.peek ();
    }

    public MissionState copy () {
        MissionState s = new MissionState (m_model);
        s.m_locationHistory = new ArrayDeque<> (m_locationHistory);
        s.m_chargeHistory = new ArrayDeque<> (m_chargeHistory);
        return s;
    }

}
