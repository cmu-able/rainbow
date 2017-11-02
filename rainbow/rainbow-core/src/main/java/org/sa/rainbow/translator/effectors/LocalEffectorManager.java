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
package org.sa.rainbow.translator.effectors;

import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.models.EffectorDescription;
import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.util.Util;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalEffectorManager extends AbstractRainbowRunnable {

    protected static final String ID = "Local Effector Manager";
    @SuppressWarnings("FieldCanBeLocal")
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
        m_id2Effectors = new HashMap<> ();
        for (EffectorAttributes effAttr : effectors.effectors) {
            // Ignore any effectors that shouldn't start on this machine
            // This should not happen
            if (!effAttr.getLocation().equals (Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_DEPLOYMENT_LOCATION))) {
                continue;
            }
            IEffector effector = null;
            // prepare class info
            String effectorClass;
            Class<?>[] params;
            Object[] args;
            // collect argument values
            String refId = Util.genID (effAttr.name, effAttr.getLocation());
            switch (effAttr.getKind ()) {
            case SCRIPT:
                // get info for a script based effector
                String path = effAttr.getInfo().get ("path");
                String argument = effAttr.getInfo().get ("argument");
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
                effectorClass = effAttr.getInfo().get ("class");
                List<Class<?>> paramList = new ArrayList<> ();
                List<Object> argsList = new ArrayList<> ();
                paramList.add (String.class); // id
                paramList.add (String.class); // name
                argsList.add (refId);
                argsList.add (effAttr.name);
                if (effAttr.getArrays().size () > 0) {
                    // get list of arguments for a pure Java effector
                    for (Object vObj : effAttr.getArrays().values ()) {
                        paramList.add (vObj.getClass ());
                        argsList.add (vObj);
                    }
                }
                params = paramList.toArray (new Class<?>[paramList.size ()]);
                args = argsList.toArray ();
                if (effectorClass != null) {
                    try {
                        Class<?> effectorClazz = Class.forName (effectorClass);
                        Constructor<?> cons = effectorClazz.getConstructor (params);
                        effector = (IEffector )cons.newInstance (args);
                        m_reportingPort.info (getComponentType (), "Java-based IEffector " + effAttr.name);
                    } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
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
    public RainbowComponentT getComponentType () {
        return RainbowComponentT.EFFECTOR_MANAGER;
    }

}
