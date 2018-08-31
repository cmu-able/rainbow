package org.sa.rainbow.swim.adaptation;

import java.util.HashMap;

import org.ho.yaml.Yaml;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.adaptation.AdaptationExecutionOperatorT;
import org.sa.rainbow.core.adaptation.AdaptationTree;
import org.sa.rainbow.model.acme.swim.SwimModelHelper;
import org.sa.rainbow.stitch.core.Strategy;

import pladapt.EnvironmentDTMCPartitioned;
import pladapt.GenericConfiguration;
import pladapt.GenericConfigurationManager;
import pladapt.JavaSDPAdaptationManager;
import pladapt.SDPAdaptationManager;
import pladapt.StringVector;

public class AdaptationManager extends AdaptationManagerBase {
	private static final String PLASDP_REACH_PATH = "rainbow.adaptation.plasdp.reachPath";
    private static final String PLASDP_REACH_MODEL = "rainbow.adaptation.plasdp.reachModel";
    private JavaSDPAdaptationManager m_adaptMgr;

	public AdaptationManager() {
		// TODO Auto-generated constructor stub
	}

    protected void initializeAdaptationMgr(SwimModelHelper swimModel) {
        log("Starting PLA-SDP Adaptation Manager initialization");
        super.initializeAdaptationMgr(swimModel);
        m_adaptMgr = new JavaSDPAdaptationManager();

        // define configuration space
        GenericConfigurationManager configMgr = new GenericConfigurationManager();
        GenericConfiguration configTemplate = configMgr.getConfigurationTemplate();
        configTemplate.setInt(SwimModelHelper.CONFIG_SERVERS, 0);
        configTemplate.setInt(SwimModelHelper.CONFIG_DIMMER, 0);
        configTemplate.setInt(SwimModelHelper.CONFIG_ADD_SERVER_PROGRESS, 0);
        
        int maxServers = swimModel.getMaxServers();
        int addServerLatencyPeriods = swimModel.getAddServerLatencyPeriods();
        
        for (int dimmerLevels = 0; dimmerLevels < swimModel.getDimmerLevels(); dimmerLevels++) {
            for (int servers = 0; servers < maxServers; servers++) {
                for (int bootProgress = 0; bootProgress <= addServerLatencyPeriods; bootProgress++) {
                	GenericConfiguration config = configMgr.addNewConfiguration();
                	config.setInt(SwimModelHelper.CONFIG_SERVERS, servers);
                	config.setInt(SwimModelHelper.CONFIG_DIMMER, dimmerLevels);
                	config.setInt(SwimModelHelper.CONFIG_ADD_SERVER_PROGRESS, bootProgress);
                }
            }
        }
        
    	// create configuration parameters.
        // the following hardcoded values are like the constants above
    	HashMap<String, Object> params = new HashMap<>();
        params.put(SDPAdaptationManager.getNO_LATENCY(), Boolean.FALSE);
        params.put(SDPAdaptationManager.getREACH_PATH(), Rainbow.instance().getProperty(PLASDP_REACH_PATH));
        params.put(SDPAdaptationManager.getREACH_MODEL(), Rainbow.instance().getProperty(PLASDP_REACH_MODEL));
		params.put(SDPAdaptationManager.getREACH_SCOPE(),
				"S=" + maxServers + " TAP#=" + addServerLatencyPeriods + " D=" + swimModel.getDimmerLevels());
        String yamlParams = Yaml.dump(params);
        
        m_adaptMgr.initialize(configMgr, yamlParams);
        log("PLA-SDP Adaptation Manager Initialized");
    }
    
	protected AdaptationTree<Strategy> checkAdaptationImpl(SwimModelHelper swimModel,
			EnvironmentDTMCPartitioned env) {
    	AdaptationTree<Strategy> at = null;
    	
        GenericConfiguration currentConfig = new GenericConfiguration();
        currentConfig.setInt(SwimModelHelper.CONFIG_SERVERS, swimModel.getNumActiveServers() - 1);
        currentConfig.setInt(SwimModelHelper.CONFIG_DIMMER, swimModel.getCurrentDimmerLevel() - 1);
        int progress = swimModel.getAddServerTacticProgress();
        currentConfig.setInt(SwimModelHelper.CONFIG_ADD_SERVER_PROGRESS, progress);
        
        log("current configuration is " + currentConfig);
        
        SwimUtilityFunction utilityFunction = new SwimUtilityFunction(m_model, swimModel);
        
        //m_adaptMgr.setDebug(true);
        StringVector tactics = m_adaptMgr.evaluate(currentConfig, env, utilityFunction, m_horizon);

        if (tactics.isEmpty()) {
        	log("no adaptation required");
        } else {
//        	at = new AdaptationTree<Strategy> (getStrategy(tactics.get(0)));
//        	at = new AdaptationTree<Strategy> (getStrategy("IncDimmer"));
        	at = new AdaptationTree<Strategy>(AdaptationExecutionOperatorT.PARALLEL);
	        for (int t = 0; t < tactics.size(); t++) {
	        	log(tactics.get(t));
	        	Strategy strategy = getStrategy(tactics.get(t)); // Strategy has tactic name
	        	at.addLeaf(strategy);
	        }
        }

        return at;
    }


}
