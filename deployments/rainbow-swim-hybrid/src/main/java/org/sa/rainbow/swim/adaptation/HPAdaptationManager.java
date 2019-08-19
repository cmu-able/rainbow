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
package org.sa.rainbow.swim.adaptation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.element.property.IAcmePropertyValue;
import org.apache.commons.lang.time.StopWatch;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.IRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.adaptation.AdaptationExecutionOperatorT;
import org.sa.rainbow.core.adaptation.AdaptationTree;
import org.sa.rainbow.core.adaptation.DefaultAdaptationTreeWalker;
import org.sa.rainbow.core.adaptation.IAdaptationManager;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.health.IRainbowHealthProtocol;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.UtilityFunction;
import org.sa.rainbow.core.models.UtilityPreferenceDescription;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowChangeBusSubscription;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.IRainbowAdaptationEnqueuePort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.AcmeRainbowOperationEvent.CommandEventT;
import org.sa.rainbow.model.acme.swim.SwimModelHelper;
import org.sa.rainbow.timeseriespredictor.model.TimeSeriesPredictorModel;
import org.sa.rainbow.timeseriespredictor.model.TimeSeriesPredictorModelInstance;
import org.sa.rainbow.util.Beacon;
import org.sa.rainbow.util.Util;

import pladapt.AdaptationPlanner;
import pladapt.EnvironmentDTMCPartitioned;
import pladapt.PMCAdaptationManager;
import pladapt.PlanDB;
import pladapt.StringVector;


