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
 * Created January 31, 2007.
 */
package org.sa.rainbow.util;

import org.ho.yaml.Yaml;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.gauges.GaugeDescription;
import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.gauges.GaugeTypeDescription;
import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.models.*;
import org.sa.rainbow.core.models.UtilityPreferenceDescription.UtilityAttributes;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.translator.effectors.IEffector;
import org.sa.rainbow.translator.probes.IProbe;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * This utility class provides methods for parsing specific Yaml files for Rainbow. The class is non-instantiable.
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public abstract class YamlUtil {

    /**
     * Retrieves the utility definitions, then
     * <ul>
     * <li>store the weights
     * <li>store the utility functions
     * <li>for each tactic, store respective tactic attribute vectors.
     * 
     * @return UtilityPreferenceDescription the data structure of utility definitions.
     */
//    @SuppressWarnings ("unchecked")
//    public static UtilityPreferenceDescription loadUtilityPrefs () {
//        String utilityPath = Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_UTILITY_PATH);
//        return loadUtilityPrefs (utilityPath);
//    }
    public static UtilityPreferenceDescription loadUtilityPrefs (String utilityPath) {
        UtilityPreferenceDescription prefDesc = new UtilityPreferenceDescription ();

        Map<String, Map<String, Map>> utilityDefMap = null;
        try {
            File defFile = new File (utilityPath);
            if (!defFile.exists ()) {
                defFile = Util.getRelativeToPath (Rainbow.instance ().getTargetPath (), utilityPath);
            }
            Object o = Yaml.load (defFile);
            Util.logger ().trace ("Utiltiy Def Yaml file loaded: " + o.toString ());
            utilityDefMap = (Map )o;
        }
        catch (FileNotFoundException e) {
            Util.logger ().error ("Loading Utiltiy Def Yaml file failed!", e);
            utilityDefMap = new HashMap<> ();
        }
        // store associated model
        Map modelMap = utilityDefMap.get ("model");
        // this is optional for backward compatibility
        if (modelMap != null) {
            prefDesc.associatedModel = new ModelReference ((String) modelMap.get ("name"), (String) modelMap.get ("type"));
        }

        // store weights
        Map<String, Map> weightMap = utilityDefMap.get ("weights");
        if (weightMap != null) {
            for (Map.Entry<String, Map> e : weightMap.entrySet ()) {
                Map<String, Double> kvMap = new HashMap<> ();
                double sum = 0.0;
                for (Object k : e.getValue ().keySet ()) {
                    Object v = e.getValue ().get (k);
                    if (k instanceof String && v instanceof Number) {
                        kvMap.put ((String )k, ((Number )v).doubleValue ());
                        sum += ((Number )v).doubleValue ();
                    }
                }
                if (sum < 1.0 || sum > 1.0) { // issue warning
                    Util.logger ().warn ("Weights for " + e.getKey () + " did NOT sum to 1!");
                }
                prefDesc.weights.put (e.getKey (), kvMap);
            }
            Util.logger ().trace (" - Weights collected: " + prefDesc.weights);
        }
        else {
            Util.logger ().error (MessageFormat.format (" - No Weights exist in ''{0}''", utilityPath));
        }
        // create utility functions
        Map<String, Map> utilMap = utilityDefMap.get ("utilities");
        if (utilMap != null) {
            for (String k : utilMap.keySet ()) {
                Map vMap = utilMap.get (k);
                UtilityAttributes ua = new UtilityAttributes ();
                ua.label = (String )vMap.get ("label");
                ua.mapping = (String )vMap.get ("mapping");
                ua.desc = (String )vMap.get ("description");
                ua.values = (Map<Number, Number> )vMap.get ("utility");
                prefDesc.addAttributes (k, ua);
            }
            Util.logger ().trace (" - Utility functions collected: " + prefDesc.getUtilities ());
        }
        else {
            Util.logger ().error (MessageFormat.format (" - No utilities exist in ''{0}''", utilityPath));
        }

        Map<String, Map> vectorMap = utilityDefMap.get ("vectors");
        if (vectorMap != null) {
            for (String k : vectorMap.keySet ()) {
                prefDesc.attributeVectors.put (k, vectorMap.get (k));
            }
            Util.logger ().trace (" - Utility attribute vectors collected: " + prefDesc.attributeVectors);
        }
        else {
            Util.logger ().error (MessageFormat.format (" - No vectors exist in ''{0}''", utilityPath));
        }

        return prefDesc;
    }


    public static GaugeDescription loadGaugeSpecs () {
        File gaugeSpec = Util.getRelativeToPath (Rainbow.instance ().getTargetPath (),
                Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_GAUGES_PATH));
        return loadGaugeSpecs (gaugeSpec);
    }


    @SuppressWarnings ("unchecked")
    public static GaugeDescription loadGaugeSpecs (File gaugeSpec) {
        GaugeDescription gd = new GaugeDescription ();

        Map<String, Map<String, Map>> gaugeSpecMap = null;
        try {

            Object o = Yaml.load (gaugeSpec);
            Util.LOGGER.trace ("Gauge Spec Yaml file loaded: " + o.toString ());
            gaugeSpecMap = (Map )o;

            Map<String, Map> typeMap = gaugeSpecMap.get ("gauge-types");
            if (typeMap != null) {
                for (Map.Entry<String, Map> typeSpec : typeMap.entrySet ()) {
                    // map type name to Gauge type desc
                    String gaugeType = typeSpec.getKey ();
                    Map<String, Object> attrMap = typeSpec.getValue (); // get attribute map
                    // get comment
                    String typeComment = (String) attrMap.get ("comment");
                    // populate type description
                    GaugeTypeDescription gaugeTypeSpec = new GaugeTypeDescription (gaugeType, typeComment);
                    gd.typeSpec.put (gaugeType, gaugeTypeSpec);
                    // get mappings of reported values
                    Map<String, String> values = (Map<String, String>) attrMap.get ("commands");
                    for (Map.Entry<String, String> value : values.entrySet ()) {
                        String valName = value.getKey ();
                        String signature = value.getValue ();
                        gaugeTypeSpec.addCommandSignature (valName, signature);
                    }
                    // get mappings of setup params
                    Map<String, Map> params = (Map<String, Map>) attrMap.get ("setupParams");
                    for (Map.Entry<String, Map> param : params.entrySet ()) {
                        String pname = param.getKey ();
                        Map<String, Object> paramAttr = param.getValue ();
                        String ptype = (String) paramAttr.get ("type");
                        Object pdefault = paramAttr.get ("default");
                        if (!ptype.equals ("String")) {
                            pdefault = Util.parseObject (pdefault.toString (), ptype);
                        }
                        if (pdefault != null && pdefault instanceof String) {
                            pdefault = Util.evalTokens ((String) pdefault);
                        }
                        gaugeTypeSpec.addSetupParam (new TypedAttributeWithValue (pname, ptype, pdefault));
                    }
                    // get mappings of config params
                    params = (Map<String, Map>) attrMap.get ("configParams");
                    for (Map.Entry<String, Map> param : params.entrySet ()) {
                        String pname = param.getKey ();
                        Map<String, Object> paramAttr = param.getValue ();
                        String ptype = (String) paramAttr.get ("type");
                        Object pdefault = paramAttr.get ("default");
                        if (!ptype.equals ("String")) {
                            pdefault = Util.parseObject (pdefault.toString (), ptype);
                        }
                        if (pdefault != null && pdefault instanceof String) {
                            pdefault = Util.evalTokens ((String) pdefault);
                        }
                        gaugeTypeSpec.addConfigParam (new TypedAttributeWithValue (pname, ptype, pdefault));
                    }
                }
                Util.LOGGER.trace (" - Gauge Types collected: " + gd.typeSpec.keySet ());
            }
            else
                Util.LOGGER.warn (" - No gauge types specified");
            // store gauge instances
            Map<String, Map> instanceMap = gaugeSpecMap.get ("gauge-instances");
            if (instanceMap != null) {
                for (Map.Entry<String, Map> instSpec : instanceMap.entrySet ()) {
                    // map name to Gauge instance
                    String gaugeName = instSpec.getKey ();
                    Map<String, Object> attrMap = instSpec.getValue (); // get attribute map
                    // get type name, model description, comment
                    String gaugeType = (String) attrMap.get ("type");
                    TypedAttribute modelDesc = TypedAttribute.parsePair ((String) attrMap.get ("model"));
                    String instComment = (String) attrMap.get ("comment");
                    // populate instance description
                    GaugeTypeDescription gaugeTypeSpec = gd.typeSpec.get (gaugeType);
                    if (gaugeTypeSpec == null) {
                        Util.LOGGER.error (MessageFormat.format (
                                "Cannot find gauge type: {0} referred to in gauge ''{1}''.", gaugeType, gaugeName));
                        continue;
                    }
                    GaugeInstanceDescription gaugeInstSpec = gaugeTypeSpec.makeInstance (gaugeName, instComment);
                    gaugeInstSpec.setModelDesc (modelDesc);
                    gd.instSpec.put (gaugeName, gaugeInstSpec);
                    // get commands
                    Map<String, String> commandMappings = (Map<String, String>) attrMap.get ("commands");
                    for (Entry<String, String> cmd : commandMappings.entrySet ()) {
                        String key = cmd.getKey ();
                        String[] args = Util.evalCommand (cmd.getValue ());
                        gaugeInstSpec.addCommand (Util.evalTokens (key),
                                                  new OperationRepresentation (args[1],
                                                                               new ModelReference (modelDesc.getName (), modelDesc.getType ()), args[0],

                                                                               Arrays.copyOfRange (args, 2, args.length)));

                    }

                    // get mappings of setup values and store in setup param info
                    Map<String, Object> values = (Map<String, Object>) attrMap.get ("setupValues");
                    for (Map.Entry<String, Object> param : values.entrySet ()) {
                        String paramName = param.getKey ();
                        Object paramValue = param.getValue ();
                        if (paramValue != null) { // set new value
                            if (paramValue instanceof String) {
                                paramValue = Util.evalTokens ((String) paramValue);
                            }
                            TypedAttributeWithValue setupParam = gaugeInstSpec.findSetupParam (paramName);
                            if (setupParam != null) {
                                setupParam.setValue (paramValue);
                            }
                        }
                    }
                    // get mappings of config values and store in config param info
                    values = (Map<String, Object>) attrMap.get ("configValues");
                    for (Map.Entry<String, Object> param : values.entrySet ()) {
                        String paramName = param.getKey ();
                        Object paramValue = param.getValue ();
                        if (paramValue != null) { // set new value
                            if (paramValue instanceof String) {
                                paramValue = Util.evalTokens ((String) paramValue);
                            }
                            TypedAttributeWithValue configParam = gaugeInstSpec.findConfigParam (paramName);
                            if (configParam != null) {
                                if (!configParam.getType ().equals ("String")) {
                                    paramValue = Util.parseObject (paramValue.toString (), configParam.getType ());
                                }
                                configParam.setValue (paramValue);
                            }
                        }
                    }
                }
                Util.LOGGER.trace (" - Gauge Instances collected: " + gd.instSpec.keySet ());
            }
            else {
                Util.LOGGER.warn ( " - No gauge instances specified");
            }
        }
        catch (FileNotFoundException e) {
            Util.LOGGER.error ("Loading Gauge Spec Yaml file failed!", e);
        }

        // store gauge type descriptions

        return gd;
    }


    @SuppressWarnings ("unchecked")
    public static EffectorDescription loadEffectorDesc () {
        EffectorDescription ed = new EffectorDescription ();

        Map effectorMap = null;
        try {
            String effectorPath = Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_EFFECTORS_PATH);
            if (effectorPath == null) {
                Util.logger ().error (MessageFormat.format ("No property defined for ''{0}''. No effectors loaded.",
                        RainbowConstants.PROPKEY_EFFECTORS_PATH));
                ed.effectors = new TreeSet<> ();
                return ed;
            }
            File effectorFile = Util.getRelativeToPath (Rainbow.instance ().getTargetPath (), effectorPath);
            if (!effectorFile
                    .exists ()) {
                Util.logger ().error ("Effectr file does not exist: " + effectorPath);
                return ed;
            }
            Object o = Yaml.load (effectorFile);
            Util.logger ().trace ("Effector Desc Yaml file loaded: " + o.toString ());
            effectorMap = (Map )o;
            // acquire "variable" declarations and store as rainbow properties
            Map<String, String> varMap = (Map<String, String> )effectorMap.get ("vars");
            if (varMap != null)
                for (Map.Entry<String, String> varPair : varMap.entrySet ()) {
                    Rainbow.instance ().setProperty (varPair.getKey (), Util.evalTokens (varPair.getValue ()));
                }

            // store effector type info
            Map<String, Map> effTypeMap = (Map<String, Map> )effectorMap.get ("effector-types");
            if (effTypeMap != null) {
                for (Map.Entry<String, Map> etInfo : effTypeMap.entrySet ()) {
                    EffectorDescription.EffectorAttributes ea = new EffectorDescription.EffectorAttributes ();
                    ea.name = etInfo.getKey ();
                    Map<String, Object> attrMap = etInfo.getValue ();
                    ea.setKindName ((String )attrMap.get ("type"));
                    ea.setLocation (Util.evalTokens ((String )attrMap.get ("location"))); // the default location
                    String commandSignature = Util.evalTokens ((String )attrMap.get ("command"));
                    if (commandSignature != null) {
                        ea.setCommandPattern (OperationRepresentation.parseCommandSignature (commandSignature));
                    }
                    Map<String, Object> addlInfoMap = (Map<String, Object> )attrMap.get (ea.infoPropName ());
                    extractArrays (ea, addlInfoMap);
                    ed.effectorTypes.put (ea.name, ea);
                }
            }

            // store effector description info
            Map<String, Map> effMap = (Map<String, Map> )effectorMap.get ("effectors");
            for (Map.Entry<String, Map> effInfo : effMap.entrySet ()) {
                EffectorDescription.EffectorAttributes ea = new EffectorDescription.EffectorAttributes ();

                // get effector name
                ea.name = effInfo.getKey ();
                Map<String, Object> attrMap = effInfo.getValue (); // get attribute map
                ea.setKindName ((String )attrMap.get ("type"));
                if (ea.getKindName () != null) {
                    ea.setKind (IEffector.Kind.valueOf (ea.getKindName ().toUpperCase ()));
                }

                String effectorType = (String )attrMap.get ("effector-type");
                if (effectorType != null) {
                    ea.effectorType = ed.effectorTypes.get (effectorType);
                }

                // get location and effector type
                ea.setLocation (Util.evalTokens ((String )attrMap.get ("location")));
                String commandSignature = Util.evalTokens ((String )attrMap.get ("command"));
                if (commandSignature != null) {
                    ea.setCommandPattern (OperationRepresentation.parseCommandSignature (commandSignature));
                }

                Map<String, Object> addlInfoMap = (Map<String, Object> )attrMap.get (ea.infoPropName ());
                extractArrays (ea, addlInfoMap);
                ed.effectors.add (ea);
            }
            Util.logger ().trace (" - Effectors collected: " + ed.effectors);
        }
        catch (FileNotFoundException e) {
            Util.logger ().error ("Loading Effector Desc Yaml file failed!", e);
        }

        return ed;
    }


    @SuppressWarnings ("unchecked")
    public static ProbeDescription loadProbeDesc () {
        ProbeDescription ed = new ProbeDescription ();

        Map probeMap = null;
        try {
            String probePath = Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_PROBES_PATH);
            if (probePath == null) {
                Util.logger ().error (MessageFormat.format ("No property defined for ''{0}''. No probes loaded.",
                        RainbowConstants.PROPKEY_PROBES_PATH));
                ed.probes = new TreeSet<> ();
                return ed;
            }
            File probeFile = Util.getRelativeToPath (Rainbow.instance ().getTargetPath (), probePath);
            Object o = Yaml.load (probeFile);
            Util.logger ().trace ("Probe Desc Yaml file loaded: " + o.toString ());
            probeMap = (Map )o;
            Map<String, String> varMap = (Map<String, String> )probeMap.get ("vars");
            if (varMap != null)
                for (Map.Entry<String, String> varPair : varMap.entrySet ()) {
                    Rainbow.instance ().setProperty (varPair.getKey (), Util.evalTokens (varPair.getValue ()));
                }

            // store probe description info
            Map<String, Map> pbMap = (Map<String, Map> )probeMap.get ("probes");
            if (pbMap != null) {
                for (Map.Entry<String, Map> pbInfo : pbMap.entrySet ()) {
                    ProbeDescription.ProbeAttributes pa = new ProbeDescription.ProbeAttributes ();

                    // get probe name
                    pa.name = pbInfo.getKey ();
                    Map<String, Object> attrMap = pbInfo.getValue (); // get attribute map
                    // get location, alias, and probe type
                    pa.setLocation (Util.evalTokens ((String) attrMap.get ("location")));
                    pa.alias = (String) attrMap.get ("alias");
                    pa.setKindName ((String) attrMap.get ("type"));
                    pa.kind = IProbe.Kind.valueOf (pa.getKindName ().toUpperCase ());
                    Map<String, Object> addlInfoMap = (Map<String, Object>) attrMap.get (pa.infoPropName ());
                    extractArrays (pa, addlInfoMap);
                    ed.probes.add (pa);
                }
                Util.logger ().trace (" - Probe collected: " + ed.probes);
            }
            else {
                Util.logger ().warn (" - No probes specified");

            }
        }
        catch (FileNotFoundException e) {
            Util.logger ().error ("Loading Probe Desc Yaml file failed!", e);
        }

        // acquire "variable" declarations and store as rainbow properties

        return ed;
    }

    /**
     * Acquires additional info (key-value pairs) based on the element Kind.
     * 
     * @param attr
     *            the DescriptionAttributes object to populate
     * @param infoMap
     *            the map of key-value info pairs
     */
    public static void extractArrays (DescriptionAttributes attr, Map<String, Object> infoMap) {
        List<String> arrayKeys = new ArrayList<> ();
        if (infoMap == null) return;
        for (Map.Entry<String, Object> pair : infoMap.entrySet ()) {
            if (pair.getKey ().endsWith (".length")) { // store just the key
                arrayKeys.add (pair.getKey ().replace (".length", ""));
            }
            String valStr = String.valueOf (pair.getValue ());
//            attr.getInfo().put (pair.getKey (), Util.evalTokens (valStr));
            attr.putInfo (pair.getKey (), Util.evalTokens (valStr));
        }
        /* Get any key-value pair named "key.length", remove it, find all
         * key.# items, and construct an array out of the list of values
         */
        for (String arrayKey : arrayKeys) {
            int length = Integer.parseInt (attr.getInfo ()./*remove*/get (arrayKey + ".length"));
            String[] valArray = new String[length]; // new array
            for (int i = 0; i < length; ++i) { // store item in array
                String itemKey = arrayKey + Util.DOT + i;
                if (attr.getInfo ().containsKey (itemKey)) {
                    valArray[i] = attr.getInfo ()./*remove*/get (itemKey);
                }
            }
            attr.putArray (arrayKey, valArray);
//            attr.getArrays().put (arrayKey, valArray); // store array
        }
    }

}
