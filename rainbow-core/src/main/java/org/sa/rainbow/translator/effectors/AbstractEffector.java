/**
 * Created November 4, 2006.
 */
package org.sa.rainbow.translator.effectors;

import java.util.List;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.ports.IEffectorLifecycleBusPort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;

/**
 * Abstract definition of the effector with common methods to simplify
 * effector implementations.  Note that this is NOT a runnable since a
 * RainbowDelegate thread or a Strategy Executor thread is expected to execute
 * the effector.
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public abstract class AbstractEffector implements IEffector {

    protected Logger LOGGER = Logger.getLogger (this.getClass ());
    private String m_id = null;
    private String m_name = null;
    private Kind m_kind = null;

    protected IRainbowReportingPort m_reportingPort;
    protected IEffectorLifecycleBusPort m_effectorManagementPort;
    private IEffectorExecutionPort    m_executionPort;


    /**
     * Main Constructor that subclass should call.
     * @param refID  the location-unique reference identifier to match this IEffector
     * @param name   the name used to label this IEffector
     * @param kind   the implementation type of this IEffector
     */
    public AbstractEffector (String refID, String name, Kind kind) {
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

    protected void log (String txt) {
        String msg = "E[" + service() + "] " + txt;
        if (m_reportingPort != null) {
            m_reportingPort.info (RainbowComponentT.EFFECTOR, msg);
        }
        // avoid duplicate output in the master's process
        if (!Rainbow.isMaster () || m_reportingPort == null) {
            LOGGER.info (msg);
        }
    }

    protected void reportExecuted (Outcome r, List<String> args) {
        m_effectorManagementPort.reportExecuted (this, r, args);
    }

}
