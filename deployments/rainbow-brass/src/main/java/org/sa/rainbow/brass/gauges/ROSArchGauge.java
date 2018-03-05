package org.sa.rainbow.brass.gauges;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.acmestudio.acme.model.util.UMSystem;
import org.acmestudio.standalone.resource.StandaloneLanguagePackHelper;
import org.sa.rainbow.brass.gauges.acme.ROSToAcmeTranslator;
import org.sa.rainbow.brass.gauges.acme.ROSToAcmeTranslator.IncompleteCommandsException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.AbstractGaugeWithProbes;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.translator.probes.IProbeIdentifier;

public class ROSArchGauge extends AbstractGaugeWithProbes {

    public static final String NAME = "ROS Architecture Gauge";

    // m_report needs to be synchronized because it is read and written by multiple threads
    protected String  m_report    = "";
    protected boolean m_newReport = false;

    private UMSystem m_currentSystem = null;

    private ROSToAcmeTranslator m_trans;

    synchronized void setReport (String report) {
        m_report = report;
        m_newReport = true;
    }

    synchronized String getReport () {
        return m_report;
    }

    synchronized boolean checkAnSetNewReport () {
        if (m_newReport) {
            m_newReport = false;
            return true;
        }

        return false;
    }

    public ROSArchGauge (String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc,
            List<TypedAttributeWithValue> setupParams, Map<String, IRainbowOperation> mappings)
                    throws RainbowException {
        super (NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);
    }

    @Override
    public void reportFromProbe (IProbeIdentifier probe, String data) {
        super.reportFromProbe (probe, data);
        setReport (data);
    }

    @Override
    protected void runAction () {
        super.runAction ();
        String data = "";
        // Only report new on new report
        synchronized (this) {
            if (!checkAnSetNewReport ()) return;
            data = getReport ();
        }
        List<IRainbowOperation> ops = new LinkedList<> ();
        List<Map<String, String>> params = new LinkedList<> ();
        if (m_currentSystem == null) {
            m_currentSystem = m_trans.processROSDataToNewSystem (data);
            IRainbowOperation op = m_commands.get ("newSystem");
            Map<String, String> p = new HashMap<> ();
            p.put ("system",
                    StandaloneLanguagePackHelper.defaultLanguageHelper ().elementToString (m_currentSystem, null));
            issueCommand (op, p);
        }
        else {
            try {
                m_trans.processROSDataToUpdateSystem (data, m_currentSystem, ops, params);
            }
            catch (IncompleteCommandsException e) {
                reportingPort ().error (getComponentType (),
                        "Could not do incremental update due to incomplete commands");
            }
        }
    }

    @Override
    public boolean configureGauge (List<TypedAttributeWithValue> configParams) {
        boolean b = super.configureGauge (configParams);
        try {
            m_trans = new ROSToAcmeTranslator (m_commands);
            return b;
        }
        catch (IncompleteCommandsException e) {
            m_reportingPort.error (getComponentType (),
                    "Not all commands were present in the gauge specification, so won't be able to do incremental updates.");
            m_trans = new ROSToAcmeTranslator ();

            return false;
        }
    }

}
