package org.sa.rainbow.core.ports.guava;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.IModelUpdater;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.eseb.ESEBMasterReportingPort;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.ChannelT;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.IGuavaMessageListener;

public class GuavaModelManagerModelUpdatePort 
		implements IModelUSBusPort, ESEBConstants {

	private final IRainbowReportingPort LOGGER = new GuavaMasterReportingPort();

	private IModelUpdater m_mm;
	private GuavaEventConnector m_eventBus;

	public GuavaModelManagerModelUpdatePort(IModelUpdater m) {
		m_eventBus = new GuavaEventConnector(ChannelT.MODEL_US);
		m_mm = m;
		m_eventBus.addListener(new IGuavaMessageListener() {
			
			@Override
			public void receive(GuavaRainbowMessage msg) {
				String msgType = (String) msg.getProperty(ESEBConstants.MSG_TYPE_KEY);
				if (ESEBConstants.MSG_TYPE_UPDATE_MODEL.equals(msgType)) {
					String modelType = (String) msg.getProperty(MODEL_TYPE_KEY);
					String modelName = (String) msg.getProperty(MODEL_NAME_KEY);

					List<String> params = new LinkedList<>();
					params.add((String) msg.getProperty(COMMAND_TARGET_KEY));
					int i = 0;
					String p;
					while ((p = (String) msg.getProperty(COMMAND_PARAMETER_KEY + i++)) != null) {
						params.add(p);
					}

					String commandName = (String) msg.getProperty(COMMAND_NAME_KEY);
					ModelReference mref = new ModelReference(modelName, modelType);

					try {
						IModelInstance model = getModelInstance(mref);
						if (model != null) {
							IRainbowOperation command = model.getCommandFactory().generateCommand(commandName,
									params.toArray(new String[params.size()]));
							if (msg.hasProperty(COMMAND_ORIGIN)) {
								command.setOrigin((String) msg.getProperty(COMMAND_ORIGIN));
							}
							updateModel(command);
						} else {
							params.remove(0);
							IRainbowOperation command = new OperationRepresentation(commandName, mref,
									(String) msg.getProperty(COMMAND_TARGET_KEY),
									params.toArray(new String[params.size()]));
							if (msg.hasProperty(COMMAND_ORIGIN)) {
								command.setOrigin((String) msg.getProperty(COMMAND_ORIGIN));
							}
							updateModel(command);
						}

					} catch (Throwable e) {
						params.remove(0);
						IRainbowOperation command = new OperationRepresentation(commandName, mref,
								(String) msg.getProperty(COMMAND_TARGET_KEY),
								params.toArray(new String[params.size()]));
						if (msg.hasProperty(COMMAND_ORIGIN)) {
							command.setOrigin((String) msg.getProperty(COMMAND_ORIGIN));
						}
						LOGGER.error(RainbowComponentT.MODEL, MessageFormat
								.format("Could not form the command ''{0}'' from the ESEB message = {1}", commandName, command.toString()), e);
					}
				} else if ((ESEBConstants.MSG_TYPE_UPDATE_MODEL + "_multi").equals(msgType)) {
					int i = 0;
					IRainbowOperation cmd;
					List<IRainbowOperation> ops = new LinkedList<>();
					do {
						cmd = GuavaCommandHelper.msgToCommand(msg, "_" + i + "_");
						if (cmd != null) {
							try {
								IModelInstance model = getModelInstance(cmd.getModelReference());
								if (model != null) {
									String[] params = new String[cmd.getParameters().length + 1];
									params[0] = cmd.getTarget();
									for (int j = 0; j < cmd.getParameters().length; j++) {
										params[j + 1] = cmd.getParameters()[j];
									}
									IRainbowOperation command = model.getCommandFactory().generateCommand(cmd.getName(),
											params);
									if (msg.hasProperty(COMMAND_ORIGIN)) {
										command.setOrigin((String) msg.getProperty(COMMAND_ORIGIN));
									}
									ops.add(command);
									
									
								} else {
									IRainbowOperation command = new OperationRepresentation(cmd);
									if (msg.hasProperty(COMMAND_ORIGIN)) {
										command.setOrigin((String) msg.getProperty(COMMAND_ORIGIN));
									}
									ops.add(command);
								}
							} catch (Throwable e) {
								LOGGER.error(RainbowComponentT.MODEL, MessageFormat.format(
										"Could not form the command ''{0}'' from the ESEB message= {1}", cmd.getName(), cmd.toString()), e);
								
							}
						}
						i++;
					} while (cmd != null);
					boolean b = (Boolean) msg.getProperty(ESEBConstants.MSG_TRANSACTION);
					updateModel(ops, b);

				}
			}
			
		});
		
	}

	@Override
	public void updateModel(IRainbowOperation command) {
		try {
			m_mm.requestModelUpdate(command);
		} catch (IllegalStateException | RainbowException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void updateModel(List<IRainbowOperation> commands, boolean transaction) {
		try {
			m_mm.requestModelUpdate(commands, transaction);
		} catch (IllegalStateException | RainbowException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public <T> IModelInstance<T> getModelInstance(ModelReference modelRef) {
		return m_mm.getModelInstance(modelRef);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}


}
