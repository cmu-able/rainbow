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
package org.sa.rainbow.model.acme;

import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.core.exception.AcmeException;
import org.acmestudio.acme.core.resource.IAcmeLanguageHelper;
import org.acmestudio.acme.core.resource.RegionManager;
import org.acmestudio.acme.core.type.IAcmeFloatType;
import org.acmestudio.acme.core.type.IAcmeFloatingPointValue;
import org.acmestudio.acme.core.type.IAcmeIntType;
import org.acmestudio.acme.core.type.IAcmeIntValue;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmeElementInstance;
import org.acmestudio.acme.element.IAcmeElementType;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.element.property.IAcmePropertyBearer;
import org.acmestudio.acme.element.property.IAcmePropertyValue;
import org.acmestudio.acme.environment.error.AcmeError;
import org.acmestudio.acme.model.IAcmeModel;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmeCompoundCommand;
import org.acmestudio.acme.model.command.IAcmeElementCopyCommand;
import org.acmestudio.acme.model.event.AcmeEventListenerAdapter;
import org.acmestudio.acme.model.event.AcmePropertyEvent;
import org.acmestudio.acme.rule.node.IExpressionNode;
import org.acmestudio.acme.type.AcmeTypeHelper;
import org.acmestudio.acme.type.verification.NodeScopeLookup;
import org.acmestudio.acme.type.verification.RuleTypeChecker;
import org.acmestudio.standalone.resource.StandaloneResourceProvider;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.error.RainbowAbortException;
import org.sa.rainbow.core.error.RainbowCopyException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

public abstract class AcmeModelInstance implements IModelInstance<IAcmeSystem> {

    private final Logger LOGGER = Logger.getLogger (this.getClass ());
    private static final String EXP_AVG_KEY = "[EAvg]";
    private static final String PENALTY_KEY = "[Penalty]";
    private static final String EXPR_KEY = "[EXPR]";

    /** The property identifier for obtaining the deployment location of an element */
    private static final String PROPKEY_LOCATION = "deploymentLocation";
    /** The property identifier for determining whether an element is in the architecture or the env't */
    public static final String PROPKEY_ARCH_ENABLED = "isArchEnabled";
    private static final String PROPKEY_HTTPPORT = "httpPort";

    private IAcmeSystem                    m_system;
    /** Map of qualified name to average values */
    private final Map<String, Double> m_propExpAvg = new HashMap<> ();
    /** Map of additional, non-model properties */
    private final Map<String, Double> m_moreProp = new HashMap<> ();
    private final Map<String, IExpressionNode> m_registeredExpressions = new HashMap<> ();
    private final Properties m_opMap;
    private String                         m_source;

    /** Map of paths to IAcmeResources to make sure the same is retrieved */

    public AcmeModelInstance (IAcmeSystem system, String source) {
        setModelInstance (system);
        setOriginalSource (source);
        // TODO: This may need to change to either be in the model already, or to make several op maps possible
        m_opMap = new Properties ();
        File opPath = Util.getRelativeToPath (new File (m_source).getParentFile (), "op.map");
        try {
            FileInputStream fis = new FileInputStream (opPath);
            m_opMap.load (fis);
            fis.close ();
        }
        catch (IOException e) {
            LOGGER.warn (MessageFormat
                    .format ("Could not find operator map ''{0}''. Adaptation of this model will fail.", opPath));
        }

    }

    @Override
    public String getModelType () {
        return "Acme";
    }

    @Override
    public IAcmeSystem getModelInstance () {
        return m_system;
    }

    @Override
    public void setOriginalSource (String source) {
        m_source = source;
    }

    @Override
    public String getOriginalSource () {
        return m_source;
    }

