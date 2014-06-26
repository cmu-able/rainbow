package org.sa.rainbow.stitch.tactic.history;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.sa.rainbow.core.error.RainbowCopyException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.stitch.util.ExecutionHistoryData;

public class ExecutionHistoryModelInstance implements IModelInstance<Map<String, ExecutionHistoryData>> {

    public static final String        EXECUTION_HISTORY_TYPE = "ExecutionHistory";
    Map<String, ExecutionHistoryData> m_tacticHistoryMap;
    private String                    m_name;
    private String                    m_source;

    @Override
    public String getOriginalSource () {
        return m_source;
    }

    public ExecutionHistoryModelInstance (Map<String, ExecutionHistoryData> map, String name, String source) {
        m_name = name;
        m_source = source;
        m_tacticHistoryMap = map;
    }

    @Override
    public Map<String, ExecutionHistoryData> getModelInstance () {
        return m_tacticHistoryMap;
    }

    @Override
    public void setModelInstance (Map<String, ExecutionHistoryData> model) {
        m_tacticHistoryMap = model;
    }

    @Override
    public IModelInstance<Map<String, ExecutionHistoryData>> copyModelInstance (String newName)
            throws RainbowCopyException {
        Map<String, ExecutionHistoryData> n = new HashMap<> ();
        for (Entry<String, ExecutionHistoryData> e : m_tacticHistoryMap.entrySet ()) {
            n.put (e.getKey (), new ExecutionHistoryData (e.getValue ()));
        }
        return new ExecutionHistoryModelInstance (n, newName, null);
    }

    @Override
    public String getModelType () {
        return EXECUTION_HISTORY_TYPE;
    }

    @Override
    public String getModelName () {
        return m_name;
    }

    public void setModelName (String name) {
        m_name = name;
    }

    @Override
    public ExecutionHistoryCommandFactory getCommandFactory () {
        return new ExecutionHistoryCommandFactory (this);
    }

    @Override
    public void setOriginalSource (String source) {
        m_source = source;
    }

    @Override
    public void dispose () throws RainbowException {
    }

    public void markDisruption (double level) {

    }


}