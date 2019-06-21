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
package org.sa.rainbow.stitch.adaptation;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.adaptation.AdaptationTree;
import org.sa.rainbow.core.adaptation.IAdaptationExecutor;
import org.sa.rainbow.core.adaptation.IAdaptationManager;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.IRainbowAdaptationDequeuePort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.history.ExecutionHistoryModelInstance;
import org.sa.rainbow.stitch.util.ExecutionHistoryData;
import org.sa.rainbow.stitch.util.ExecutionHistoryData.ExecutionStateT;

/**
 * The Strategy Executor serves the role of maintaining the active thread(s) to carry a strategy/ies. This design
 * allows, in the future, for simultaneous execution of multiple strategies if Rainbow should support that ability.
 * <p>
 * Generic architectural operators: - connect (comp1, comp2) - disconnect (comp1, comp2) - start (comp) - shutdown
 * (comp) - changeState (element, prop, val) - execute (element, args)
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class StitchExecutor extends AbstractRainbowRunnable implements IAdaptationExecutor<Strategy> {

    public static final String                      NAME = "Rainbow Strategy Executor";

    /* A queue of strategies to execute, this could be enriched later with
     * priorities and such... */

    private ModelReference                          m_modelRef;

    private AcmeModelInstance                       m_model;
    private IRainbowAdaptationDequeuePort<Strategy> m_adapationDQPort;
    private IModelUSBusPort                         m_modelUSBusPort;
    private IModelDSBusPublisherPort                m_modelDSPort;
    private ThreadGroup                             m_executionThreadGroup;

    private ExecutionHistoryModelInstance           m_historyModel;

    /**
     */
    public StitchExecutor () {
        super (NAME);

    }

    public ExecutionHistoryModelInstance getExecutionHistoryModel () {
        return m_historyModel;
    }

    @Override
    public void setModelToManage (ModelReference model) {
        m_modelRef = model;
        IModelInstance<IAcmeSystem> mi = Rainbow.instance ().getRainbowMaster ().modelsManager ()
                .getModelInstance (model);
        m_adapationDQPort = RainbowPortFactory.createAdaptationDequeuePort (model);
        m_model = (AcmeModelInstance )mi;
        if (m_model == null) {
            m_reportingPort.error (RainbowComponentT.EXECUTOR, "Referring to unknown model " + model.getModelName ()
            + ":" + model.getModelType ());
        }
        m_executionThreadGroup = new ThreadGroup (m_modelRef.toString () + " ThreadGroup");
        ModelsManager mm = Rainbow.instance ().getRainbowMaster ().modelsManager ();
        try {
            if (!mm.getRegisteredModelTypes ().contains (ExecutionHistoryModelInstance.EXECUTION_HISTORY_TYPE)) {
                mm.registerModelType (ExecutionHistoryModelInstance.EXECUTION_HISTORY_TYPE);
            }
            String historyModelName = "strategy-execution-" + model.toString();
            IModelInstance strategyExecutionHistory = mm.getModelInstance(new ModelReference(historyModelName,ExecutionHistoryModelInstance.EXECUTION_HISTORY_TYPE));
			if (strategyExecutionHistory == null) {
				 m_historyModel = new ExecutionHistoryModelInstance (new HashMap<String, ExecutionHistoryData> (),
		                    historyModelName, "memory");
		            mm.registerModel (new ModelReference (historyModelName,
		                    ExecutionHistoryModelInstance.EXECUTION_HISTORY_TYPE), m_historyModel);
            }
			else {
				m_historyModel = (ExecutionHistoryModelInstance )strategyExecutionHistory;
			}
           
        }
        catch (RainbowModelException e) {
            m_reportingPort.warn (getComponentType (), "Could not create a tactic execution history model", e);
        }
    }
    
    @Override
    public ModelReference getManagedModel() {
    	return m_modelRef;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.core.IDisposable#dispose()
     */
    @Override
    public void dispose () {

        m_modelDSPort.dispose ();
        m_reportingPort.dispose ();
        if (m_adapationDQPort != null) {
            m_adapationDQPort.dispose ();
        }

    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.core.AbstractRainbowRunnable#log(java.lang.String)
     */
    @Override
    public void log (String txt) {
        m_reportingPort.info (RainbowComponentT.EXECUTOR, txt);
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.core.AbstractRainbowRunnable#runAction()
     */
    @Override
    protected void runAction () {
        if (!m_adapationDQPort.isEmpty ()) {
            // retrieve the next adaptation in the queue and execute it
            // Because we are dealing only in stitch in this executor, then
            // use a StitchExecutionVisitor to visit this adaptation
            AdaptationTree<Strategy> at = m_adapationDQPort.dequeue ();
			m_modelUSBusPort
					.updateModel(m_historyModel.getCommandFactory().strategyExecutionStateCommand(getManagedModel(), at.getId(),
							ExecutionHistoryModelInstance.ADAPTATION_TREE, ExecutionStateT.ADAPTATION_EXECUTING, null));
            log ("Dequeued an adaptation");
            log(MessageFormat.format("[[{0}]]: Executing an adaptation", id()));
//            final AtomicInteger numLL = new AtomicInteger (0);
//            // Count the number of parallel threads in the tree so that we can wait until they are finished
//            at.visit (new IAdaptationVisitor<Strategy> () {
//                @Override
//                public boolean visitLeaf (AdaptationTree<Strategy> tree) {
//                    return false;
//                }
//
//                @Override
//                public boolean visitSequence (AdaptationTree<Strategy> tree) {
//                    for (AdaptationTree<Strategy> t :
//                         tree.getSubTrees ()) {
//                        t.visit (this);
//                    }
//                    return true;
//                }
//
//                @Override
//                public boolean visitSequenceStopSuccess (AdaptationTree<Strategy> tree) {
//                    for (AdaptationTree<Strategy> t :
//                            tree.getSubTrees ()) {
//                        t.visit (this);
//                    }
//                    return true;
//                }
//
//                @Override
//                public boolean visitSequenceStopFailure (AdaptationTree<Strategy> tree) {
//                    for (AdaptationTree<Strategy> t :
//                            tree.getSubTrees ()) {
//                        t.visit (this);
//                    }
//                    return true;
//                }
//
//                @Override
//                public boolean visitParallel (AdaptationTree<Strategy> tree) {
//                    numLL.addAndGet (tree.getSubTrees ().size ());
//                    for (AdaptationTree<Strategy> t :
//                            tree.getSubTrees ()) {
//                        t.visit (this);
//                    }
//                    return true;
//                }
//            });
            final CountDownLatch done = new CountDownLatch (1);
            StitchExecutionVisitor stitchVisitor = new StitchExecutionVisitor (this, this.m_modelRef,
                                                                               m_historyModel.getCommandFactory (),
                                                                               at, m_executionThreadGroup,
                                                                               done);
            stitchVisitor.start ();

            try {
                done.await ();
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }

            if (!Rainbow.instance ().shouldTerminate ()) {
				m_modelUSBusPort.updateModel(
						m_historyModel.getCommandFactory().strategyExecutionStateCommand(getManagedModel(), at.getId(),
								ExecutionHistoryModelInstance.ADAPTATION_TREE, ExecutionStateT.ADAPTATION_DONE, null));
                final IAdaptationManager<Strategy> adaptationManager = Rainbow.instance ()
                        .getRainbowMaster ().adaptationManagerForModel (this.m_modelRef.toString ());
                if (adaptationManager != null) {
                    adaptationManager
                            .markStrategyExecuted (at);
                }
            }
//            
//            
//            
//            Strategy strategy = at.getHead ();
//            strategy.setExecutor (this);
//            log("Executing Strategy " + strategy.getName() + "...");
//            Strategy.Outcome o = null;
//            try {
//                // provide var for _dur_
//                Var v = new Var();
//                v.scope = strategy.stitch().scope;
//                v.setType("long");
//                v.name = "_dur_";
////                if (Rainbow.predictionEnabled()) {  // provide future duration
////                    v.setValue(strategy.estimateAvgTimeCost());
////                } else {
//                v.setValue(0L);
////                }
//                strategy.stitch().script.addVar(v.name, v);
//                // execute strategy
//                o = (Strategy.Outcome )strategy.evaluate (null);
//                // cleanup, remove temp var
//                strategy.stitch().script.vars().remove(v.name);
//            } catch (NullPointerException e) {
//                // see if cause of NPE is Rainbow termination
//                if (!Rainbow.shouldTerminate()) {  // nope, terminate and re-throw
//                    terminate();
//                    throw e;
//                }
//            }
//            log(" - Outcome: " + o);
//            if (! Rainbow.shouldTerminate()) {
//                // TODO: Should we check for evaluation outcome?
//                IAdaptationManager<Strategy> adaptationManager = Rainbow.instance ()
//                        .getRainbowMaster ().adaptationManagerForModel (m_modelRef);
//                if (adaptationManager != null) {
//                    adaptationManager
//                    .markStrategyExecuted (strategy);
//                }
//            }
        }
    }

//////////////////////////////////////////
//// ModelOperator method used by Ohana
//////////////////////////////////////////
//
//    /* (non-Javadoc)
//     * @see org.sa.rainbow.stitch.model.ModelOperator#invoke(java.lang.String, java.lang.Object[])
//     */
//    @Override
//    public Object invoke(String name, Object[] args) {
//        Outcome o = Outcome.UNKNOWN;
//        // extract the first argument, args assumed to have at least one entry
//        Object element = args[0];
//        // check effector name against opMap to determine which generic op to invoke
//        String opName = m_model.getGenericOperatorName(name);
//        if (opName == null) {  // no operator name found, just plain invoke
//            String[] strArgs = new String[args.length-1];
//            for (int i=0; i < strArgs.length; ++i) {
//                strArgs[i] = args[i+1].toString();
//            }
//            o = execute(name, element, strArgs);
//        } else if (opName.equals("start")) {
//            String[] strArgs = new String[args.length-1];
//            for (int i=0; i < strArgs.length; ++i) {
//                strArgs[i] = args[i+1].toString();
//            }
//            o = start(name, element, strArgs);
//        } else if (opName.equals("stop")) {
//            String[] strArgs = new String[args.length-1];
//            for (int i=0; i < strArgs.length; ++i) {
//                strArgs[i] = args[i+1].toString();
//            }
//            o = stop(name, element, strArgs);
//        } else if (opName.equals("changeState")) {
//            Map<String,String> statePairs = new HashMap<String,String>();
//            for (int i=1; i < args.length; i+=2) {
//                statePairs.put(args[i].toString(), args[i+1].toString());
//            }
//            o = changeState(name, element, statePairs);
//        } else if (opName.equals("connect")) {
//            Object comp2 = args[1];
//            if (args.length > 2) {
//                String[] strArgs = new String[args.length-2];
//                for (int i=0; i < strArgs.length; ++i) {
//                    strArgs[i] = args[i+2].toString();
//                }
//                o = connect(name, element, comp2, strArgs);
//            }
//        } else if (opName.equals("disconnect")) {
//            int i = 1;
//            Object comp2 = args[i++];
//            Object conn = null;
//            if (args.length > 2) {
//                conn = args[i++];
//            }
//            String[] strArgs = new String[args.length-i];
//            for (int j=0; j < strArgs.length; ++j) {
//                strArgs[j] = args[j+i].toString();
//            }
//            o = disconnect(name, element, comp2, conn, strArgs);
//        }
//        return o.name();
//    }
//
//
//////////////////////////////////////
//// Generic Architectural Operators
//////////////////////////////////////
//
//    /* (non-Javadoc)
//     * @see org.sa.rainbow.adaptation.IGenericArchOperators#start(java.lang.String, java.lang.Object, java.lang.String[])
//     */
//    @Override
//    public Outcome start (String effName, Object component, String[] optArgs) {
//        String target = null;
//        if (component instanceof IAcmeElementInstance<?,?>) {
//            target = m_model.getElementLocation ((IAcmeElementInstance<?, ?> )component);
//        } else {
//            target = component.toString();
//        }
//        // Get a reference to the named effector interface & execute
//        Outcome r = Rainbow.instance ().getRainbowMaster ().effectorManager ()
//                .executeEffector (effName, target, optArgs);
//
//        m_reportingPort.trace (getComponentType (), effName + " ('start' Eff) returned " + r);
//        return r;
//    }
//
//    /* (non-Javadoc)
//     * @see org.sa.rainbow.adaptation.IGenericArchOperators#stop(java.lang.String, java.lang.Object, java.lang.String[])
//     */
//    @Override
//    public Outcome stop (String effName, Object component, String[] optArgs) {
//        String target = null;
//        if (component instanceof IAcmeElementInstance<?,?>) {
//            target = m_model.getElementLocation ((IAcmeElementInstance<?, ?> )component);
//        } else {
//            target = component.toString();
//        }
//        // Get a reference to the named effector interface & execute
//        Outcome r = Rainbow.instance ().getRainbowMaster ().effectorManager ()
//                .executeEffector (effName, target, optArgs);
//
//        m_reportingPort.trace (getComponentType (), effName + " ('stop' Eff) returned " + r);
//        return r;
//    }
//
//    /* (non-Javadoc)
//     * @see org.sa.rainbow.adaptation.IGenericArchOperators#changeState(java.lang.String, java.lang.Object, java.util.Map)
//     */
//    @Override
//    public Outcome changeState (String effName, Object element, Map<String,String> statePairs) {
//        String target = null;
//        if (element instanceof IAcmeElementInstance<?,?>) {
//            target = m_model.getElementLocation ((IAcmeElementInstance<?, ?> )element);
//        } else {
//            target = element.toString();
//        }
//        // form string arguments from key-value pairs of states to change
//        List<String> pairList = new ArrayList<String>();
//        for (Map.Entry<String,String> pair : statePairs.entrySet()) {
//            pairList.add(pair.getKey() + "=" + pair.getValue());
//        }
//        String[] args = pairList.toArray(new String[0]);
//        Outcome r = Rainbow.instance ().getRainbowMaster ().effectorManager ().executeEffector (effName, target, args);
//        // Get a reference to the named effector interface & execute
//        m_reportingPort.trace (getComponentType (), effName + " ('changeState' Eff) returned " + r);
//        return r;
//    }
//
//    /* (non-Javadoc)
//     * @see org.sa.rainbow.adaptation.IGenericArchOperators#connect(java.lang.String, java.lang.Object, java.lang.Object, java.lang.String[])
//     */
//    @Override
//    public Outcome connect (String effName, Object initiatingComp, Object targetComp, String[] optArgs) {
//        String target = null;
//        if (initiatingComp instanceof IAcmeElementInstance<?,?>) {
//            target = m_model.getElementLocation ((IAcmeElementInstance<?, ?> )initiatingComp);
//        } else {
//            target = initiatingComp.toString();
//        }
//        // include target component's location or name as part of arguments
//        List<String> argList = new ArrayList<String>();
//        if (targetComp instanceof IAcmeElementInstance<?,?>) {
//            argList.add (m_model.getElementLocation ((IAcmeElementInstance<?, ?> )targetComp));
//        } else {
//            argList.add(targetComp.toString());
//        }
//        Collections.addAll(argList, optArgs);
//        String[] args = argList.toArray(new String[0]);
//        // Get a reference to the named effector interface & execute
//        Outcome r = Rainbow.instance ().getRainbowMaster ().effectorManager ()
//                .executeEffector (effName, target, optArgs);
//
//        m_reportingPort.trace (getComponentType (), effName + " ('connect' Eff) returned " + r);
//        return r;
//    }
//
//    /* (non-Javadoc)
//     * @see org.sa.rainbow.adaptation.IGenericArchOperators#disconnect(java.lang.String, java.lang.Object, java.lang.Object, java.lang.Object, java.lang.String[])
//     */
//    @Override
//    public Outcome disconnect (String effName, Object comp1, Object comp2, Object conn, String[] optArgs) {
//        String target = null;
//        if (comp1 instanceof IAcmeElementInstance<?,?>) {
//            target = m_model.getElementLocation ((IAcmeElementInstance<?, ?> )comp1);
//        } else {
//            target = comp1.toString();
//        }
//        // include target component's location or name as part of arguments
//        List<String> argList = new ArrayList<String>();
//        if (comp2 instanceof IAcmeElementInstance<?,?>) {
//            argList.add (m_model.getElementLocation ((IAcmeElementInstance<?, ?> )comp2));
//        } else {
//            argList.add(comp2.toString());
//        }
//        // include connector reference if provided
//        if (conn != null) {
//            if (conn instanceof IAcmeElementInstance<?,?>) {
//                argList.add(((IAcmeElementInstance<?,?> )conn).getQualifiedName());
//            } else {
//                argList.add(conn.toString());
//            }
//        }
//        Collections.addAll(argList, optArgs);
//        String[] args = argList.toArray(new String[0]);
//        // Get a reference to the named effector interface & execute
//        Outcome r = Rainbow.instance ().getRainbowMaster ().effectorManager ().executeEffector (effName, target, args);
//
//        m_reportingPort.trace (getComponentType (), effName + " ('disconnect' Eff) returned " + r);
//        return r;
//    }
//
//    /* (non-Javadoc)
//     * @see org.sa.rainbow.adaptation.IGenericArchOperators#execute(java.lang.String, java.lang.Object, java.lang.String[])
//     */
////    @Override
////    public IEffector.Outcome execute (String effName, Object element, String[] args) {
////        String target = null;
////        if (element instanceof IAcmeElementInstance<?,?>) {
////            target = m_model.getElementLocation ((IAcmeElementInstance<?, ?> )element);
////        } else {
////            target = element.toString();
////        }
////        // Get a reference to the named effector interface & execute
////        Outcome r = Rainbow.instance ().getRainbowMaster ().effectorManager ().executeEffector (effName, target, args);
////
////        m_reportingPort.trace (getComponentType (), effName + " returned " + r);
////        return r;
////    }
//
//    @Override
//    public Object lookupOperator(String name) {
//        String opName = m_model.getGenericOperatorName (name);
//        return opName;
//    }

    @Override
    public RainbowComponentT getComponentType () {
        return RainbowComponentT.EXECUTOR;
    }

    @Override
    public IModelDSBusPublisherPort getOperationPublishingPort () {
        return m_modelDSPort;
    }

    public IModelUSBusPort getHistoryModelUSPort () {
        return m_modelUSBusPort;
    }

    @Override
    public void initialize (IRainbowReportingPort port) throws RainbowConnectionException {
        super.initialize (port);
        m_modelDSPort = RainbowPortFactory.createModelDSPublishPort (this);
        m_modelUSBusPort = RainbowPortFactory.createModelsManagerClientUSPort (this);
        // Create a tactics execution model
 
        log ("Strategy executor initialized");
    }

    @Override
    public IRainbowReportingPort getReportingPort () {
        return m_reportingPort;
    }


}
