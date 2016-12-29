package org.sa.rainbow.brass.model.mission;

import org.sa.rainbow.core.models.ModelReference;

import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;

/**
 * Created by schmerl on 12/27/2016.
 */
public class MissionState {


    private final ModelReference m_model;

    public static class LocationRecording {
        protected double m_x;
        protected double m_y;
        protected long m_time;

        public double getX () {return m_x;}
        public double getY () {return m_y;}
        public long getTime () {return m_time;}

        public LocationRecording copy() {
            LocationRecording l = new LocationRecording ();
            l.m_x = m_x;
            l.m_y = m_y;
            l.m_time = m_time;
            return l;
        }
    }

    Deque<LocationRecording> m_locationHistory          = new ArrayDeque<> ();
    Deque<Long>              m_predictedTimeHistory     = new ArrayDeque<> ();
    Deque<Long>              m_predictedAccuracyHistory = new ArrayDeque<> ();
    Deque<Double>            m_timeScore                = new ArrayDeque<> ();
    Deque<Double>            m_accuracyScore            = new ArrayDeque<> ();
    Deque<Double>            m_safetyScore              = new ArrayDeque<> ();

    public MissionState (ModelReference model) {m_model = model;}

    public ModelReference getModelReference () {
        return m_model;
    }

    public void setCurrentLocation (double x, double y) {
        LocationRecording l = new LocationRecording ();
        l.m_x = x;
        l.m_y = y;
        l.m_time = new Date ().getTime ();
        m_locationHistory.push (l);
    }

    public LocationRecording getCurrentLocation () {
        return m_locationHistory.peek ().copy ();
    }

    public MissionState copy () {
        MissionState s = new MissionState (m_model);
        s.m_locationHistory = new ArrayDeque<> (m_locationHistory);
        return s;
    }

}
