package org.sa.rainbow.stitch.adaptation;

import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.adaptation.IAdaptationExecutor;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.history.ExecutionHistoryModelInstance;

public interface IStitchExecutor extends IAdaptationExecutor<Strategy> {

	IModelUSBusPort getHistoryModelUSPort();

	ExecutionHistoryModelInstance getExecutionHistoryModel();

	RainbowComponentT getComponentType();

}