    @Override
    public void setModelInstance (IAcmeSystem model) {
        m_system = model;
        // Add listener to update exponential averages for the properties
        final AcmeEventListenerAdapter propertyListener = new AcmeEventListenerAdapter () {
            @SuppressWarnings ("rawtypes")
            @Override
            public void propertyValueSet (AcmePropertyEvent event) {
                try {
                    IAcmePropertyBearer parent = event.getPropertyBearer ();
                    if (parent instanceof IAcmeElementInstance)
                        if (ModelHelper.getAcmeSystem ((IAcmeElementInstance )event.getPropertyBearer ()) == m_system)
                            if (event.getProperty ().getType () instanceof IAcmeFloatType) {
                                updateExponentialAverage (event.getProperty ().getQualifiedName (), PropertyHelper.toJavaVal ((IAcmeFloatingPointValue) event.getProperty ().getValue ()));
                            }
                            else if (event.getProperty ().getType () instanceof IAcmeIntType) {
                                updateExponentialAverage (event.getProperty ().getQualifiedName (), PropertyHelper.toJavaVal ((IAcmeIntValue) event.getProperty ().getValue ()));
                            }
                }
                catch (RainbowAbortException e) {
                    m_system.getContext ().getModel ().removeEventListener (this);
                }

            }
        };
        m_system.getContext ().getModel ().addEventListener (propertyListener);

    }

    /**
     * Updates the exponential average associated with a property in the model (Currently, this is not stored in the
     * Acme model.)
     * 
     * @param id
     * @param val
     */
    private void updateExponentialAverage (String id, double val) {
        double avg = 0.0;
        // retrieve exponential alpha
        double alpha = Rainbow.getProperty (RainbowConstants.PROPKEY_MODEL_ALPHA, .3);
        if (m_propExpAvg.containsKey (id)) {
            avg = m_propExpAvg.get (id);
            avg = (1 - alpha) * avg + alpha * val;
        }
        else {
            avg = val;
        }
        if (LOGGER.isTraceEnabled ()) {
            LOGGER.trace (MessageFormat.format ("(iden,val,alpha,avg) == ({0},{1},{2},{3})", id, val, alpha, avg));
        }
        m_propExpAvg.put (id, avg);
    }

    @Override
    public void dispose () throws RainbowException {
        synchronized (m_system) {
            try {
                m_system.getContext ().getModel ().dispose ();
//                m_system.getCommandFactory ().systemDeleteCommand (m_system).execute ();
            }
            catch (IllegalStateException e) {
                RainbowException exc = new RainbowException ("Failed to remove " + m_system.getName ());
                exc.addSuppressed (e);
                throw exc;
            }
        }
    }

    @Override
    public IModelInstance<IAcmeSystem> copyModelInstance (String newName) throws RainbowCopyException {
        synchronized (m_system) {
            List<IAcmeCommand<?>> cmds = new LinkedList<> ();
            IAcmeModel model = m_system.getContext ().getModel ();
            IAcmeElementCopyCommand cmd = model.getCommandFactory ().copyElementCommand (model, m_system);
            cmds.add (cmd);
            cmds.add (model.getCommandFactory ().elementRenameCommand (cmd, newName));
// This is commented out because it doesn't quite work. The strategy scope establisher binds
// binds types before tactic execution, and so during tactic execution
// the copy of the model refers to a different type
//            
//            // Copy all families into this system, whether imported or local
//            Set<IAcmeFamily> types = ModelHelper.gatherSuperFamilies (m_system);
//            types.remove (DefaultAcmeModel.defaultFamily ());
//            for (IAcmeFamily f : types) {
//                cmds.add (model.getCommandFactory ().copyElementCommand (model, f));
//            }
//
//            // Copy the system itself
//            IAcmeElementCopyCommand cmd = model.getCommandFactory ().copyElementCommand (model, m_system);
//            cmds.add (cmd);
//            cmds.add (model.getCommandFactory ().elementRenameCommand (cmd, newName));
            // Execute the command to create a new system
            try {
                if (cmds.size () == 1)
                    return generateInstance ((IAcmeSystem )cmd.execute ());
                else {
                    IAcmeCompoundCommand cc = model.getCommandFactory ().compoundCommand (cmds);
                    List<Object> execute = cc.execute ();

                    return generateInstance ((IAcmeSystem )execute.get (execute.size () - 1));
                }
            }
            catch (IllegalStateException | AcmeException e) {
                RainbowCopyException exc = new RainbowCopyException (
                        MessageFormat.format ("Could not copy Acme system {0}", m_system.getName ()));
                exc.addSuppressed (e);
                throw exc;
            }
        }
    }

