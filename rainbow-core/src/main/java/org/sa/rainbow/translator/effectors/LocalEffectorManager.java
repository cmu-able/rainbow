package org.sa.rainbow.translator.effectors;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.models.EffectorDescription;
import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.util.Util;

public class LocalEffectorManager extends AbstractRainbowRunnable {

    protected static String        ID = "Local Effector Manager";
    private Map<String, IEffector> m_id2Effectors;

    public LocalEffectorManager (String delegateId) {
        super (ID + "[@" + delegateId + "]");
    }

    @Override
    public void dispose () {
        // TODO Auto-generated method stub

    }

    @Override
    protected void log (String txt) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void runAction () {
        // TODO Auto-generated method stub

    }

    public void initEffectors (EffectorDescription effectors) {
        m_id2Effectors = new HashMap<String, IEffector> ();
        for (EffectorAttributes effAttr : effectors.effectors) {
            // Ignore any effectors that shouldn't start on this machine
            // This should not happen
            if (!effAttr.location.equals (Rainbow.getProperty (RainbowConstants.PROPKEY_DEPLOYMENT_LOCATION))) {
                continue;
            }
            IEffector effector = null;
            // prepare class info
            String effectorClass = null;
            Class<?>[] params = null;
            Object[] args = null;
            // collect argument values
            String refId = Util.genID (effAttr.name, effAttr.location);
            switch (effAttr.kind) {
            case SCRIPT:
                // get info for a script based effector
                String path = effAttr.info.get ("path");
                String argument = effAttr.info.get ("argument");
                if (!new File (path).exists ()) {
                    String msg = MessageFormat.format (
                            "Could not create effector {0} because script does not exist: {1}", refId, path);
                    m_reportingPort.error (RainbowComponentT.EFFECTOR_MANAGER, msg);
                    continue;
                }
                effector = new GenericScriptBasedEffector (refId, effAttr.name, path, argument);
                effector.setReportingPort (m_reportingPort);
                m_reportingPort.info (getComponentType (), "Script-based IEffector " + effAttr.name + ": " + path + " "
                        + argument);
                break;
            case JAVA:
                effectorClass = effAttr.info.get ("class");
                List<Class<?>> paramList = new ArrayList<> ();
                List<Object> argsList = new ArrayList<> ();
                paramList.add (String.class);
                argsList.add (refId);
                if (effAttr.arrays.size () > 0) {
                    // get list of arguments for a pure Java effector
                    for (Object vObj : effAttr.arrays.values ()) {
                        paramList.add (vObj.getClass ());
                        argsList.add (vObj);
                    }
                }
                params = paramList.toArray (new Class<?>[0]);
                args = argsList.toArray ();
                if (effectorClass != null) {
                    try {
                        Class<?> effectorClazz = Class.forName (effectorClass);
                        Constructor<?> cons = effectorClazz.getConstructor (params);
                        effector = (IEffector )cons.newInstance (args);
                        m_reportingPort.info (getComponentType (), "Java-based IEffector " + effAttr.name);
                    }
                    catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
                            | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        String msg = MessageFormat.format ("Could not instantiate IEffector ''{0}''", effectorClass);
                        m_reportingPort.error (RainbowComponentT.EFFECTOR_MANAGER, msg);
                        continue;
                    }
                }
            }

            if (effector != null) {
                m_id2Effectors.put (refId, effector);
                effector.setReportingPort (m_reportingPort);
            }

        }
    }

    @Override
    protected RainbowComponentT getComponentType () {
        return RainbowComponentT.EFFECTOR_MANAGER;
    }

}
