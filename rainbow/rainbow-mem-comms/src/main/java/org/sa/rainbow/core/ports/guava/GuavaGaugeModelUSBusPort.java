package org.sa.rainbow.core.ports.guava;

import java.util.List;

import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.ChannelT;

public class GuavaGaugeModelUSBusPort implements IModelUSBusPort, ESEBConstants {

	private GuavaEventConnector m_eventBus;
	private Identifiable m_client;

	public GuavaGaugeModelUSBusPort(Identifiable client) {
		m_client = client;
		m_eventBus = new GuavaEventConnector(ChannelT.MODEL_US);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateModel(IRainbowOperation command) {
		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		msg.setProperty(ESEBConstants.MSG_DELEGATE_ID_KEY, m_client.id());
		msg.setProperty(ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_UPDATE_MODEL);
		GuavaCommandHelper.command2Message(command, msg);
		m_eventBus.publish(msg);

	}

	@Override
	public void updateModel(List<IRainbowOperation> commands, boolean transaction) {
		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		msg.setProperty(ESEBConstants.MSG_DELEGATE_ID_KEY, m_client.id());
		msg.setProperty(ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_UPDATE_MODEL + "_multi");
		for (int i = 0; i < commands.size(); i++) {
			GuavaCommandHelper.command2Message(commands.get(i), msg, "_" + i + "_");
		}
		msg.setProperty(GuavaCommandHelper.MSG_TRANSACTION, Boolean.valueOf(transaction));
		m_eventBus.publish(msg);

	}

	@Override
	public <T> IModelInstance<T> getModelInstance(ModelReference modelRef) {
		if (Rainbow.instance().isMaster()) {
			RainbowMaster master = Rainbow.instance().getRainbowMaster();
			return master.modelsManager().getModelInstance(modelRef);
		}
		throw new UnsupportedOperationException(
				"A model instance cannot be retrieved currently if not running in the RainbowMaster.");
	}

}
