package org.sa.rainbow.swim.adaptation;

import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.adaptation.IAdaptationExecutor;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.OperationResult;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.Result;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.swim.commands.DivertTrafficCmd;
import org.sa.rainbow.model.acme.swim.commands.SwimCommandFactory;

public class DivertTrafficPlan extends SwimExtendedPlan {

  private String         m_divertCommand;
  private boolean            m_outcome;
  private IModelsManagerPort m_modelsManager;
  
  //private SwimModelUpdateOperatorsImpl         m_model;
  private AcmeModelInstance m_reference;

  public DivertTrafficPlan (AcmeModelInstance m, String divertCommand) {
      //m_instructionGraph = instructionGraph;
      m_reference = m;
      m_divertCommand = divertCommand;
  }

  @Override
  public Object evaluate(Object[] argsIn) {
    IAdaptationExecutor<SwimExtendedPlan> executor = 
        Rainbow.instance().getRainbowMaster().strategyExecutor
        (m_reference.getModelName() + ":" + m_reference.getModelType());
    SwimCommandFactory cf = (SwimCommandFactory) m_reference.getCommandFactory();
    DivertTrafficCmd cmd = cf.divertTrafficCmd(m_reference.getModelInstance().getComponent("LB0"), m_divertCommand);
    System.out.println ("Changing Instructions");
    if (executor == null) {
      System.out.println("executor null"); return false;
    }
    else {
    OperationResult result = executor.getOperationPublishingPort().publishOperation(cmd);
    m_outcome = result.result == Result.SUCCESS;
    System.out.println ("Done: " + m_outcome);
    return m_outcome; }
  }

  @Override
  public boolean getOutcome() {
    // TODO Auto-generated method stub
    return m_outcome;
  }

}
