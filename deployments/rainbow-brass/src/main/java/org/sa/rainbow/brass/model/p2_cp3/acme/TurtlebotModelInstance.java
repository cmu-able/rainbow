package org.sa.rainbow.brass.model.p2_cp3.acme;

import org.acmestudio.acme.core.type.IAcmeEnumType;
import org.acmestudio.acme.element.IAcmeElementInstance;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.command.IAcmePropertyCommand;
import org.acmestudio.acme.model.util.core.UMEnumValue;
import org.acmestudio.basicmodel.core.AcmeEnumType;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

public class TurtlebotModelInstance extends AcmeModelInstance {

	public static enum ActiveT {ACTIVE, INACTIVE, FAILED};
	
	
	private TurtlebotModelCommandFactory m_commandFactory;

	public TurtlebotModelInstance(IAcmeSystem system, String source) {
		super(system, source);
	}

	@Override
	protected AcmeModelInstance generateInstance(IAcmeSystem sys) {
		return new TurtlebotModelInstance(sys, getOriginalSource());
	}

	@Override
	public TurtlebotModelCommandFactory getCommandFactory() {
		if (m_commandFactory == null) 
			m_commandFactory = new TurtlebotModelCommandFactory(this);
		return m_commandFactory;
	}
	
	

}