    @Override
    public String getModelName () {
        return m_system.getName ();
    }

    protected abstract AcmeModelInstance generateInstance (IAcmeSystem sys);

    @Override
    public abstract AcmeModelCommandFactory getCommandFactory ();

    /////////////////////////////////////////////////////////////////////////////////////
    // Convenience functions for getting various Acme properties
    ////////////////////////////////////////////////////////////////////////////////////
    public Object getProperty (String id) {
        return internalGetProperty (id, 0);
    }

    public Object predictProperty (String id, long dur) {
        return internalGetProperty (id, dur);
    }

    public String getStringProperty (String id) {
        String propVal = null;
        IAcmeProperty prop = (IAcmeProperty )internalGetProperty (id, 0);
        Object javaVal = PropertyHelper.toJavaVal (prop.getValue ());
        if (javaVal instanceof String) {
            propVal = (String )javaVal;
        }
        return propVal;
    }

    /**
     * Gets current or predicted property, depending on whether prediction is enabled and future duration is greater
     * than 0.
     * 
     * @param id
     *            the identifier fo the property to get value for
     * @param dur
     *            duration into the future to predict value, if applicable
     * @return the Object that represents the value of the sought property.
     */
    private Object internalGetProperty (String id, long dur) {
        if (dur > 0) throw new NotImplementedException ("Prediction is not implemented");
        if (getModelInstance () == null) return null;
        Object prop = null;
        if (id.startsWith (EXP_AVG_KEY)) { // Special treatment for exp.avg
            // the property id is expected to be of the form <elem-type>.<prop>
            int idxStart = EXP_AVG_KEY.length ();
            int idxDot = id.indexOf (".");
            if (idxDot == -1) { // property ID is not of expected form
                LOGGER.error ("Unrecognized form of Average Property Name!" + id);
                return null;
            }
            String typeName = id.substring (idxStart, idxDot);
            String propName = id.substring (idxDot + 1);

            // algorithm:
            // 1. find props of all instances of the specified type
            Set<String> propKeys = collectInstanceProps (typeName, propName);
            if (propKeys.size () > 0) {
                // 2. collec the exp.avg values
                double sum = 0.0;
                for (String k : propKeys) {
//                    if (Rainbow.predictionEnabled () && dur > 0L)
//                        // grab predicted value from "target system"
//                        throw new NotImplementedException ("Prediction is not implemented");
//                        sum += (Double) Oracle.instance().targetSystem()
//                                .predictProperty(k, dur, StatType.SINGLE);
//                    else {
                    sum += m_propExpAvg.get (k);
//                    }
                }
                // 3. take the mean over these exp.avg values
                prop = sum / propKeys.size ();
            }
            if (LOGGER.isTraceEnabled ()) {
                LOGGER.trace ("ExpAvg Prop " + id + (dur > 0 ? "(+" + dur + ") " : "") + " requested == " + prop);
            }
        }
        else if (id.startsWith (PENALTY_KEY)) {
            prop = m_moreProp.get (id);
        }
        else if (id.startsWith (EXPR_KEY)) {
            IExpressionNode expr = m_registeredExpressions.get (id);
            if (expr == null) {
                int idxStart = EXPR_KEY.length ();
                String exprStr = id.substring (idxStart);
                IAcmeLanguageHelper helper = StandaloneResourceProvider.instance ()
                        .languageHelperForResource (getModelInstance ().getContext ());
                try {
                    expr = helper.designRuleExpressionFromString (exprStr, new RegionManager ());
                    m_registeredExpressions.put (id, expr);
                }
                catch (Exception e) {
                    LOGGER.error ("Could not parse expression: " + id, e);
                }
            }
            try {
                Object any = RuleTypeChecker.evaluateAsFloat (getModelInstance (), null, expr, new Stack<AcmeError> (),
                        new NodeScopeLookup ());
                if (any instanceof IAcmePropertyValue) return PropertyHelper.toJavaVal ((IAcmePropertyValue )any);
                return any;
            }
            catch (AcmeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace ();
            }
        }
        else {
            IAcmeModel model = getModelInstance ().getContext ().getModel ();
            prop = model.findNamedObject (model, id);
        }
        return prop;
    }

