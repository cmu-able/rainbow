/*******************************************************************************
 * PLA Adaptation Manager
 *
 * Copyright 2017 Carnegie Mellon University. All Rights Reserved.
 * 
 * NO WARRANTY. THIS CARNEGIE MELLON UNIVERSITY AND SOFTWARE ENGINEERING
 * INSTITUTE MATERIAL IS FURNISHED ON AN "AS-IS" BASIS. CARNEGIE MELLON
 * UNIVERSITY MAKES NO WARRANTIES OF ANY KIND, EITHER EXPRESSED OR IMPLIED, AS
 * TO ANY MATTER INCLUDING, BUT NOT LIMITED TO, WARRANTY OF FITNESS FOR PURPOSE
 * OR MERCHANTABILITY, EXCLUSIVITY, OR RESULTS OBTAINED FROM USE OF THE
 * MATERIAL. CARNEGIE MELLON UNIVERSITY DOES NOT MAKE ANY WARRANTY OF ANY KIND
 * WITH RESPECT TO FREEDOM FROM PATENT, TRADEMARK, OR COPYRIGHT INFRINGEMENT.
 *
 * Released under a BSD-style license, please see license.txt or contact
 * permission@sei.cmu.edu for full terms.
 *
 * [DISTRIBUTION STATEMENT A] This material has been approved for public release
 * and unlimited distribution. Please see Copyright notice for non-US Government
 * use and distribution.
 ******************************************************************************/
import pladapt.GenericConfiguration;
import pladapt.GenericConfigurationManager;
import pladapt.GenericEnvironment;
import pladapt.GenericUtilityFunction;
import pladapt.pladapt;
import pladapt.JavaSDPAdaptationManager;
import pladapt.GenericEnvironmentDTMCPartitioned;
import pladapt.StringVector;

class TestSDPAdaptationManager {
  static {
    System.loadLibrary("pladapt_wrap");
  }
  public static void log(String msg) {
	System.out.println(msg);
  }

	public static void main(String argv[]) {
        log("Starting PLA Adaptation Manager initialization");

        JavaSDPAdaptationManager adaptMgr = new JavaSDPAdaptationManager();

        final int MAX_SERVERS = 3;
        final int LATENCY_PERIODS = 1;
        final int DIMMER_LEVELS = 5;
        
        // define configuration space
        GenericConfigurationManager configMgr = new GenericConfigurationManager();
	log("l1");
        GenericConfiguration configTemplate = configMgr.getConfigurationTemplate();
        configTemplate.setInt("s", 0);
        configTemplate.setInt("d", 0);
        configTemplate.setInt("addServerProgress", 0);
	log("l2");
        for (int dimmerLevels = 0; dimmerLevels < DIMMER_LEVELS; dimmerLevels++) {
            for (int servers = 0; servers < MAX_SERVERS; servers++) {
                for (int bootProgress = 0; bootProgress <= LATENCY_PERIODS; bootProgress++) {
                	GenericConfiguration config = configMgr.addNewConfiguration();
                	config.setInt("s", servers);
                	config.setInt("d", dimmerLevels);
                	config.setInt("addServerProgress", bootProgress);
                }
            }
        }
        log("l3");
    	// create configuration parameters.
        // the following hardcoded values are like the constants above
/*
    	HashMap<String, Object> params = new HashMap<>();
        params.put(SDPAdaptationManager.getNO_LATENCY(), Boolean.FALSE);
        params.put(SDPAdaptationManager.getREACH_PATH(), "/home/gmoreno/research/code/reach/reach.sh");
        params.put(SDPAdaptationManager.getREACH_MODEL(), "/home/gmoreno/research/code/reach/model/reachAddSvrDimmer"));
        params.put(SDPAdaptationManager.getREACH_SCOPE(), "S=3 TAP#=1 D=5");
        String yamlParams = Yaml.dump(params);
*/
	String yamlParams = "---\nreachPath: /home/gmoreno/research/code/reach/reach.sh\nreachScope: S=3 TAP#=1 D=5\nnolatency: false\nreachModel: /home/gmoreno/research/code/reach/model/reachAddSvrDimmer\n";
        adaptMgr.initialize(configMgr, yamlParams);
        log("PLA Adaptation Manager Initialized");

	GenericEnvironmentDTMCPartitioned envDTMC = new GenericEnvironmentDTMCPartitioned(2);
	GenericEnvironment e1 = new GenericEnvironment("r");
	e1.setDouble("r", 1.0);
	GenericEnvironment e2 = new GenericEnvironment("r");
	e2.setDouble("r", 2.0);
	envDTMC.assignToPart(0, 0);
	envDTMC.assignToPart(1, 1);
	envDTMC.setTransitionProbability(0, 1, 1.0);
	envDTMC.setTransitionProbability(1, 1, 1.0);
	envDTMC.setStateValue(0, e1);
	envDTMC.setStateValue(1, e2);

	log("EnvDTMC created");

	TestUtilityFunction u = new TestUtilityFunction();
	GenericConfiguration current = new GenericConfiguration();
        current.setInt("s", 1);
        current.setInt("d", 1);
        current.setInt("addServerProgress", 1);
	adaptMgr.setDebug(true);
	log("starting evaluation");
	StringVector tactics = adaptMgr.evaluate(current, envDTMC,  u, 2);
	log("evaluate result:");
	for (int i = 0; i < tactics.size(); i++) {
		log(tactics.get(i));
	}
	}
}

/*
--- 
reachPath: /home/gmoreno/research/code/reach/reach.sh
reachScope: S=3 TAP#=1 D=5
nolatency: false
reachModel: /home/gmoreno/research/code/reach/model/reachAddSvrDimmer
*/
