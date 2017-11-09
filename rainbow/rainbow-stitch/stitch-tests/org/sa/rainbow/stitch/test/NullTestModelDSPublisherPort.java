package org.sa.rainbow.stitch.test;

import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.OperationResult;

public class NullTestModelDSPublisherPort implements IModelDSBusPublisherPort {
	@Override
	public IRainbowMessage createMessage() {
		return null;
	}

	@Override
	public void dispose() {
		
	}

	@Override
	public OperationResult publishOperation(IRainbowOperation cmd) {
		return null;
	}
}