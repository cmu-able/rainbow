package org.sa.rainbow.core;

import org.sa.rainbow.core.gauges.GaugeDescription;
import org.sa.rainbow.core.models.EffectorDescription;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.ProbeDescription;
import org.sa.rainbow.core.models.UtilityPreferenceDescription;
import org.sa.rainbow.util.RainbowConfigurationChecker;
import org.sa.rainbow.util.RainbowConfigurationChecker.Problem;
import org.sa.rainbow.util.YamlUtil;

public class CheckConfiguration {

    public static void main (String[] args) {
        System.out.println ("Reading configuration files");
        Rainbow.instance ();
        System.out.println ("Loading YAMLs for target: " + Rainbow.getProperty (RainbowConstants.PROPKEY_TARGET_NAME));
        System.out.print ("Loading probes...");
        System.out.flush ();
        final ProbeDescription loadProbeDesc = YamlUtil.loadProbeDesc ();
        System.out.println ("found " + loadProbeDesc.probes.size () + " probes");
        System.out.print ("Loading effecors...");
        System.out.flush ();
        final EffectorDescription loadEffectorDesc = YamlUtil.loadEffectorDesc ();
        System.out.println ("found " + loadEffectorDesc.effectors.size () + " effectors");
        System.out.print ("Loading gauges...");
        System.out.flush ();
        final GaugeDescription loadGaugeSpecs = YamlUtil.loadGaugeSpecs ();
        System.out.println ("found " + loadGaugeSpecs.typeSpec.size () + " types, " + loadGaugeSpecs.instSpec.size ()
                + " instances");
        System.out.print ("Loading preferences...");
        System.out.flush ();
        final UtilityPreferenceDescription loadUtilityPrefs = YamlUtil.loadUtilityPrefs ();
        System.out.println ("found " + loadUtilityPrefs.attributeVectors.size () + " attribute vectors, "
                + loadUtilityPrefs.utilities.size () + " utilities, " + loadUtilityPrefs.weights.size () + " weights");
        System.out.print ("Loading models...");
        System.out.flush ();
        final ModelsManager mm = new ModelsManager ();

        RainbowConfigurationChecker checker = new RainbowConfigurationChecker (new IRainbowMaster () {

            @Override
            public ProbeDescription probeDesc () {
                return loadProbeDesc;
            }

            @Override
            public UtilityPreferenceDescription preferenceDesc () {
                return loadUtilityPrefs;
            }

            @Override
            public GaugeDescription gaugeDesc () {
                return loadGaugeSpecs;
            }

            @Override
            public EffectorDescription effectorDesc () {
                return loadEffectorDesc;
            }

            @Override
            public ModelsManager modelsManager () {
                return mm;
            }
        });
        mm.m_reportingPort = checker;
        mm.initializeModels ();
        System.out.println ("found " + mm.getRegisteredModelTypes () + " model *types*");
        System.out.println ("Checking configuration consistency...");
        checker.checkRainbowConfiguration ();
        if (checker.getProblems ().size () > 0) {
            System.out.println ("Problems with the configuration were reported:");
            for (Problem p : checker.getProblems ()) {
                System.out.println (p.problem.name () + ": " + p.msg);
            }
        }
        else {
            System.out.println ("No problems were found with the configuration");
        }
        Rainbow.signalTerminate ();
        System.exit (0);

    }

    public ModelsManager modelsManager () {
        return null;
    }

}
