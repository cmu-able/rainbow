package org.sa.rainbow.util;

/**
 * This class tracks beacon information and provides a convenient method for
 * checking if the beacon has expired.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class Beacon {

    /** The factor multiple of beacon period used to determine expiration */
    public static final int EXPIRY_FACTOR = 10;

    private long m_beaconPer = 0L;
    private long m_lastBeacon = 0L;

    /**
     * Default Constructor, would depend on period to be set later.
     */
    public Beacon () {
    }

    /**
     * Main Constructor that takes a beacon period.
     */
    public Beacon (long beaconPer) {
        setPeriod(beaconPer);
    }

    /**
     * Returns the Beacon period against which to check for expiration.
     * @return long  the defined period in milliseconds
     */
    public long period () {
        return m_beaconPer;
    }

    /**
     * Sets the new Beacon period
     * @param newPer  the new defined Beacon period in milliseconds
     */
    public void setPeriod (long newPer) {
        m_beaconPer = newPer;
    }

    /**
     * Marks the current time as the beacon receipt time, and returns the
     * last beacon time.
     * @return long  the timestamp, in milliseconds, of the previous beacon receipt
     */
    public long mark () {
        m_lastBeacon = System.currentTimeMillis();
        return m_lastBeacon;
    }

    /**
     * Returns the amount of time elapsed since last beacon mark until now.
     * If beacon has not yet been marked, then a zerio is returned.
     * @return long  the amount of elapsed time in milliseconds since the previou mark
     */
    public long elapsedTime () {
        return (m_lastBeacon == 0L) ? 0L : (System.currentTimeMillis() - m_lastBeacon);
    }

    /**
     * Returns whether the beacon period has been expired.  A beacon period is
     * considered expired if the following three conditions are both true: <ol>
     * <li> A beacon period was set (a beacon period of 0 is not very useful)
     * <li> A first beacon has been marked, i.e., last beacon != 0
     * <li> Elapsed time is EXPIRY_FACTOR that of the expected beacon period 
     * </ol>
     * @return boolean  <code>true</code> if beacon started, but
     *     {@linkplain EXPIRY_FACTOR} the expected period has elapsed;
     *     <code>false</code> otherwise
     */
    public boolean isExpired () {
        return (m_beaconPer == 0L) ? false : (EXPIRY_FACTOR*m_beaconPer < elapsedTime());
    }

    /**
     * Returns whether the beacon period has elapsed, meaning, since the last
     * marked time in milliseconds, the beacon-period amount of time has passed.
     * @return boolean  <code>true</code> if beacon started, and period has
     *     elapsed since last mark; <code>false</code> otherwise
     */
    public boolean periodElapsed () {
        long elapsedTime = elapsedTime();
        return (m_beaconPer == 0L) ? false : (m_beaconPer < elapsedTime);
    }

}