public final class HPAdaptationManager extends AbstractRainbowRunnable
        implements IAdaptationManager<SwimExtendedPlan>, IRainbowModelChangeCallback {

    static {
        System.loadLibrary("pladapt_wrap");
    }

    public static final String NAME = "Swim Extended Adaptation Manager";
    private static final String TSP_MODEL = "ArrivalRate";

    // The thread "sleep" time. runAction will be called every 60 seconds in this case
    public static final int SLEEP_TIME = 30000 /*ms*/;

    // Port to query with any models in models manager
    private IModelsManagerPort                       m_modelsManagerPort;
    private IModelChangeBusSubscriberPort            m_modelChangePort;
    private ModelReference                           m_modelRef;
    private IRainbowAdaptationEnqueuePort<SwimExtendedPlan> m_adaptationEnqueuePort;
    private boolean                                  m_adaptationEnabled = true;
    private boolean                                  m_adaptationNeeded       = false; 
    private boolean                                  m_errorDetected     = false;
    private FileChannel                   m_strategyLog              = null;
    private boolean                       m_executingPlan   = false;
   
    protected AcmeModelInstance                      m_model             = null;
   
    protected boolean m_isInitialized = false;

    protected int m_horizon;
    protected int m_currentTime;
    private TimeSeriesPredictorModelInstance m_tspModel; 
    private PMCAdaptationManager m_adaptMgr;
    private PlanDB m_planDB;
    //private AdaptationPlanner m_adaptPlanner;


    private IRainbowChangeBusSubscription m_modelTypecheckingChanged = new IRainbowChangeBusSubscription () {

        @Override
        public boolean matches (IRainbowMessage message) {
            String type = (String) message.getProperty (IModelChangeBusPort.EVENT_TYPE_PROP);
            String modelName = (String) message.getProperty (IModelChangeBusPort.MODEL_NAME_PROP);
            String modelType = (String) message.getProperty (IModelChangeBusPort.MODEL_TYPE_PROP);
            try {
                CommandEventT ct = CommandEventT.valueOf (type);
                return (ct.isEnd ()
                        && "setTypecheckResult".equals (message.getProperty (IModelChangeBusPort.COMMAND_PROP))
                        && m_modelRef.equals (Util.genModelRef (modelName, modelType)));
            } catch (Exception e) {
                return false;
            }
        }
    };

    public HPAdaptationManager () {
        super (NAME);
        String per = Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_MODEL_EVAL_PERIOD);
        if (per != null) {
            setSleepTime (Long.parseLong (per));
        }
        else {
            setSleepTime (SLEEP_TIME);
        }
    }

    @Override
    public void initialize (IRainbowReportingPort port) throws RainbowConnectionException {
        super.initialize(port);
        initConnectors ();
    }

    private void initConnectors () throws RainbowConnectionException {
        // Create port to query models manager
        m_modelsManagerPort = RainbowPortFactory.createModelsManagerRequirerPort ();        
        m_modelChangePort = RainbowPortFactory.createModelChangeBusSubscriptionPort ();
        m_modelChangePort.subscribe (m_modelTypecheckingChanged, this);
    }

    @Override
    public void setModelToManage(ModelReference modelRef) {
        m_modelRef = modelRef;
        try {
			m_strategyLog = new FileOutputStream (new File (new File (Rainbow.instance ().getTargetPath (), "log"),
			                                                    modelRef.getModelName () + "-adaptation.log")).getChannel ();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        m_model = (AcmeModelInstance) m_modelsManagerPort.<IAcmeSystem>getModelInstance (modelRef);
        if (m_model == null) {
            m_reportingPort.error (RainbowComponentT.ADAPTATION_MANAGER,
                                   MessageFormat.format ("Could not find reference to {0}", modelRef.toString ()));
        }
        m_adaptationEnqueuePort = RainbowPortFactory.createAdaptationEnqueuePort (modelRef);
        SwimModelHelper swimModel = new SwimModelHelper(m_model);
        if (!m_isInitialized) {
        	initializeAdaptationMgr(swimModel);
        }        
    }

    String init_state_r = "const double addServer_LATENCY = 120;\n" + 
    		"const int HORIZON = 5;\n" + 
    		"const double PERIOD = 60;\n" + 
    		"const int DIMMER_LEVELS = 3;\n" + 
    		"const int ini_dimmer = 2;\n" + 
    		"const int MAX_SERVERS_A = 1;\n" + 
    		"const int MAX_SERVERS_B = 1;\n" + 
    		"const int MAX_SERVERS_C = 1;\n" + 
    		"const int ini_servers_A = 1;\n" + 
    		"const int ini_servers_B = 1;\n" + 
    		"const int ini_servers_C = 1;\n" + 
    		"const int ini_addServerA_state = 0;\n" + 
    		"const int ini_addServerB_state = 0;\n" + 
    		"const int ini_addServerC_state = 0;\n" + 
    		"const double SERVERA_COST_SEC = 1;\n" + 
    		"const double SERVERB_COST_SEC = 0.7;\n" + 
    		"const double SERVERC_COST_SEC = 0.5;\n" + 
    		"const double MAX_ARRIVALA_CAPACITY = 200;\n" + 
    		"const double MAX_ARRIVALA_CAPACITY_LOW = 400;\n" + 
    		"const double MAX_ARRIVALB_CAPACITY = 140;\n" + 
    		"const double MAX_ARRIVALB_CAPACITY_LOW = 280;\n" + 
    		"const double MAX_ARRIVALC_CAPACITY = 100;\n" + 
    		"const double MAX_ARRIVALC_CAPACITY_LOW = 200;\n" + 
    		"const double penalty = -0.25;\n" + 
    		"const int ini_traffic_A = 2;\n" + 
    		"const int ini_traffic_B = 1;\n" + 
    		"const int ini_traffic_C = 1;\n" + 
    		"const double interArrivalScaleFactorForDecision = 1;";
    String env_mod_r = "formula stateValue = 0.00180519;";
    String init_state_d = "const double addServer_LATENCY = 120;\n" + 
    		"const int HORIZON = 5;\n" + 
    		"const double PERIOD = 60;\n" + 
    		"const int DIMMER_LEVELS = 3;\n" + 
    		"const int ini_dimmer = 1;\n" + 
    		"const int MAX_SERVERS_A = 1;\n" + 
    		"const int MAX_SERVERS_B = 1;\n" + 
    		"const int MAX_SERVERS_C = 1;\n" + 
    		"const int ini_servers_A = 1;\n" + 
    		"const int ini_servers_B = 0;\n" + 
    		"const int ini_servers_C = 0;\n" + 
    		"const int ini_addServerA_state = 0;\n" + 
    		"const int ini_addServerB_state = 0;\n" + 
    		"const int ini_addServerC_state = 0;\n" + 
    		"const double SERVERA_COST_SEC = 1;\n" + 
    		"const double SERVERB_COST_SEC = 0.7;\n" + 
    		"const double SERVERC_COST_SEC = 0.5;\n" + 
    		"const double MAX_ARRIVALA_CAPACITY = 200;\n" + 
    		"const double MAX_ARRIVALA_CAPACITY_LOW = 400;\n" + 
    		"const double MAX_ARRIVALB_CAPACITY = 140;\n" + 
    		"const double MAX_ARRIVALB_CAPACITY_LOW = 280;\n" + 
    		"const double MAX_ARRIVALC_CAPACITY = 100;\n" + 
    		"const double MAX_ARRIVALC_CAPACITY_LOW = 200;\n" + 
    		"const double penalty = -0.25;\n" + 
    		"const int ini_traffic_A = 4;\n" + 
    		"const int ini_traffic_B = 0;\n" + 
    		"const int ini_traffic_C = 0;\n" + 
    		"const double interArrivalScaleFactorForDecision = 1;";
    String env_mod_d = "module environment\n" + 
    		"s : [0..201] init 0;\n" + 
    		"[tick] s = 0 -> \n" + 
    		"	0.185 : (s' = 1)\n" + 
    		"	+ 0.63 : (s' = 2)\n" + 
    		"	+ 0.185 : (s' = 3);\n" + 
    		"[tick] s = 3 -> \n" + 
    		"	0.185 : (s' = 4)\n" + 
    		"	+ 0.63 : (s' = 5)\n" + 
    		"	+ 0.185 : (s' = 6);\n" + 
    		"[tick] s = 6 -> \n" + 
    		"	0.185 : (s' = 7)\n" + 
    		"	+ 0.63 : (s' = 8)\n" + 
    		"	+ 0.185 : (s' = 9);\n" + 
    		"[tick] s = 9 -> \n" + 
    		"	0.185 : (s' = 10)\n" + 
    		"	+ 0.63 : (s' = 11)\n" + 
    		"	+ 0.185 : (s' = 12);\n" + 
    		"[tick] s = 12 -> \n" + 
    		"	1 : (s' = 13);\n" + 
    		"[tick] s = 11 -> \n" + 
    		"	1 : (s' = 14);\n" + 
    		"[tick] s = 10 -> \n" + 
    		"	1 : (s' = 15);\n" + 
    		"[tick] s = 8 -> \n" + 
    		"	0.185 : (s' = 16)\n" + 
    		"	+ 0.63 : (s' = 17)\n" + 
    		"	+ 0.185 : (s' = 18);\n" + 
    		"[tick] s = 18 -> \n" + 
    		"	1 : (s' = 19);\n" + 
    		"[tick] s = 17 -> \n" + 
    		"	1 : (s' = 20);\n" + 
    		"[tick] s = 16 -> \n" + 
    		"	1 : (s' = 21);\n" + 
    		"[tick] s = 7 -> \n" + 
    		"	0.185 : (s' = 22)\n" + 
    		"	+ 0.63 : (s' = 23)\n" + 
    		"	+ 0.185 : (s' = 24);\n" + 
    		"[tick] s = 24 -> \n" + 
    		"	1 : (s' = 25);\n" + 
    		"[tick] s = 23 -> \n" + 
    		"	1 : (s' = 26);\n" + 
    		"[tick] s = 22 -> \n" + 
    		"	1 : (s' = 27);\n" + 
    		"[tick] s = 5 -> \n" + 
    		"	0.185 : (s' = 28)\n" + 
    		"	+ 0.63 : (s' = 29)\n" + 
    		"	+ 0.185 : (s' = 30);\n" + 
    		"[tick] s = 30 -> \n" + 
    		"	0.185 : (s' = 31)\n" + 
    		"	+ 0.63 : (s' = 32)\n" + 
    		"	+ 0.185 : (s' = 33);\n" + 
    		"[tick] s = 33 -> \n" + 
    		"	1 : (s' = 34);\n" + 
    		"[tick] s = 32 -> \n" + 
    		"	1 : (s' = 35);\n" + 
    		"[tick] s = 31 -> \n" + 
    		"	1 : (s' = 36);\n" + 
    		"[tick] s = 29 -> \n" + 
    		"	0.185 : (s' = 37)\n" + 
    		"	+ 0.63 : (s' = 38)\n" + 
    		"	+ 0.185 : (s' = 39);\n" + 
    		"[tick] s = 39 -> \n" + 
    		"	1 : (s' = 40);\n" + 
    		"[tick] s = 38 -> \n" + 
    		"	1 : (s' = 41);\n" + 
    		"[tick] s = 37 -> \n" + 
    		"	1 : (s' = 42);\n" + 
    		"[tick] s = 28 -> \n" + 
    		"	0.185 : (s' = 43)\n" + 
    		"	+ 0.63 : (s' = 44)\n" + 
    		"	+ 0.185 : (s' = 45);\n" + 
    		"[tick] s = 45 -> \n" + 
    		"	1 : (s' = 46);\n" + 
    		"[tick] s = 44 -> \n" + 
    		"	1 : (s' = 47);\n" + 
    		"[tick] s = 43 -> \n" + 
    		"	1 : (s' = 48);\n" + 
    		"[tick] s = 4 -> \n" + 
    		"	0.185 : (s' = 49)\n" + 
    		"	+ 0.63 : (s' = 50)\n" + 
    		"	+ 0.185 : (s' = 51);\n" + 
    		"[tick] s = 51 -> \n" + 
    		"	0.185 : (s' = 52)\n" + 
    		"	+ 0.63 : (s' = 53)\n" + 
    		"	+ 0.185 : (s' = 54);\n" + 
    		"[tick] s = 54 -> \n" + 
    		"	1 : (s' = 55);\n" + 
    		"[tick] s = 53 -> \n" + 
    		"	1 : (s' = 56);\n" + 
    		"[tick] s = 52 -> \n" + 
    		"	1 : (s' = 57);\n" + 
    		"[tick] s = 50 -> \n" + 
    		"	0.185 : (s' = 58)\n" + 
    		"	+ 0.63 : (s' = 59)\n" + 
    		"	+ 0.185 : (s' = 60);\n" + 
    		"[tick] s = 60 -> \n" + 
    		"	1 : (s' = 61);\n" + 
    		"[tick] s = 59 -> \n" + 
    		"	1 : (s' = 62);\n" + 
    		"[tick] s = 58 -> \n" + 
    		"	1 : (s' = 63);\n" + 
    		"[tick] s = 49 -> \n" + 
    		"	0.185 : (s' = 64)\n" + 
    		"	+ 0.63 : (s' = 65)\n" + 
    		"	+ 0.185 : (s' = 66);\n" + 
    		"[tick] s = 66 -> \n" + 
    		"	1 : (s' = 67);\n" + 
    		"[tick] s = 65 -> \n" + 
    		"	1 : (s' = 68);\n" + 
    		"[tick] s = 64 -> \n" + 
    		"	1 : (s' = 69);\n" + 
    		"[tick] s = 2 -> \n" + 
    		"	0.185 : (s' = 70)\n" + 
    		"	+ 0.63 : (s' = 71)\n" + 
    		"	+ 0.185 : (s' = 72);\n" + 
    		"[tick] s = 72 -> \n" + 
    		"	0.185 : (s' = 73)\n" + 
    		"	+ 0.63 : (s' = 74)\n" + 
    		"	+ 0.185 : (s' = 75);\n" + 
    		"[tick] s = 75 -> \n" + 
    		"	0.185 : (s' = 76)\n" + 
    		"	+ 0.63 : (s' = 77)\n" + 
    		"	+ 0.185 : (s' = 78);\n" + 
    		"[tick] s = 78 -> \n" + 
    		"	1 : (s' = 79);\n" + 
    		"[tick] s = 77 -> \n" + 
    		"	1 : (s' = 80);\n" + 
    		"[tick] s = 76 -> \n" + 
    		"	1 : (s' = 81);\n" + 
    		"[tick] s = 74 -> \n" + 
    		"	0.185 : (s' = 82)\n" + 
    		"	+ 0.63 : (s' = 83)\n" + 
    		"	+ 0.185 : (s' = 84);\n" + 
    		"[tick] s = 84 -> \n" + 
    		"	1 : (s' = 85);\n" + 
    		"[tick] s = 83 -> \n" + 
    		"	1 : (s' = 86);\n" + 
    		"[tick] s = 82 -> \n" + 
    		"	1 : (s' = 87);\n" + 
    		"[tick] s = 73 -> \n" + 
    		"	0.185 : (s' = 88)\n" + 
    		"	+ 0.63 : (s' = 89)\n" + 
    		"	+ 0.185 : (s' = 90);\n" + 
    		"[tick] s = 90 -> \n" + 
    		"	1 : (s' = 91);\n" + 
    		"[tick] s = 89 -> \n" + 
    		"	1 : (s' = 92);\n" + 
    		"[tick] s = 88 -> \n" + 
    		"	1 : (s' = 93);\n" + 
    		"[tick] s = 71 -> \n" + 
    		"	0.185 : (s' = 94)\n" + 
    		"	+ 0.63 : (s' = 95)\n" + 
    		"	+ 0.185 : (s' = 96);\n" + 
    		"[tick] s = 96 -> \n" + 
    		"	0.185 : (s' = 97)\n" + 
    		"	+ 0.63 : (s' = 98)\n" + 
    		"	+ 0.185 : (s' = 99);\n" + 
    		"[tick] s = 99 -> \n" + 
    		"	1 : (s' = 100);\n" + 
    		"[tick] s = 98 -> \n" + 
    		"	1 : (s' = 101);\n" + 
    		"[tick] s = 97 -> \n" + 
    		"	1 : (s' = 102);\n" + 
    		"[tick] s = 95 -> \n" + 
    		"	0.185 : (s' = 103)\n" + 
    		"	+ 0.63 : (s' = 104)\n" + 
    		"	+ 0.185 : (s' = 105);\n" + 
    		"[tick] s = 105 -> \n" + 
    		"	1 : (s' = 106);\n" + 
    		"[tick] s = 104 -> \n" + 
    		"	1 : (s' = 107);\n" + 
    		"[tick] s = 103 -> \n" + 
    		"	1 : (s' = 108);\n" + 
    		"[tick] s = 94 -> \n" + 
    		"	0.185 : (s' = 109)\n" + 
    		"	+ 0.63 : (s' = 110)\n" + 
    		"	+ 0.185 : (s' = 111);\n" + 
    		"[tick] s = 111 -> \n" + 
    		"	1 : (s' = 112);\n" + 
    		"[tick] s = 110 -> \n" + 
    		"	1 : (s' = 113);\n" + 
    		"[tick] s = 109 -> \n" + 
    		"	1 : (s' = 114);\n" + 
    		"[tick] s = 70 -> \n" + 
    		"	0.185 : (s' = 115)\n" + 
    		"	+ 0.63 : (s' = 116)\n" + 
    		"	+ 0.185 : (s' = 117);\n" + 
    		"[tick] s = 117 -> \n" + 
    		"	0.185 : (s' = 118)\n" + 
    		"	+ 0.63 : (s' = 119)\n" + 
    		"	+ 0.185 : (s' = 120);\n" + 
    		"[tick] s = 120 -> \n" + 
    		"	1 : (s' = 121);\n" + 
    		"[tick] s = 119 -> \n" + 
    		"	1 : (s' = 122);\n" + 
    		"[tick] s = 118 -> \n" + 
    		"	1 : (s' = 123);\n" + 
    		"[tick] s = 116 -> \n" + 
    		"	0.185 : (s' = 124)\n" + 
    		"	+ 0.63 : (s' = 125)\n" + 
    		"	+ 0.185 : (s' = 126);\n" + 
    		"[tick] s = 126 -> \n" + 
    		"	1 : (s' = 127);\n" + 
    		"[tick] s = 125 -> \n" + 
    		"	1 : (s' = 128);\n" + 
    		"[tick] s = 124 -> \n" + 
    		"	1 : (s' = 129);\n" + 
    		"[tick] s = 115 -> \n" + 
    		"	0.185 : (s' = 130)\n" + 
    		"	+ 0.63 : (s' = 131)\n" + 
    		"	+ 0.185 : (s' = 132);\n" + 
    		"[tick] s = 132 -> \n" + 
    		"	1 : (s' = 133);\n" + 
    		"[tick] s = 131 -> \n" + 
    		"	1 : (s' = 134);\n" + 
    		"[tick] s = 130 -> \n" + 
    		"	1 : (s' = 135);\n" + 
    		"[tick] s = 1 -> \n" + 
    		"	0.185 : (s' = 136)\n" + 
    		"	+ 0.63 : (s' = 137)\n" + 
    		"	+ 0.185 : (s' = 138);\n" + 
    		"[tick] s = 138 -> \n" + 
    		"	0.185 : (s' = 139)\n" + 
    		"	+ 0.63 : (s' = 140)\n" + 
    		"	+ 0.185 : (s' = 141);\n" + 
    		"[tick] s = 141 -> \n" + 
    		"	0.185 : (s' = 142)\n" + 
    		"	+ 0.63 : (s' = 143)\n" + 
    		"	+ 0.185 : (s' = 144);\n" + 
    		"[tick] s = 144 -> \n" + 
    		"	1 : (s' = 145);\n" + 
    		"[tick] s = 143 -> \n" + 
    		"	1 : (s' = 146);\n" + 
    		"[tick] s = 142 -> \n" + 
    		"	1 : (s' = 147);\n" + 
    		"[tick] s = 140 -> \n" + 
    		"	0.185 : (s' = 148)\n" + 
    		"	+ 0.63 : (s' = 149)\n" + 
    		"	+ 0.185 : (s' = 150);\n" + 
    		"[tick] s = 150 -> \n" + 
    		"	1 : (s' = 151);\n" + 
    		"[tick] s = 149 -> \n" + 
    		"	1 : (s' = 152);\n" + 
    		"[tick] s = 148 -> \n" + 
    		"	1 : (s' = 153);\n" + 
    		"[tick] s = 139 -> \n" + 
    		"	0.185 : (s' = 154)\n" + 
    		"	+ 0.63 : (s' = 155)\n" + 
    		"	+ 0.185 : (s' = 156);\n" + 
    		"[tick] s = 156 -> \n" + 
    		"	1 : (s' = 157);\n" + 
    		"[tick] s = 155 -> \n" + 
    		"	1 : (s' = 158);\n" + 
    		"[tick] s = 154 -> \n" + 
    		"	1 : (s' = 159);\n" + 
    		"[tick] s = 137 -> \n" + 
    		"	0.185 : (s' = 160)\n" + 
    		"	+ 0.63 : (s' = 161)\n" + 
    		"	+ 0.185 : (s' = 162);\n" + 
    		"[tick] s = 162 -> \n" + 
    		"	0.185 : (s' = 163)\n" + 
    		"	+ 0.63 : (s' = 164)\n" + 
    		"	+ 0.185 : (s' = 165);\n" + 
    		"[tick] s = 165 -> \n" + 
    		"	1 : (s' = 166);\n" + 
    		"[tick] s = 164 -> \n" + 
    		"	1 : (s' = 167);\n" + 
    		"[tick] s = 163 -> \n" + 
    		"	1 : (s' = 168);\n" + 
    		"[tick] s = 161 -> \n" + 
    		"	0.185 : (s' = 169)\n" + 
    		"	+ 0.63 : (s' = 170)\n" + 
    		"	+ 0.185 : (s' = 171);\n" + 
    		"[tick] s = 171 -> \n" + 
    		"	1 : (s' = 172);\n" + 
    		"[tick] s = 170 -> \n" + 
    		"	1 : (s' = 173);\n" + 
    		"[tick] s = 169 -> \n" + 
    		"	1 : (s' = 174);\n" + 
    		"[tick] s = 160 -> \n" + 
    		"	0.185 : (s' = 175)\n" + 
    		"	+ 0.63 : (s' = 176)\n" + 
    		"	+ 0.185 : (s' = 177);\n" + 
    		"[tick] s = 177 -> \n" + 
    		"	1 : (s' = 178);\n" + 
    		"[tick] s = 176 -> \n" + 
    		"	1 : (s' = 179);\n" + 
    		"[tick] s = 175 -> \n" + 
    		"	1 : (s' = 180);\n" + 
    		"[tick] s = 136 -> \n" + 
    		"	0.185 : (s' = 181)\n" + 
    		"	+ 0.63 : (s' = 182)\n" + 
    		"	+ 0.185 : (s' = 183);\n" + 
    		"[tick] s = 183 -> \n" + 
    		"	0.185 : (s' = 184)\n" + 
    		"	+ 0.63 : (s' = 185)\n" + 
    		"	+ 0.185 : (s' = 186);\n" + 
    		"[tick] s = 186 -> \n" + 
    		"	1 : (s' = 187);\n" + 
    		"[tick] s = 185 -> \n" + 
    		"	1 : (s' = 188);\n" + 
    		"[tick] s = 184 -> \n" + 
    		"	1 : (s' = 189);\n" + 
    		"[tick] s = 182 -> \n" + 
    		"	0.185 : (s' = 190)\n" + 
    		"	+ 0.63 : (s' = 191)\n" + 
    		"	+ 0.185 : (s' = 192);\n" + 
    		"[tick] s = 192 -> \n" + 
    		"	1 : (s' = 193);\n" + 
    		"[tick] s = 191 -> \n" + 
    		"	1 : (s' = 194);\n" + 
    		"[tick] s = 190 -> \n" + 
    		"	1 : (s' = 195);\n" + 
    		"[tick] s = 181 -> \n" + 
    		"	0.185 : (s' = 196)\n" + 
    		"	+ 0.63 : (s' = 197)\n" + 
    		"	+ 0.185 : (s' = 198);\n" + 
    		"[tick] s = 198 -> \n" + 
    		"	1 : (s' = 199);\n" + 
    		"[tick] s = 197 -> \n" + 
    		"	1 : (s' = 200);\n" + 
    		"[tick] s = 196 -> \n" + 
    		"	1 : (s' = 201);\n" + 
    		"[tick] (s = 13 | s = 14 | s = 15 | s = 19 | s = 20 | s = 21 | s = 25 | s = 26 | s = 27 | s = 34 | s = 35 | s = 36 | s = 40 | s = 41 | s = 42 | s = 46 | s = 47 | s = 48 | s = 55 | s = 56 | s = 57 | s = 61 | s = 62 | s = 63 | s = 67 | s = 68 | s = 69 | s = 79 | s = 80 | s = 81 | s = 85 | s = 86 | s = 87 | s = 91 | s = 92 | s = 93 | s = 100 | s = 101 | s = 102 | s = 106 | s = 107 | s = 108 | s = 112 | s = 113 | s = 114 | s = 121 | s = 122 | s = 123 | s = 127 | s = 128 | s = 129 | s = 133 | s = 134 | s = 135 | s = 145 | s = 146 | s = 147 | s = 151 | s = 152 | s = 153 | s = 157 | s = 158 | s = 159 | s = 166 | s = 167 | s = 168 | s = 172 | s = 173 | s = 174 | s = 178 | s = 179 | s = 180 | s = 187 | s = 188 | s = 189 | s = 193 | s = 194 | s = 195 | s = 199 | s = 200 | s = 201) -> 1 : true;\n" + 
    		"endmodule\n" + 
    		"formula stateValue = (s = 0 ? 0.00542219 : 0) + \n" + 
    		"                     (s = 3 ? 0.0058019 : 0) + \n" + 
    		"                     (s = 6 ? 0.00613341 : 0) + \n" + 
    		"                     (s = 9 ? 0.00647093 : 0) + \n" + 
    		"                     (s = 12 ? 0.00683459 : 0) + \n" + 
    		"                     (s = 13 ? 0.006665 : 0) + \n" + 
    		"                     (s = 11 ? 0.00634498 : 0) + \n" + 
    		"                     (s = 14 ? 0.00623415 : 0) + \n" + 
    		"                     (s = 10 ? 0.00585538 : 0) + \n" + 
    		"                     (s = 15 ? 0.00580329 : 0) + \n" + 
    		"                     (s = 8 ? 0.00604796 : 0) + \n" + 
    		"                     (s = 18 ? 0.00638176 : 0) + \n" + 
    		"                     (s = 19 ? 0.00626651 : 0) + \n" + 
    		"                     (s = 17 ? 0.00597277 : 0) + \n" + 
    		"                     (s = 20 ? 0.0059066 : 0) + \n" + 
    		"                     (s = 16 ? 0.00556378 : 0) + \n" + 
    		"                     (s = 21 ? 0.00554669 : 0) + \n" + 
    		"                     (s = 7 ? 0.00562499 : 0) + \n" + 
    		"                     (s = 24 ? 0.00596396 : 0) + \n" + 
    		"                     (s = 25 ? 0.00589885 : 0) + \n" + 
    		"                     (s = 23 ? 0.00560055 : 0) + \n" + 
    		"                     (s = 26 ? 0.00557905 : 0) + \n" + 
    		"                     (s = 22 ? 0.00523714 : 0) + \n" + 
    		"                     (s = 27 ? 0.00525925 : 0) + \n" + 
    		"                     (s = 5 ? 0.00575623 : 0) + \n" + 
    		"                     (s = 30 ? 0.00608888 : 0) + \n" + 
    		"                     (s = 33 ? 0.00642429 : 0) + \n" + 
    		"                     (s = 34 ? 0.00630394 : 0) + \n" + 
    		"                     (s = 32 ? 0.00600878 : 0) + \n" + 
    		"                     (s = 35 ? 0.00593829 : 0) + \n" + 
    		"                     (s = 31 ? 0.00559326 : 0) + \n" + 
    		"                     (s = 36 ? 0.00557263 : 0) + \n" + 
    		"                     (s = 29 ? 0.00571605 : 0) + \n" + 
    		"                     (s = 39 ? 0.00605014 : 0) + \n" + 
    		"                     (s = 40 ? 0.00597469 : 0) + \n" + 
    		"                     (s = 38 ? 0.00568068 : 0) + \n" + 
    		"                     (s = 41 ? 0.00564956 : 0) + \n" + 
    		"                     (s = 37 ? 0.00531122 : 0) + \n" + 
    		"                     (s = 42 ? 0.00532444 : 0) + \n" + 
    		"                     (s = 28 ? 0.00534321 : 0) + \n" + 
    		"                     (s = 45 ? 0.00571214 : 0) + \n" + 
    		"                     (s = 46 ? 0.00567724 : 0) + \n" + 
    		"                     (s = 44 ? 0.00535259 : 0) + \n" + 
    		"                     (s = 47 ? 0.00536084 : 0) + \n" + 
    		"                     (s = 43 ? 0.00499304 : 0) + \n" + 
    		"                     (s = 48 ? 0.00504444 : 0) + \n" + 
    		"                     (s = 4 ? 0.00537906 : 0) + \n" + 
    		"                     (s = 51 ? 0.0057429 : 0) + \n" + 
    		"                     (s = 54 ? 0.00607598 : 0) + \n" + 
    		"                     (s = 55 ? 0.00599742 : 0) + \n" + 
    		"                     (s = 53 ? 0.00570431 : 0) + \n" + 
    		"                     (s = 56 ? 0.00567036 : 0) + \n" + 
    		"                     (s = 52 ? 0.00533264 : 0) + \n" + 
    		"                     (s = 57 ? 0.00534329 : 0) + \n" + 
    		"                     (s = 50 ? 0.00538413 : 0) + \n" + 
    		"                     (s = 60 ? 0.00574728 : 0) + \n" + 
    		"                     (s = 61 ? 0.00570817 : 0) + \n" + 
    		"                     (s = 59 ? 0.0053886 : 0) + \n" + 
    		"                     (s = 62 ? 0.00539253 : 0) + \n" + 
    		"                     (s = 58 ? 0.00502991 : 0) + \n" + 
    		"                     (s = 63 ? 0.00507688 : 0) + \n" + 
    		"                     (s = 49 ? 0.00502537 : 0) + \n" + 
    		"                     (s = 66 ? 0.00545493 : 0) + \n" + 
    		"                     (s = 67 ? 0.0054509 : 0) + \n" + 
    		"                     (s = 65 ? 0.00507288 : 0) + \n" + 
    		"                     (s = 68 ? 0.0051147 : 0) + \n" + 
    		"                     (s = 64 ? 0.00469084 : 0) + \n" + 
    		"                     (s = 69 ? 0.0047785 : 0) + \n" + 
    		"                     (s = 2 ? 0.00543652 : 0) + \n" + 
    		"                     (s = 72 ? 0.00579298 : 0) + \n" + 
    		"                     (s = 75 ? 0.00612467 : 0) + \n" + 
    		"                     (s = 78 ? 0.00646176 : 0) + \n" + 
    		"                     (s = 79 ? 0.00633691 : 0) + \n" + 
    		"                     (s = 77 ? 0.00604027 : 0) + \n" + 
    		"                     (s = 80 ? 0.005966 : 0) + \n" + 
    		"                     (s = 76 ? 0.00561879 : 0) + \n" + 
    		"                     (s = 81 ? 0.0055951 : 0) + \n" + 
    		"                     (s = 74 ? 0.00574839 : 0) + \n" + 
    		"                     (s = 84 ? 0.00608128 : 0) + \n" + 
    		"                     (s = 85 ? 0.00600209 : 0) + \n" + 
    		"                     (s = 83 ? 0.00570914 : 0) + \n" + 
    		"                     (s = 86 ? 0.00567461 : 0) + \n" + 
    		"                     (s = 82 ? 0.005337 : 0) + \n" + 
    		"                     (s = 87 ? 0.00534712 : 0) + \n" + 
    		"                     (s = 73 ? 0.0053721 : 0) + \n" + 
    		"                     (s = 90 ? 0.0057369 : 0) + \n" + 
    		"                     (s = 91 ? 0.00569903 : 0) + \n" + 
    		"                     (s = 89 ? 0.00537801 : 0) + \n" + 
    		"                     (s = 92 ? 0.00538321 : 0) + \n" + 
    		"                     (s = 88 ? 0.00501912 : 0) + \n" + 
    		"                     (s = 93 ? 0.00506739 : 0) + \n" + 
    		"                     (s = 71 ? 0.0054347 : 0) + \n" + 
    		"                     (s = 96 ? 0.00579138 : 0) + \n" + 
    		"                     (s = 99 ? 0.00612311 : 0) + \n" + 
    		"                     (s = 100 ? 0.00603889 : 0) + \n" + 
    		"                     (s = 98 ? 0.00574698 : 0) + \n" + 
    		"                     (s = 101 ? 0.0057079 : 0) + \n" + 
    		"                     (s = 97 ? 0.00537085 : 0) + \n" + 
    		"                     (s = 102 ? 0.00537691 : 0) + \n" + 
    		"                     (s = 95 ? 0.00543309 : 0) + \n" + 
    		"                     (s = 105 ? 0.00578997 : 0) + \n" + 
    		"                     (s = 106 ? 0.00574574 : 0) + \n" + 
    		"                     (s = 104 ? 0.00543168 : 0) + \n" + 
    		"                     (s = 107 ? 0.00543044 : 0) + \n" + 
    		"                     (s = 103 ? 0.0050734 : 0) + \n" + 
    		"                     (s = 108 ? 0.00511515 : 0) + \n" + 
    		"                     (s = 94 ? 0.00507481 : 0) + \n" + 
    		"                     (s = 111 ? 0.0054932 : 0) + \n" + 
    		"                     (s = 112 ? 0.00548458 : 0) + \n" + 
    		"                     (s = 110 ? 0.00511639 : 0) + \n" + 
    		"                     (s = 113 ? 0.00515299 : 0) + \n" + 
    		"                     (s = 109 ? 0.00473959 : 0) + \n" + 
    		"                     (s = 114 ? 0.0048214 : 0) + \n" + 
    		"                     (s = 70 ? 0.00507641 : 0) + \n" + 
    		"                     (s = 117 ? 0.00549445 : 0) + \n" + 
    		"                     (s = 120 ? 0.00584445 : 0) + \n" + 
    		"                     (s = 121 ? 0.00579368 : 0) + \n" + 
    		"                     (s = 119 ? 0.00548568 : 0) + \n" + 
    		"                     (s = 122 ? 0.00547796 : 0) + \n" + 
    		"                     (s = 118 ? 0.0051269 : 0) + \n" + 
    		"                     (s = 123 ? 0.00516224 : 0) + \n" + 
    		"                     (s = 116 ? 0.0051178 : 0) + \n" + 
    		"                     (s = 126 ? 0.00552697 : 0) + \n" + 
    		"                     (s = 127 ? 0.0055143 : 0) + \n" + 
    		"                     (s = 125 ? 0.00515423 : 0) + \n" + 
    		"                     (s = 128 ? 0.00518628 : 0) + \n" + 
    		"                     (s = 124 ? 0.00478148 : 0) + \n" + 
    		"                     (s = 129 ? 0.00485827 : 0) + \n" + 
    		"                     (s = 115 ? 0.00474116 : 0) + \n" + 
    		"                     (s = 132 ? 0.00524549 : 0) + \n" + 
    		"                     (s = 133 ? 0.00526659 : 0) + \n" + 
    		"                     (s = 131 ? 0.00482278 : 0) + \n" + 
    		"                     (s = 134 ? 0.00489461 : 0) + \n" + 
    		"                     (s = 130 ? 0.00440007 : 0) + \n" + 
    		"                     (s = 135 ? 0.00452262 : 0) + \n" + 
    		"                     (s = 1 ? 0.00507114 : 0) + \n" + 
    		"                     (s = 138 ? 0.00549034 : 0) + \n" + 
    		"                     (s = 141 ? 0.00584076 : 0) + \n" + 
    		"                     (s = 144 ? 0.00617171 : 0) + \n" + 
    		"                     (s = 145 ? 0.00608167 : 0) + \n" + 
    		"                     (s = 143 ? 0.00579043 : 0) + \n" + 
    		"                     (s = 146 ? 0.00574614 : 0) + \n" + 
    		"                     (s = 142 ? 0.00540915 : 0) + \n" + 
    		"                     (s = 147 ? 0.00541062 : 0) + \n" + 
    		"                     (s = 140 ? 0.00548206 : 0) + \n" + 
    		"                     (s = 150 ? 0.00583336 : 0) + \n" + 
    		"                     (s = 151 ? 0.00578392 : 0) + \n" + 
    		"                     (s = 149 ? 0.00547477 : 0) + \n" + 
    		"                     (s = 152 ? 0.00546836 : 0) + \n" + 
    		"                     (s = 148 ? 0.00511618 : 0) + \n" + 
    		"                     (s = 153 ? 0.0051528 : 0) + \n" + 
    		"                     (s = 139 ? 0.00512335 : 0) + \n" + 
    		"                     (s = 156 ? 0.00553137 : 0) + \n" + 
    		"                     (s = 157 ? 0.00551816 : 0) + \n" + 
    		"                     (s = 155 ? 0.00515911 : 0) + \n" + 
    		"                     (s = 158 ? 0.00519058 : 0) + \n" + 
    		"                     (s = 154 ? 0.00478685 : 0) + \n" + 
    		"                     (s = 159 ? 0.00486299 : 0) + \n" + 
    		"                     (s = 137 ? 0.00511316 : 0) + \n" + 
    		"                     (s = 162 ? 0.0055233 : 0) + \n" + 
    		"                     (s = 165 ? 0.00587045 : 0) + \n" + 
    		"                     (s = 166 ? 0.00581656 : 0) + \n" + 
    		"                     (s = 164 ? 0.00551107 : 0) + \n" + 
    		"                     (s = 167 ? 0.0055003 : 0) + \n" + 
    		"                     (s = 163 ? 0.00515169 : 0) + \n" + 
    		"                     (s = 168 ? 0.00518405 : 0) + \n" + 
    		"                     (s = 161 ? 0.00515014 : 0) + \n" + 
    		"                     (s = 171 ? 0.00555269 : 0) + \n" + 
    		"                     (s = 172 ? 0.00553693 : 0) + \n" + 
    		"                     (s = 170 ? 0.00518269 : 0) + \n" + 
    		"                     (s = 173 ? 0.00521133 : 0) + \n" + 
    		"                     (s = 169 ? 0.00481268 : 0) + \n" + 
    		"                     (s = 174 ? 0.00488572 : 0) + \n" + 
    		"                     (s = 160 ? 0.00477698 : 0) + \n" + 
    		"                     (s = 177 ? 0.005271 : 0) + \n" + 
    		"                     (s = 178 ? 0.00528904 : 0) + \n" + 
    		"                     (s = 176 ? 0.00485431 : 0) + \n" + 
    		"                     (s = 179 ? 0.00492235 : 0) + \n" + 
    		"                     (s = 175 ? 0.00443761 : 0) + \n" + 
    		"                     (s = 180 ? 0.00455566 : 0) + \n" + 
    		"                     (s = 136 ? 0.00473599 : 0) + \n" + 
    		"                     (s = 183 ? 0.00524183 : 0) + \n" + 
    		"                     (s = 186 ? 0.00562712 : 0) + \n" + 
    		"                     (s = 187 ? 0.00560243 : 0) + \n" + 
    		"                     (s = 185 ? 0.00526337 : 0) + \n" + 
    		"                     (s = 188 ? 0.00528233 : 0) + \n" + 
    		"                     (s = 184 ? 0.00489962 : 0) + \n" + 
    		"                     (s = 189 ? 0.00496223 : 0) + \n" + 
    		"                     (s = 182 ? 0.00481823 : 0) + \n" + 
    		"                     (s = 192 ? 0.00530066 : 0) + \n" + 
    		"                     (s = 193 ? 0.00531514 : 0) + \n" + 
    		"                     (s = 191 ? 0.0048906 : 0) + \n" + 
    		"                     (s = 194 ? 0.00495429 : 0) + \n" + 
    		"                     (s = 190 ? 0.00448054 : 0) + \n" + 
    		"                     (s = 195 ? 0.00459344 : 0) + \n" + 
    		"                     (s = 181 ? 0.00439463 : 0) + \n" + 
    		"                     (s = 198 ? 0.00500909 : 0) + \n" + 
    		"                     (s = 199 ? 0.00505856 : 0) + \n" + 
    		"                     (s = 197 ? 0.00451784 : 0) + \n" + 
    		"                     (s = 200 ? 0.00462626 : 0) + \n" + 
    		"                     (s = 196 ? 0.00402658 : 0) + \n" + 
    		"                     (s = 201 ? 0.00419396 : 0);";
    
    protected void initializeAdaptationMgr(SwimModelHelper swimModel) {
        log("Starting SWIMEXT HP Adaptation Manager initialization");
        computeDecisionHorizon(swimModel);
        m_currentTime = 0;
        m_adaptMgr = new PMCAdaptationManager();
        m_planDB = PlanDB.get_instance();
        

        log("begin testing...");
        StringVector sv = new StringVector();
        m_tspModel.getModelInstance().observe(0.00727339);
        m_tspModel.getModelInstance().observe(0.00688481);
        m_tspModel.getModelInstance().observe(0.00626385);
        m_tspModel.getModelInstance().observe(0.00603236);
        m_tspModel.getModelInstance().observe(0.00655949);
        log("after observations");
        generateEnvironmentModel(); generateEnvironmentModel();
        log("after generations");
        AdaptationPlanner m_adaptPlanner = new AdaptationPlanner();
        m_adaptPlanner.setModelTemplatePath("/home/frank/Sandbox/plasasim/templates/final_ibl.prism");
        //sv = m_adaptPlanner.plan("foooo", "barrrr", "/home/frank/PrismDump", true);
        //sv = m_adaptPlanner.plan(env_mod_r, init_state_r, "/home/frank/PrismDump", true);
        log("size: " + sv.size());
        for(int i = 0; i < sv.size(); ++i) {
        	log("R" + i + ": " + sv.get(i));
        }
        // sv.clear();
        sv = m_adaptPlanner.plan(env_mod_d, init_state_d, "/home/frank/PrismDump", false);
        log("size: " + sv.size());
        for(int i = 0; i < sv.size(); ++i) {
        	log("D" + i + ": " + sv.get(i));
        }
        m_planDB.update_val(5, m_currentTime, 1, 0, 0, 1, 4, 0, 0, 0, 60, 60, 0, 0.03);
        log("updated");
        String path = m_adaptPlanner.getPlanned_path();
        log("path is: " + path);
        if(m_planDB.populate_db(path)) {
        	log("success!");
        } else {
        	log("fail!");
        }
        m_planDB.get_plan();
        sv = m_planDB.getActions();
        log("size: " + sv.size());
        for(int i = 0; i < sv.size(); ++i) {
            log("D" + i + ": " + sv.get(i));
        }

        m_isInitialized = true;
        log("m_isInitialized set to true, finished initialization");
    }
    
    private void computeDecisionHorizon(SwimModelHelper swimModel) {
        
        log("max servers: " + swimModel.getMaxServers());
        log("latency: " + swimModel.getAddServerLatencyPeriods());
        m_horizon = (int) Math.max(5.0, 
                swimModel.getAddServerLatencyPeriods() * (swimModel.getMaxServers() - 1) + 1);
        
        // set value in tsp model
        ModelReference modelRef = new ModelReference(TSP_MODEL, TimeSeriesPredictorModelInstance.MODEL_TYPE);
        m_tspModel = (TimeSeriesPredictorModelInstance) m_modelsManagerPort.<TimeSeriesPredictorModel>getModelInstance (modelRef);
        try {
            m_tspModel.getModelInstance().setHorizon(m_horizon);
        } catch (RainbowException e) {
            m_reportingPort.error (RainbowComponentT.ADAPTATION_MANAGER, e.toString());
        }
    }

    private EnvironmentDTMCPartitioned generateEnvironmentModel() {
        return m_tspModel.getModelInstance().generateEnvironmentDTMC(2, m_horizon);
    } 
    
    private String get_initial_state_str (SwimModelHelper swimModel, boolean fastPlanning) {
    	String res = "";
    	res += "const double addServer_LATENCY = ";
        res += swimModel.getAddServerLatencySec();
        res += ";\n";

        res += "const int HORIZON = ";
        res += m_horizon;
        res += ";\n";
        
        res += "const double PERIOD = ";
        res += SLEEP_TIME / 1000; //evaluation period, in sec(?)
        res += ";\n";
        
        res += "const int DIMMER_LEVELS = ";
        res += swimModel.getDimmerLevels();
        res += ";\n";
        
        res += "const int ini_dimmer = ";
        // int discretizedBrownoutFactor = 1 + (hpModel.numberOfBrownoutLevels - 1) * (int)(hpModel.brownoutFactor);
        int discretizedBrownoutFactor = 1 + (swimModel.getDimmerLevels() - 1) * (int)(swimModel.getCurrentDimmer());
        res += discretizedBrownoutFactor;
        res += ";\n";
        
        res += "const int MAX_SERVERS_A = ";
        res += 1; //hardcoded
        res += ";\n";
        
        res += "const int MAX_SERVERS_B = ";
        res += 1; //hardcoded
        res += ";\n";
        
        res += "const int MAX_SERVERS_C = ";
        res += 1; //hardcoded
        res += ";\n";
        
        res += "const int ini_servers_A = ";
        res += getBoolVal(m_model.getModelInstance().getComponent("server1").getProperty("isActive")) ? 1 : 0;
        res += ";\n";
        
        res += "const int ini_servers_B = ";
        res += getBoolVal(m_model.getModelInstance().getComponent("server2").getProperty("isActive")) ? 1 : 0;
        res += ";\n";
        
        res += "const int ini_servers_C = ";
        res += getBoolVal(m_model.getModelInstance().getComponent("server3").getProperty("isActive")) ? 1 : 0;
        res += ";\n";
        
        int addServerAState = 0;
        int addServerBState = 0;
        int addServerCState = 0;

        int addServerState = swimModel.getAddServerTacticProgress();
        int bootType = getBootType();
        if(bootType == 1)
            addServerAState = addServerState;
        if(bootType == 2)
            addServerBState = addServerState;
        if(bootType == 3)
            addServerCState = addServerState;

        res += "const int ini_addServerA_state = ";
        res += addServerAState;
        res += ";\n";
        
        res += "const int ini_addServerB_state = ";
        res += addServerBState;
        res += ";\n";
        
        res += "const int ini_addServerC_state = ";
        res += addServerCState;
        res += ";\n";
        
        res += "const double SERVERA_COST_SEC = ";
        res += getDoubleVal(m_model.getModelInstance().getComponent("server1").getProperty("cost"));
        res += ";\n";
        
        res += "const double SERVERB_COST_SEC = ";
        res += getDoubleVal(m_model.getModelInstance().getComponent("server2").getProperty("cost"));
        res += ";\n";
        
        res += "const double SERVERC_COST_SEC = ";
        res += getDoubleVal(m_model.getModelInstance().getComponent("server3").getProperty("cost"));
        res += ";\n";
        
        res += "const double MAX_ARRIVALA_CAPACITY = ";
        res += getIntVal(m_model.getModelInstance().getComponent("server1").getProperty("max_arrival_capacity"));
        res += ";\n";
        
        res += "const double MAX_ARRIVALA_CAPACITY_LOW = ";
        res += getIntVal(m_model.getModelInstance().getComponent("server1").getProperty("max_arrival_capacity_low"));
        res += ";\n";
        
        res += "const double MAX_ARRIVALB_CAPACITY = ";
        res += getIntVal(m_model.getModelInstance().getComponent("server2").getProperty("max_arrival_capacity"));
        res += ";\n";
        
        res += "const double MAX_ARRIVALB_CAPACITY_LOW = ";
        res += getIntVal(m_model.getModelInstance().getComponent("server2").getProperty("max_arrival_capacity_low"));
        res += ";\n";
        
        res += "const double MAX_ARRIVALC_CAPACITY = ";
        res += getIntVal(m_model.getModelInstance().getComponent("server3").getProperty("max_arrival_capacity"));
        res += ";\n";
        
        res += "const double MAX_ARRIVALC_CAPACITY_LOW = ";
        res += getIntVal(m_model.getModelInstance().getComponent("server3").getProperty("max_arrival_capacity_low"));
        res += ";\n";
        
        res += "const double penalty = ";
        res += -0.25; //TODO: hardcoded from swimExtention.ini
        res += ";\n";
        
        res += "const int ini_traffic_A = ";
        res += (int) getDoubleVal(m_model.getModelInstance().getComponent("server1").getProperty("traffic"));
        res += ";\n";
        
        res += "const int ini_traffic_B = ";
        res += (int) getDoubleVal(m_model.getModelInstance().getComponent("server2").getProperty("traffic"));
        res += ";\n";
        
        res += "const int ini_traffic_C = ";
        res += (int) getDoubleVal(m_model.getModelInstance().getComponent("server3").getProperty("traffic"));
        res += ";\n";
        
        //useInterArrivalScaleFactorForFast(Slow)Planning (?)
        if(fastPlanning){
            res += "const double interArrivalScaleFactorForDecision = 1;\n";
        } else {
            res += "const double interArrivalScaleFactorForDecision = 1;\n";
        }

    	return res;
    }
    
    protected SwimExtendedPlan parsePlan (String plan) {
        SwimExtendedPlan res = null;
        if(plan.equals("addServerA_start"))
        	res = new AddServerPlan(m_model, "1");
        else if(plan.equals("addServerB_start"))
        	res = new AddServerPlan(m_model, "2");
        else if(plan.equals("addServerC_start"))
        	res = new AddServerPlan(m_model, "3");
        else if(plan.equals("removeServerA_start"))
        	res = new RemoveServerPlan(m_model, "1");
        else if(plan.equals("removeServerB_start"))
        	res = new RemoveServerPlan(m_model, "2");
        else if(plan.equals("removeServerC_start"))
        	res = new RemoveServerPlan(m_model, "3");
        else if(plan.equals("increaseDimmer_start"))
        	res = new IncDimmerPlan(m_model);
        else if(plan.equals("decreaseDimmer_start"))
        	res = new DecDimmerPlan(m_model);
        else if(plan.equals("divert_100_0_0"))
        	res = new DivertTrafficPlan(m_model, "divert_100_0_0");
        else if(plan.equals("divert_75_25_0"))
        	res = new DivertTrafficPlan(m_model, "divert_75_25_0");
        else if(plan.equals("divert_75_0_25"))
        	res = new DivertTrafficPlan(m_model, "divert_75_0_25");
        else if(plan.equals("divert_50_50_0"))
        	res = new DivertTrafficPlan(m_model, "divert_50_50_0");
        else if(plan.equals("divert_50_0_50"))
        	res = new DivertTrafficPlan(m_model, "divert_50_0_50");
        else if(plan.equals("divert_50_25_25"))
        	res = new DivertTrafficPlan(m_model, "divert_50_25_25");
        else if(plan.equals("divert_25_75_0"))
        	res = new DivertTrafficPlan(m_model, "divert_25_75_0");
        else if(plan.equals("divert_25_0_75"))
        	res = new DivertTrafficPlan(m_model, "divert_25_0_75");
        else if(plan.equals("divert_25_50_25"))
        	res = new DivertTrafficPlan(m_model, "divert_25_50_25");
        else if(plan.equals("divert_25_25_50"))
        	res = new DivertTrafficPlan(m_model, "divert_25_25_50");
        else if(plan.equals("divert_0_100_0"))
        	res = new DivertTrafficPlan(m_model, "divert_0_100_0");
        else if(plan.equals("divert_0_0_100"))
        	res = new DivertTrafficPlan(m_model, "divert_0_0_100");
        else if(plan.equals("divert_0_75_25"))
        	res = new DivertTrafficPlan(m_model, "divert_0_75_25");
        else if(plan.equals("divert_0_25_75"))
        	res = new DivertTrafficPlan(m_model, "divert_0_25_75");
        else if(plan.equals("divert_0_50_50"))
        	res = new DivertTrafficPlan(m_model, "divert_0_50_50");
        
        return res;
    }

    protected AdaptationTree<SwimExtendedPlan> parseActions (StringVector actions){
        AdaptationTree<SwimExtendedPlan> at = new AdaptationTree<>(AdaptationExecutionOperatorT.PARALLEL);
        for(int i = 0; i < actions.size(); ++i) {
        	at.addLeaf(parsePlan(actions.get(i)));
        }
        return at;
    }


	@Override
	public void dispose() {
        if (m_adaptationEnqueuePort != null) {
            m_adaptationEnqueuePort.dispose ();
        }
        if (m_modelChangePort != null) {
            m_modelChangePort.dispose ();
        }

        // null-out data members
        m_model = null;
	}

    @Override
    protected void log(String txt) {
        m_reportingPort.info (RainbowComponentT.ADAPTATION_MANAGER, txt);
    }

    protected void error (String txt) {
        m_reportingPort.error (RainbowComponentT.ADAPTATION_MANAGER, txt);
    }

    public void setAdaptationEnabled (boolean b) {
        m_adaptationEnabled = b;
    }

    public boolean adaptationInProgress () {
        return m_adaptationNeeded;
    }


    @Override
    public void markStrategyExecuted(AdaptationTree<SwimExtendedPlan> plan) {
        AdaptationResultsVisitor v = new AdaptationResultsVisitor (plan);
        plan.visit(v);
        if (v.m_allOk) {
        	log("Finished adapting the system");
        } else {
        	log("Something in the adaptation plan failed to execute.");
        }
        try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        m_executingPlan = false;
    }


	@Override
	public void onEvent(ModelReference reference, IRainbowMessage message) {
        // Because of the subscription, the model should be the model ref so no need to check
        String typecheckSt = (String) message.getProperty (IModelChangeBusPort.PARAMETER_PROP + "0");
        Boolean typechecks = Boolean.valueOf (typecheckSt);
        // Cause the thread to wake up if it is sleeping
        if (!typechecks) {
            activeThread ().interrupt ();
        }		
	}





	@Override
	public void setEnabled(boolean enabled) {
        m_reportingPort.info (getComponentType (),
                              MessageFormat.format ("Turning adaptation {0}.", (enabled ? "on" : "off")));
        // if (!enabled && !m_pendingStrategies.isEmpty ()) {
        //     m_reportingPort.info (getComponentType (), "There is an adaptation in progress. This will finish.");
        // }
        // m_adaptEnabled = enabled;		
	}


	@Override
	public boolean isEnabled() {
        return true;
	}


    protected AdaptationTree<SwimExtendedPlan> checkAdaptation () {
        boolean decide = true;
        AdaptationTree<SwimExtendedPlan> at = null;
        SwimModelHelper swimModel = new SwimModelHelper(m_model);
        if (!m_isInitialized) {
            initializeAdaptationMgr(swimModel);
        }

        EnvironmentDTMCPartitioned env = generateEnvironmentModel();
        if (env == null) {
            log("No environment observations available. Can't make adaptation decision");
            decide = false;
        }
        if (decide) {
            at = checkAdaptationImpl (swimModel, env);
            log("Adaptation decision time DNE");
        }

        log ("About to return at");
        return at;
    }
    
    protected int getIntVal (IAcmeProperty val) {
    	return (int) PropertyHelper.toJavaVal(val.getValue());
    }
    protected double getDoubleVal (IAcmeProperty val) {
    	return (double) PropertyHelper.toJavaVal(val.getValue());
    }
    protected boolean getBoolVal (IAcmeProperty val) {
    	return (boolean) PropertyHelper.toJavaVal(val.getValue());
    }
    
    protected int getBootType () {
    	if(getBoolVal(m_model.getModelInstance().getComponent("server1").getProperty("isActive")) 
    	!= getBoolVal(m_model.getModelInstance().getComponent("server1").getProperty("isArchEnabled")))
    		return 1;
    	else if (getBoolVal(m_model.getModelInstance().getComponent("server2").getProperty("isActive")) 
    	      != getBoolVal(m_model.getModelInstance().getComponent("server2").getProperty("isArchEnabled")))
    		return 2;
    	else if (getBoolVal(m_model.getModelInstance().getComponent("server3").getProperty("isActive")) 
      	      != getBoolVal(m_model.getModelInstance().getComponent("server3").getProperty("isArchEnabled")))
      		return 3;
    	else
    		return 0;
    }
    
    //class that defines fast planning procedure
	public class Reactive extends Thread {
		public SwimModelHelper swimModel;
		public boolean usePredictor;
		public String initialState;
		public String environmentModel;
		public StringVector actions;
		public String ret_path;
		
		Reactive(SwimModelHelper swimModel, boolean usePredictor){
			this.swimModel = swimModel;
			this.usePredictor = usePredictor;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			initialState = get_initial_state_str(swimModel, true);
			if(usePredictor) {
    			environmentModel = "formula stateValue = " + ";\n";
    		} else {
    			environmentModel = "formula stateValue = " + swimModel.getArrivalRate() + ";\n";
    		}
			log("fast planning triggered");
            AdaptationPlanner m_adaptPlanner = new AdaptationPlanner();
            m_adaptPlanner.setModelTemplatePath("/home/frank/Sandbox/plasasim/templates/final_ibl.prism");
    		actions = m_adaptPlanner.plan(environmentModel, initialState, "", true);
    		String ret_path = m_adaptPlanner.getPlanned_path();
    		log("fast plan over, path = " + ret_path);
		}
		
	}
	
    //class that defines slow planning procedure
	public class Deliberative extends Thread {
		public SwimModelHelper swimModel;
		public boolean usePredictor;
		public String initialState;
		public String environmentModel;
		public String ret_path;
		
		Deliberative(SwimModelHelper swimModel, boolean usePredictor){
			this.swimModel = swimModel;
			this.usePredictor = usePredictor;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
        	initialState = get_initial_state_str(swimModel, false);
        	m_currentTime = 0; //reset current time for new plan
        	if(usePredictor) {
        		environmentModel = PMCAdaptationManager.generateEnvironmentDTMC(generateEnvironmentModel());
        	} else {
        		environmentModel = PMCAdaptationManager.generateEnvironmentDTMC(generateEnvironmentModel());
        	}
        	log("slow planning triggered");
            AdaptationPlanner m_adaptPlanner = new AdaptationPlanner();
            m_adaptPlanner.setModelTemplatePath("/home/frank/Sandbox/plasasim/templates/final_ibl.prism");
        	m_adaptPlanner.plan(environmentModel, initialState, "", false);
        	String ret_path = m_adaptPlanner.getPlanned_path();
        	log("slow plan over, path = " + ret_path);
        	m_planDB.clean_db();
        	m_planDB.populate_db(ret_path);
        	log("slow plan populate db over");
		}
	}
	

    protected AdaptationTree<SwimExtendedPlan> checkAdaptationImpl(SwimModelHelper swimModel,
            EnvironmentDTMCPartitioned env){
    	//first we update planDB
        m_planDB.update_val
        	(m_horizon, 
             m_currentTime, 
             getBoolVal(m_model.getModelInstance().getComponent("server1").getProperty("isActive")) ? 1 : 0,
             getBoolVal(m_model.getModelInstance().getComponent("server2").getProperty("isActive")) ? 1 : 0,
             getBoolVal(m_model.getModelInstance().getComponent("server3").getProperty("isActive")) ? 1 : 0,
             swimModel.getCurrentDimmerLevel(),
             (int) getDoubleVal(m_model.getModelInstance().getComponent("server1").getProperty("traffic")),
             (int) getDoubleVal(m_model.getModelInstance().getComponent("server2").getProperty("traffic")),
             (int) getDoubleVal(m_model.getModelInstance().getComponent("server3").getProperty("traffic")),
             swimModel.getAddServerTacticProgress(),
             swimModel.getAddServerLatencySec(),
             SLEEP_TIME / 1000, //evaluation period, in sec(?)
             getBootType(),
             swimModel.getArrivalRate() //should be mean arrival rate, need fix
             );
        
        //then we try to get plan
        StringVector actions = new StringVector();
        if(!m_planDB.get_plan()) {
        	if(m_horizon > m_currentTime) {
        		log("Plan Failed");
        	} else {
        		log("Plan Over");
        	}
        	
        	boolean usePredictor = false;
        	
        	//Generate a new plan
        	boolean trigger_fast_planning = false;
        	//check if fast planning needed against threshold
        	if(swimModel.getAverageResponseTime() >= 0) {
        		trigger_fast_planning = true;
        	}
        	
        	//trigger slow planning
        	Deliberative runD = new Deliberative(swimModel, usePredictor);
        	runD.start();
        	
        	if(trigger_fast_planning) {
        		//spawn a new reactive thread, trigger fast planning
        		Reactive runR = new Reactive(swimModel, usePredictor);
        		runR.start();
        		try {
					runR.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		actions = runR.actions;
        	}

        	
        } else {
        	actions = m_planDB.getActions();
        }
        
        //parse actions
        AdaptationTree<SwimExtendedPlan> at = parseActions(actions);
        log("actions to take: " + actions.size());
        for(int i = 0; i < actions.size(); ++i) {
        	log("#" + i + ": " + actions.get(i));
        	
        }
        return at;
    }



	@Override
	protected void runAction() {
        log("inside runAction");
		// double arrivalRate = getDoubleVal(m_model.getModelInstance().getComponent("LB0").getProperty("arrivalRate"));
		// m_tspModel.getModelInstance().observe(arrivalRate);
        m_currentTime++;
        if (m_adaptationEnabled && !m_executingPlan) {
            AdaptationTree<SwimExtendedPlan> at = checkAdaptation ();
            if (at != null) {
                //log (">> do strategy: " + at);
                m_adaptationEnqueuePort.offerAdaptation (at, new Object[0]);
                String logMessage = at.toString ();
                log("New plan generated. Enqueueing...");
                log(logMessage);
                m_executingPlan = true;
            }
        } else {
            log("occupied");
        } 

        //testing code
        String logInfo = "";
        logInfo += "Load Balancer:";
        logInfo += "\ndimmer: " + getDoubleVal(m_model.getModelInstance().getComponent("LB0").getProperty("dimmer"));
        logInfo += "\narrivalRate: " + getDoubleVal(m_model.getModelInstance().getComponent("LB0").getProperty("arrivalRate"));
        logInfo += "\naverageResponseTime: " + getDoubleVal(m_model.getModelInstance().getComponent("LB0").getProperty("averageResponseTime"));
        logInfo += "\nServer 1:";
        logInfo += "\ntraffic: " + getDoubleVal(m_model.getModelInstance().getComponent("server1").getProperty("traffic"));
        //logInfo += "\nisArchEnabled: " + getBoolVal(m_model.getModelInstance().getComponent("server1").getProperty("nisArchEnabled"));
        logInfo += "\nmax_arrival_capacity: " + getIntVal(m_model.getModelInstance().getComponent("server1").getProperty("max_arrival_capacity"));
        logInfo += "\nmax_arrival_capacity_low: " + getIntVal(m_model.getModelInstance().getComponent("server1").getProperty("max_arrival_capacity_low"));
        logInfo += "\nindex: " + getIntVal(m_model.getModelInstance().getComponent("server1").getProperty("index"));
        logInfo += "\nisActive: " + getBoolVal(m_model.getModelInstance().getComponent("server1").getProperty("isActive"));
        logInfo += "\nexpectedActivationTime: " + getIntVal(m_model.getModelInstance().getComponent("server1").getProperty("expectedActivationTime"));
        logInfo += "\nreqServiceRate: " + getDoubleVal(m_model.getModelInstance().getComponent("server1").getProperty("reqServiceRate"));
        logInfo += "\nbyteServiceRate: " + getDoubleVal(m_model.getModelInstance().getComponent("server1").getProperty("byteServiceRate"));
        logInfo += "\ncost: " + getDoubleVal(m_model.getModelInstance().getComponent("server1").getProperty("cost"));
        logInfo += "\nServer 2:";
        logInfo += "\ntraffic: " + getDoubleVal(m_model.getModelInstance().getComponent("server2").getProperty("traffic"));
        //logInfo += "\nisArchEnabled: " + getBoolVal(m_model.getModelInstance().getComponent("server2").getProperty("nisArchEnabled"));
        logInfo += "\nmax_arrival_capacity: " + getIntVal(m_model.getModelInstance().getComponent("server2").getProperty("max_arrival_capacity"));
        logInfo += "\nmax_arrival_capacity_low: " + getIntVal(m_model.getModelInstance().getComponent("server2").getProperty("max_arrival_capacity_low"));
        logInfo += "\nindex: " + getIntVal(m_model.getModelInstance().getComponent("server2").getProperty("index"));
        logInfo += "\nisActive: " + getBoolVal(m_model.getModelInstance().getComponent("server2").getProperty("isActive"));
        logInfo += "\nexpectedActivationTime: " + getIntVal(m_model.getModelInstance().getComponent("server2").getProperty("expectedActivationTime"));
        logInfo += "\nreqServiceRate: " + getDoubleVal(m_model.getModelInstance().getComponent("server2").getProperty("reqServiceRate"));
        logInfo += "\nbyteServiceRate: " + getDoubleVal(m_model.getModelInstance().getComponent("server2").getProperty("byteServiceRate"));
        logInfo += "\ncost: " + getDoubleVal(m_model.getModelInstance().getComponent("server2").getProperty("cost"));
        logInfo += "\nServer 3:";
        logInfo += "\ntraffic: " + getDoubleVal(m_model.getModelInstance().getComponent("server3").getProperty("traffic"));
        //logInfo += "\nisArchEnabled: " + getBoolVal(m_model.getModelInstance().getComponent("server3").getProperty("nisArchEnabled"));
        logInfo += "\nmax_arrival_capacity: " + getIntVal(m_model.getModelInstance().getComponent("server3").getProperty("max_arrival_capacity"));
        logInfo += "\nmax_arrival_capacity_low: " + getIntVal(m_model.getModelInstance().getComponent("server3").getProperty("max_arrival_capacity_low"));
        logInfo += "\nindex: " + getIntVal(m_model.getModelInstance().getComponent("server3").getProperty("index"));
        logInfo += "\nisActive: " + getBoolVal(m_model.getModelInstance().getComponent("server3").getProperty("isActive"));
        logInfo += "\nexpectedActivationTime: " + getIntVal(m_model.getModelInstance().getComponent("server3").getProperty("expectedActivationTime"));
        logInfo += "\nreqServiceRate: " + getDoubleVal(m_model.getModelInstance().getComponent("server3").getProperty("reqServiceRate"));
        logInfo += "\nbyteServiceRate: " + getDoubleVal(m_model.getModelInstance().getComponent("server3").getProperty("byteServiceRate"));
        logInfo += "\ncost: " + getDoubleVal(m_model.getModelInstance().getComponent("server3").getProperty("cost"));
        //log(logInfo);
        
        //testing 2
        SwimModelHelper swimModel = new SwimModelHelper(m_model);
        if (!m_isInitialized) {
            initializeAdaptationMgr(swimModel);
        }
        String environmentModel = PMCAdaptationManager.generateEnvironmentDTMC(generateEnvironmentModel());
        //log(get_initial_state_str(swimModel, true));
        //log(environmentModel);
	}

    private void strategyLog (String logMessage) {
        if (m_strategyLog != null) {
            Date d = new Date ();
            String log = MessageFormat.format ("{0,number,#},queuing,{1}\n", d.getTime (),
                                               logMessage);
            try {
                m_strategyLog.write (java.nio.ByteBuffer.wrap (log.getBytes ()));
            } catch (IOException e) {
                reportingPort ().error (getComponentType (), "Failed to write " + log + " to log file");
            }
        }
    }

	@Override
	public RainbowComponentT getComponentType() {
        return RainbowComponentT.ADAPTATION_MANAGER;
	}


    private class AdaptationResultsVisitor extends DefaultAdaptationTreeWalker<SwimExtendedPlan> {

        public AdaptationResultsVisitor (AdaptationTree<SwimExtendedPlan> adt) {
            super (adt);
        }

        boolean m_allOk = true;

        @Override
        protected void evaluate (SwimExtendedPlan adaptation) {
            m_allOk &= adaptation.getOutcome ();
        }

    }


}










