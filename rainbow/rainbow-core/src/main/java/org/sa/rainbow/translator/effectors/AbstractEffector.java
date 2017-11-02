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
 * Created November 4, 2006.
 */
package org.sa.rainbow.translator.effectors;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.ports.IEffectorLifecycleBusPort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;

import java.util.List;

/**
 * Abstract definition of the effector with common methods to simplify
 * effector implementations.  Note that this is NOT a runnable since a
 * RainbowDelegate thread or a Strategy Executor thread is expected to execute
 * the effector.
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public abstract class AbstractEffector implements IEffector {

    final Logger LOGGER = Logger.getLogger (this.getClass ());

    private String m_id = null;

    private String m_name = null;

    private Kind m_kind = null;


    private IRainbowReportingPort m_reportingPort;

    private IEffectorLifecycleBusPort m_effectorManagementPort;

    private IEffectorExecutionPort    m_executionPort;


    /**
     * Main Constructor that subclass should call.
     * @param refID  the location-unique reference identifier to match this IEffector
     * @param name   the name used to label this IEffector
     * @param kind   the implementation type of this IEffector
     */
    protected AbstractEffector (String refID, String name, Kind kind) {
        m_id = refID;
        m_name = name;
        m_kind = kind;

        try {
            m_effectorManagementPort = RainbowPortFactory.createEffectorSideLifecyclePort ();
            m_executionPort = RainbowPortFactory.createEffectorExecutionPort (this);

            m_effectorManagementPort.reportCreated (this);

            log ("+ " + refID);
        }
        catch (RainbowConnectionException e) {
            LOGGER.error ("Could not create " + refID, e);
        }

    }

    @Override
    public void setReportingPort (IRainbowReportingPort reportginPort) {
        m_reportingPort = reportginPort;
    }
    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.effectors.IEffector#id()
     */

    @Override
    public String id () {
        return m_id;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.effectors.IEffector#service()
     */

    @Override
    public String service () {
        return m_name;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.effectors.IEffector#type()
     */

    @Override
    public Kind kind() {
        return m_kind;
    }

    void log (String txt) {
        String msg = "E[" + service() + "] " + txt;
        if (m_reportingPort != null) {
            m_reportingPort.info (RainbowComponentT.EFFECTOR, msg);
        }
        // avoid duplicate output in the master's process
        if (!Rainbow.instance ().isMaster () || m_reportingPort == null) {
            LOGGER.info (msg);
        }
    }

    void reportExecuted (Outcome r, List<String> args) {
        m_effectorManagementPort.reportExecuted (this, r, args);
    }

    @Override
    public void dispose () {
        m_reportingPort.dispose ();
        m_effectorManagementPort.dispose ();
        m_executionPort.dispose ();
        m_reportingPort = null;
        m_effectorManagementPort = null;
        m_executionPort = null;
    }

}
