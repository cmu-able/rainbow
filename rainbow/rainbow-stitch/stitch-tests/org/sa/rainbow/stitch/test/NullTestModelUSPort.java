package org.sa.rainbow.stitch.test;

import java.util.List;

import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelUSBusPort;

public class NullTestModelUSPort implements IModelUSBusPort {
	@Override
	public void dispose() {
	}

	@Override
	public void updateModel(List<IRainbowOperation> commands, boolean transaction) {
	}

	@Override
	public void updateModel(IRainbowOperation command) {
	}

	@Override
	public <T> IModelInstance<T> getModelInstance(ModelReference modelRef) {
		return null;
	}
}