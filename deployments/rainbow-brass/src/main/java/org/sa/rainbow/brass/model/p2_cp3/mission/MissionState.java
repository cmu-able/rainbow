package org.sa.rainbow.brass.model.p2_cp3.mission;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.TimeZone;

import org.sa.rainbow.brass.model.instructions.SetLocalizationFidelityInstruction.LocalizationFidelity;
import org.sa.rainbow.brass.model.p2_cp3.clock.ClockedModel;
import org.sa.rainbow.core.models.ModelReference;

/**
 * Created by schmerl on 12/27/2016.
 */
public class MissionState extends ClockedModel {

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

        public double getX () {
            return m_x;
        }

        public double getY () {
            return m_y;
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
            return l;
        }
    }


    private final ModelReference m_model;


    Deque<TimeStamped<LocationRecording>>     m_locationHistory          = new ArrayDeque<> ();
    Deque<TimeStamped<Long>>              m_deadlineHistory = new ArrayDeque<> ();

    private String  m_targetWaypoint  = "";


	private boolean m_reconfiguring;

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
        synchronized (m_locationHistory) {
            m_locationHistory.push (new TimeStamped<MissionState.LocationRecording>(l));
        }
    }

    public LocationRecording getCurrentPose () {
        if (m_locationHistory.isEmpty ()) return null;
        synchronized (m_locationHistory) {
            return m_locationHistory.peek ().data.copy ();
        }
    }

    public LocationRecording getInitialPose () {
        synchronized (m_locationHistory) {
            if (m_locationHistory.isEmpty ()) return null;
            return m_locationHistory.getLast ().data.copy ();
        }
    }

    public void setDeadline (long d) {
        synchronized (m_deadlineHistory) {
            m_deadlineHistory.push (new TimeStamped<Long>(d));
        }
    }

    public Long getDeadline () {
        synchronized (m_deadlineHistory) {

            TimeStamped<Long> peek = m_deadlineHistory.peek ();
			return peek!=null?peek.data:null;
        }
    }

    public void setTargetWaypoint (String waypoint) {
        m_targetWaypoint = waypoint;
    }

    public String getTargetWaypoint () {
        return m_targetWaypoint;
    }


    public MissionState copy () {
        MissionState s = new MissionState (m_model);
        s.m_locationHistory = new ArrayDeque<> (m_locationHistory);
        s.m_deadlineHistory = new ArrayDeque<> (m_deadlineHistory);
        s.m_reconfiguring = m_reconfiguring;
        return s;
    }

    public boolean isMissionStarted () {
    	// TODO: CHange this when mission start detection is implemented
    	return true || !"".equals(getTargetWaypoint ());
    }
    
    public boolean isReconfiguring() {
    	return m_reconfiguring;
    }
    
    public void setReconfiguring(boolean r) {
    	m_reconfiguring = r;
    }

}