    /**
     * Iterates through model's systems to collect all instances that either declares or instantiates the specified type
     * name, then for all instances found that also have the specified property names, collect the fully qualified
     * property names
     * 
     * @param typeName
     *            the specified element type name
     * @param propName
     *            the specified property name within relevant instance
     * @return the set of qualified property names
     */
    private Set<String> collectInstanceProps (String typeName, String propName) {
        Set<String> propKeys = new HashSet<> ();
        boolean useSatisfies = false;
        if (typeName.startsWith ("!")) {
            typeName = typeName.substring (1);
            useSatisfies = true;
        }
        // Object elemObj = m_acme.findNamedObject(m_acme, typeName);
        // if (! (elemObj instanceof IAcmeElementType<?,?>)) {
        // Debug.errorln("Element type of Average Property requested NOT found! "
        // + typeName);
        // return propKeys;
        // }
        IAcmeSystem sys = getModelInstance ();
        Set<IAcmeElementInstance<?, ?>> children = new HashSet<> ();
        children.addAll (sys.getComponents ());
        children.addAll (sys.getConnectors ());
        children.addAll (sys.getPorts ());
        children.addAll (sys.getRoles ());
        for (IAcmeElementInstance<?, ?> child : children) {

            // seek element with specified type AND specified property
            if ((useSatisfies && AcmeTypeHelper.satisfiesElementType (child,
                    ((IAcmeElementType<?, ?> )child.lookupName (typeName, true)), null))
                    || child.declaresType (typeName) || child.instantiatesType (typeName)) {
                IAcmeProperty childProp = child.getProperty (propName);
                if (childProp != null) {
                    String qName = childProp.getQualifiedName ();
                    if (m_propExpAvg.containsKey (qName)) {
                        propKeys.add (qName);
                    }
                }
            }
        }
        return propKeys;
    }

    public void markDisruption (double level) {
        m_moreProp.put (PENALTY_KEY + "Disruption", level);
        LOGGER.info ("X_X disruption marked: " + level);
    }

    public String getGenericOperatorName (String archOpName) {
        return m_opMap.getProperty (archOpName);
    }

    public String getElementLocation (IAcmeElementInstance<?, ?> element) {
        String location = null;
        if (ModelHelper.getAcmeSystem (element) != m_system) return location;
        IAcmeProperty prop = element.getProperty (PROPKEY_LOCATION);
        if (prop.getValue () != null) {
            location = PropertyHelper.toJavaVal (prop.getValue ()).toString ().toLowerCase ();
        }
        return location;
    }

    public IAcmeComponent getElementForLocation (String location, String type) {
        String[] parts = location.split (":");
        Set<? extends IAcmeComponent> components = m_system.getComponents ();
        for (IAcmeComponent comp : components) {
            if (comp.declaresType (type)) {
                IAcmeProperty prop = comp.getProperty (PROPKEY_LOCATION);
                if (prop != null && prop.getValue () != null) {
                    if (PropertyHelper.toJavaVal (prop.getValue ()).equals (parts[0])) {
                        if (parts.length == 2) {
                            prop = comp.getProperty (PROPKEY_HTTPPORT);
                            if (prop != null && prop.getValue () != null) {
                                if (PropertyHelper.toJavaVal (prop.getValue ()).equals (parts[1])) return comp;
                            }
                        }
                        else
                            return comp;
                    }
                }
            }
        }
        return null;
    }

    public <T> T resolveInModel (String qname, Class<T> clazz) throws RainbowModelException {
        // The model is an Acme System, but the qname could include the Acme system.
        String[] names = qname.split ("\\.");
        if (names[0].equals (getModelInstance ().getName ())) {
            qname = qname.substring (qname.indexOf ('.') + 1);
        }
        Object resolve = getModelInstance ().lookupName (qname);
        if (resolve == null || !(clazz.isInstance (resolve))) throw new RainbowModelException (
                MessageFormat.format ("Cannot find the ''{0}'' in the model as a {1}", qname, clazz.getName ()));

        @SuppressWarnings ("unchecked")
        T lb = (T )resolve;
        return lb;
    }
}
