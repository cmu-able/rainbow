package org.sa.rainbow.brass.model.p2_cp3.acme;

import java.util.LinkedList;
import java.util.List;

import org.acmestudio.acme.core.IAcmeType;
import org.acmestudio.acme.core.type.IAcmeEnumType;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmePropertyCommand;
import org.acmestudio.acme.model.util.core.UMEnumValue;
import org.acmestudio.acme.type.AcmeTypeHelper;
import org.sa.rainbow.brass.model.p2_cp3.acme.TurtlebotModelInstance.ActiveT;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.AcmeModelOperation;

public class SetActiveCmd extends AcmeModelOperation<IAcmeProperty> {

	private ActiveT m_enablement;

	public SetActiveCmd(String commandName, AcmeModelInstance model, String target, String enablement) {
		super(commandName, model, target, enablement);
		m_enablement = TurtlebotModelInstance.ActiveT.valueOf(enablement);
	}

	@Override
	public IAcmeProperty getResult() throws IllegalStateException {
		return ((IAcmePropertyCommand )m_command).getProperty();
	}

	@Override
	protected List<IAcmeCommand<?>> doConstructCommand() throws RainbowModelException {

		IAcmeComponent cr = getModelContext().resolveInModel(getTarget(), IAcmeComponent.class);
		if (cr == null)
			throw new RainbowModelException("Cannot find component " + getTarget() + " in the system");
		IAcmeEnumType et = (IAcmeEnumType) AcmeTypeHelper.extractTypeStructure((IAcmeType )cr.lookupName("ActiveT", true));

		if (!et.getValues().contains(m_enablement.name())) {
			throw new RainbowModelException("Cannot assign value to EnablementT: " + m_enablement.name());
		}

		IAcmeProperty prop = cr.getProperty("enablement");

		List<IAcmeCommand<?>> cmds = new LinkedList<>();

		if (prop != null) {
			m_command = cr.getCommandFactory().propertyValueSetCommand(prop,
					new UMEnumValue(m_enablement.name()));
			cmds.add(m_command);
		} else {
			m_command = cr.getCommandFactory().propertyCreateCommand(cr, "enablement", et, new UMEnumValue(m_enablement.name()));
			cmds.add(m_command);
		}
		return cmds;
	}

	@Override
	protected boolean checkModelValidForCommand(IAcmeSystem model) {
		return true;
	}

}
