package org.sa.rainbow.model.acme.swim.commands;

import java.util.List;

import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmePropertyCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

public class RemoveServerWithTypeCmd extends SwimAcmeModelCommand<IAcmeProperty> {

  private String m_server;

	public RemoveServerWithTypeCmd(AcmeModelInstance model, String target, String server) {
		super("removeServer", model, target, server);
		m_server = server;
	}

	@Override
	public IAcmeProperty getResult() throws IllegalStateException {
		// TODO Auto-generated method stub
    return ((IAcmePropertyCommand )m_command).getProperty ();	
  }

  // This command cannot be executed on acme system, return null
  // Only serves as place holder on bus to signal effector
	@Override
	protected List<IAcmeCommand<?>> doConstructCommand() throws RainbowModelException {
		// TODO Auto-generated method stub
		return null;
	}

}