/*
 * The MIT License
 *
 * Copyright 2014 CMU ABLE Group.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
/**
 * Created November 1, 2006.
 */
package org.sa.rainbow.translator.znn.gauges;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.RegularPatternGauge;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gauge for consuming Apache Top's monitoring output.
 * 
 * <b>This is no longer used in the experiments.</b>
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class ApacheTopGauge extends RegularPatternGauge {

    public static final String NAME = "G - Apache Top";

    /** List of values reported by this Gauge */
    private static final String[] valueNames = {
            "reqServiceRate",
            "byteServiceRate",
            "numReqsSuccess",
            "numReqsRedirect",
            "numReqsClientError",
            "numReqsServerError",
            "pageHit"
    };
    private static final String LAST_HIT = "LAST_HIT";
    private static final String RUNTIME = "RUNTIME";
    private static final String CURTIME = "CURTIME";
    private static final String ALL_HITS = "ALL_HITS";
    private static final String RECENT_HITS = "RECENT_HITS";
    private static final String ALL_RC = "ALL_RC";
    private static final String RECENT_RC = "RECENT_RC";
    private static final String RC = "RC";
    private static final String HIT = "HIT";

    /**
     * Main constructor.
     * 
     * @throws RainbowException
     */
    public ApacheTopGauge (String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc,
            List<TypedAttributeWithValue> setupParams, Map<String, IRainbowOperation> mappings)
                    throws RainbowException {

        super(NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);

        addPattern(LAST_HIT, Pattern.compile("last hit: (\\d{2}+):(\\d{2}+):(\\d{2}+)"));
        addPattern(RUNTIME, Pattern.compile("atop runtime: (\\d{2}+) days, (\\d{2}+):(\\d{2}+):(\\d{2}+)"));
        addPattern(CURTIME, Pattern.compile("curtime: (\\d{2}+):(\\d{2}+):(\\d{2}+)"));
        addPattern(ALL_HITS, Pattern.compile("All: (\\d{12}+) reqs \\(([0-9.]{6}+)/sec\\) ([0-9.]{11}+)(\\w) \\(([0-9.]{7}+)(\\w)/sec\\) ([0-9.]{7}+)(\\w)/req"));
        addPattern(RECENT_HITS, Pattern.compile("R\\((\\d{3}+)s\\): (\\d{7}+) reqs \\(([0-9.]{6}+)/sec\\) ([0-9.]{11}+)(\\w) \\(([0-9.]{7}+)(\\w)/sec\\) ([0-9.]{7}+)(\\w)/req"));
        addPattern(ALL_RC, Pattern.compile("All: 2xx: (\\d{7}+) \\(([0-9.]{4}+)%\\) 3xx: (\\d{7}+) \\(([0-9.]{4}+)%\\) 4xx: (\\d{5}+) \\(([0-9.]{4}+)%\\) 5xx: (\\d{5}+) \\(([0-9.]{4}+)%\\)"));
        addPattern(RECENT_RC, Pattern.compile("R: 2xx: (\\d{7}+) \\(([0-9.]{4}+)%\\) 3xx: (\\d{7}+) \\(([0-9.]{4}+)%\\) 4xx: (\\d{5}+) \\(([0-9.]{4}+)%\\) 5xx: (\\d{5}+) \\(([0-9.]{4}+)%\\)"));
        addPattern(RC, Pattern.compile("R: (\\d{5}+) ([0-9.]{5}+) ([0-9.]{5}+) ([0-9.]{4}+) (.*+)"));
        addPattern(HIT, Pattern.compile("H: (\\d{5}+) ([0-9.]{5}+) ([0-9.]{5}+) ([0-9.]{4}+) (.+?)( \\[(.+)\\])?"));
    }


    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.RegularPatternGauge#doMatch(java.lang.String, java.util.regex.Matcher)
     */
    @Override
    protected void doMatch (String matchName, Matcher m) {
        //log(" - line matches pattern \"" + p + "\" with " + m.groupCount() + " groups");
        if (matchName == CURTIME) {
            // should we remember the report time?
        } else if (matchName == RECENT_HITS) {
            // acquire the recent hits data
            int numSecs = Integer.parseInt(m.group(1));
            int numReqs = Integer.parseInt(m.group(2));
            double numBytes = Double.parseDouble(m.group(4));
            char unit = m.group(5).charAt(0);
            // convert to kBps
            switch (unit) {
            case 'G':
                numBytes *= 1024;
                // intentional fall thru
            case 'M':
                numBytes *= 1024;
                // intentional fall thru
            case 'K':
                break;
            case 'B':
                numBytes /= 1024;
                break;
            }
            if (numReqs > 0 && numBytes > 0) {
                // update server comp in model with requests per sec
                m_reportingPort.trace (getComponentType (), "Updating server prop using (sec,req,kBps) = (" + numSecs
                        + "," + numReqs + "," + numBytes + ")");
                // ZNewsSys.s0.reqServiceRate
                IRainbowOperation reqServiceRateCmd = getCommand (valueNames[0]);
                Map<String, String> pMap = new HashMap<> ();
                pMap.put (reqServiceRateCmd.getParameters ()[0], Double.toString ((double )numReqs / numSecs));
                issueCommand (reqServiceRateCmd, pMap);
                // ZNewsSys.s0.byteServiceRate
                IRainbowOperation byteServiceRateCmd = getCommand (valueNames[0]);
                pMap = new HashMap<> ();
                pMap.put (reqServiceRateCmd.getParameters ()[0], Double.toString (numBytes / numSecs));
                issueCommand (byteServiceRateCmd, pMap);
            }
        } else if (matchName == RECENT_RC) {
            int num2x = Integer.parseInt(m.group(1));
            int num3x = Integer.parseInt(m.group(3));
            int num4x = Integer.parseInt(m.group(5));
            int num5x = Integer.parseInt(m.group(7));
            if (num2x + num3x + num4x + num5x > 0) {
                // update http conn in model with requests per sec
                m_reportingPort.trace (getComponentType (),"Updating server prop using (2xx,3xx,4xx,5xx) = ("
                        + num2x + "," + num3x + "," + num4x + "," + num5x + ")");
                // ZNewsSys.conn0.numReqsSuccess

                IRainbowOperation numReqsSuccessCmd = getCommand (valueNames[2]);
                Map<String, String> pMap = new HashMap<> ();
                pMap.put (numReqsSuccessCmd.getParameters ()[0], Integer.toString (num2x));
                issueCommand (numReqsSuccessCmd, pMap);
                IRainbowOperation numReqsRedirect = getCommand (valueNames[3]);
                pMap = new HashMap<> ();
                pMap.put (numReqsRedirect.getParameters ()[0], Integer.toString (num3x));
                issueCommand (numReqsRedirect, pMap);
                IRainbowOperation numReqsClientError = getCommand (valueNames[4]);
                pMap = new HashMap<> ();
                pMap.put (numReqsClientError.getParameters ()[0], Integer.toString (num4x));
                issueCommand (numReqsClientError, pMap);
                IRainbowOperation numReqsServerError = getCommand (valueNames[5]);
                pMap = new HashMap<> ();
                pMap.put (numReqsServerError.getParameters ()[0], Integer.toString (num5x));
                issueCommand (numReqsServerError, pMap);

            }
        } else if (matchName == HIT) {
            int hitCnt = Integer.parseInt(m.group(1));
//			double rps = Double.parseDouble(m.group(2));
            double kB = Double.parseDouble(m.group(3));
//			double kbps = Double.parseDouble(m.group(4));
            String uri = m.group(5);
//			String ip = m.group(7);  // 6th is the "optional" grouping
            String hitStr =
                    "[ uri:string=\"" + uri + "\"; cnt:int=" + hitCnt
                    + "; kbytes:float=" + kB + "; ]";

            IRainbowOperation lastPageHit = getCommand (valueNames[6]);
            Map<String, String> pMap = new HashMap<> ();
            pMap.put (lastPageHit.getParameters ()[0], hitStr);
            issueCommand (lastPageHit, pMap);
        }
    }


}
