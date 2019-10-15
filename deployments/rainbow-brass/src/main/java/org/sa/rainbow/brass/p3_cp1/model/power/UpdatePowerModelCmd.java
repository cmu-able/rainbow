package org.sa.rainbow.brass.p3_cp1.model.power;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.json.simple.parser.ParseException;
import org.sa.rainbow.brass.confsynthesis.SimpleConfiguration;
import org.sa.rainbow.brass.confsynthesis.SimpleConfigurationStore;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public class UpdatePowerModelCmd extends AbstractRainbowModelOperation<Boolean, SimpleConfigurationStore> {

	public static final String NAME = "updatePowerModel";
	private String m_newFileName;
	private Boolean m_successful;
	private HashMap<String, SimpleConfiguration> m_oldModel;

	public UpdatePowerModelCmd(CP1PowerModelInstance modelInstance, String target, String newFileName) {
		super(NAME, modelInstance, target, newFileName);
		m_newFileName = newFileName;
	
	}

	@Override
	public Boolean getResult() throws IllegalStateException {
		return m_successful;
	}

	@Override
	protected List<? extends IRainbowMessage> getGeneratedEvents(IRainbowMessageFactory messageFactory) {
		return generateEvents(messageFactory, NAME);
	}

	@Override
	protected void subExecute() throws RainbowException {
		try {
			m_oldModel = new HashMap<String,SimpleConfiguration> (getModelContext().getModelInstance().m_configuration_objects);
			getModelContext().getModelInstance().reloadFromFile(m_newFileName);
		} catch (IOException | ParseException e) {
			m_successful = false;
		}
	}

	@Override
	protected void subRedo() throws RainbowException {
		try {
			getModelContext().getModelInstance().reloadFromFile(m_newFileName);
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void subUndo() throws RainbowException {
		getModelContext().getModelInstance().m_configuration_objects = m_oldModel;
	}

	@Override
	protected boolean checkModelValidForCommand(SimpleConfigurationStore model) {
		return model == getModelContext().getModelInstance();
	}

}
