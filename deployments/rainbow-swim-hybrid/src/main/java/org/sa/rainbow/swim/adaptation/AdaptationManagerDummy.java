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

import org.acmestudio.acme.element.IAcmeSystem;
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
import org.sa.rainbow.stitch.Ohana;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.core.Tactic;
import org.sa.rainbow.stitch.error.DummyStitchProblemHandler;
import org.sa.rainbow.stitch.error.IStitchProblem;
import org.sa.rainbow.stitch.error.StitchProblem;
import org.sa.rainbow.stitch.visitor.Stitch;
import org.sa.rainbow.util.Beacon;
import org.sa.rainbow.util.Util;


public final class AdaptationManagerDummy extends AbstractRainbowRunnable
        implements IAdaptationManager<SwimExtendedPlan>, IRainbowModelChangeCallback {


    public static final String NAME = "Swim Extended Adaptation Manager Dummy";
    // The thread "sleep" time. runAction will be called every 10 seconds in this case
    public static final int SLEEP_TIME = 60000 /*ms*/;

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

    public AdaptationManagerDummy () {
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

    protected void initializeAdaptationMgr(SwimModelHelper swimModel) {
    	m_isInitialized = true;
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


    private int adaptTestCt = 0;
	@Override
	protected void runAction() {
        SwimExtendedPlan incDim = new IncDimmerPlan(m_model);
        SwimExtendedPlan decDim = new DecDimmerPlan(m_model);
        SwimExtendedPlan divTrf = new DivertTrafficPlan(m_model, "divert_100_0_0");
        SwimExtendedPlan addSev = new AddServerPlan(m_model, "2");
        SwimExtendedPlan remSev = new RemoveServerPlan(m_model, "2");
        AdaptationTree<SwimExtendedPlan> at = new AdaptationTree<>(AdaptationExecutionOperatorT.PARALLEL);
        //at = at.addLeaf(incDim); at = at.getParent();
        //at = at.addLeaf(decDim); at = at.getParent();
        //at = at.addLeaf(divTrf); at = at.getParent();
        switch (adaptTestCt++){
        // case 0:
        //     at = at.addLeaf(addSev); at = at.getParent();
        //     break;
        // case 1:
        //     at = at.addLeaf(incDim); at = at.getParent();
        //     break;
        // case 2:
        //     at = at.addLeaf(incDim); at = at.getParent();
        //     break;
        // case 3:
        //     at = at.addLeaf(decDim); at = at.getParent();
        //     break;
        // case 4:
        //     at = at.addLeaf(remSev); at = at.getParent();
        //     break;
        // case 5:
        //     at = at.addLeaf(decDim); at = at.getParent();
        //     break;
        default:
        	at = at.addLeaf(decDim); at = at.getParent();
            at = at.addLeaf(incDim); at = at.getParent();
        	break;
        }
        log(at.toString());
        log("New plan generated. Enqueueing...");
        m_adaptationEnqueuePort.offerAdaptation(at, new Object[0]);
        m_executingPlan = true;
        String logInfo = "";
        logInfo += "Load Balancer:\n";
        logInfo += "deploymentLocation: " + m_model.getModelInstance().getComponent("LB0").getProperty("deploymentLocation").getValue().toString();
        logInfo += "\ndimmer: " + m_model.getModelInstance().getComponent("LB0").getProperty("dimmer").getValue().toString();
        logInfo += "\narrivalRate: " + m_model.getModelInstance().getComponent("LB0").getProperty("arrivalRate").getValue().toString();
        logInfo += "\naverageResponseTime: " + m_model.getModelInstance().getComponent("LB0").getProperty("averageResponseTime").getValue().toString();
        logInfo += "\nServer 1:\n";
        logInfo += "traffic: " + m_model.getModelInstance().getComponent("server1").getProperty("traffic").getValue().toString();
        logInfo += "\nmax_arrival_capacity: " + m_model.getModelInstance().getComponent("server1").getProperty("max_arrival_capacity").getValue().toString();
        logInfo += "\nmax_arrival_capacity_low: " + m_model.getModelInstance().getComponent("server1").getProperty("max_arrival_capacity_low").getValue().toString();
        logInfo += "\nindex: " + m_model.getModelInstance().getComponent("server1").getProperty("index").getValue().toString();
        logInfo += "\nisActive: " + m_model.getModelInstance().getComponent("server1").getProperty("isActive").getValue().toString();
        logInfo += "\nexpectedActivationTime: " + m_model.getModelInstance().getComponent("server1").getProperty("expectedActivationTime").getValue().toString();
        logInfo += "\nreqServiceRate: " + m_model.getModelInstance().getComponent("server1").getProperty("reqServiceRate").getValue().toString();
        logInfo += "\nbyteServiceRate: " + m_model.getModelInstance().getComponent("server1").getProperty("byteServiceRate").getValue().toString();
        logInfo += "\ncost: " + m_model.getModelInstance().getComponent("server1").getProperty("cost").getValue().toString();
        logInfo += "\nServer 2:\n";
        logInfo += "traffic: " + m_model.getModelInstance().getComponent("server2").getProperty("traffic").getValue().toString();
        logInfo += "\nmax_arrival_capacity: " + m_model.getModelInstance().getComponent("server2").getProperty("max_arrival_capacity").getValue().toString();
        logInfo += "\nmax_arrival_capacity_low: " + m_model.getModelInstance().getComponent("server2").getProperty("max_arrival_capacity_low").getValue().toString();
        logInfo += "\nindex: " + m_model.getModelInstance().getComponent("server2").getProperty("index").getValue().toString();
        logInfo += "\nisActive: " + m_model.getModelInstance().getComponent("server2").getProperty("isActive").getValue().toString();
        logInfo += "\nexpectedActivationTime: " + m_model.getModelInstance().getComponent("server2").getProperty("expectedActivationTime").getValue().toString();
        logInfo += "\nreqServiceRate: " + m_model.getModelInstance().getComponent("server2").getProperty("reqServiceRate").getValue().toString();
        logInfo += "\nbyteServiceRate: " + m_model.getModelInstance().getComponent("server2").getProperty("byteServiceRate").getValue().toString();
        logInfo += "\ncost: " + m_model.getModelInstance().getComponent("server2").getProperty("cost").getValue().toString();
        logInfo += "\nServer 3:\n";
        logInfo += "traffic: " + m_model.getModelInstance().getComponent("server3").getProperty("traffic").getValue().toString();
        logInfo += "\nmax_arrival_capacity: " + m_model.getModelInstance().getComponent("server3").getProperty("max_arrival_capacity").getValue().toString();
        logInfo += "\nmax_arrival_capacity_low: " + m_model.getModelInstance().getComponent("server3").getProperty("max_arrival_capacity_low").getValue().toString();
        logInfo += "\nindex: " + m_model.getModelInstance().getComponent("server3").getProperty("index").getValue().toString();
        logInfo += "\nisActive: " + m_model.getModelInstance().getComponent("server3").getProperty("isActive").getValue().toString();
        logInfo += "\nexpectedActivationTime: " + m_model.getModelInstance().getComponent("server3").getProperty("expectedActivationTime").getValue().toString();
        logInfo += "\nreqServiceRate: " + m_model.getModelInstance().getComponent("server3").getProperty("reqServiceRate").getValue().toString();
        logInfo += "\nbyteServiceRate: " + m_model.getModelInstance().getComponent("server3").getProperty("byteServiceRate").getValue().toString();
        logInfo += "\ncost: " + m_model.getModelInstance().getComponent("server3").getProperty("cost").getValue().toString();
        String logInfoNew = "";
        logInfoNew += "Load Balancer:";
        logInfoNew += "\ndimmer: ";
        
        log(logInfo);
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










