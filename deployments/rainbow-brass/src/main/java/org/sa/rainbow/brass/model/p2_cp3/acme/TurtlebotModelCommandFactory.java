package org.sa.rainbow.brass.model.p2_cp3.acme;

import java.io.InputStream;

import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.element.IAcmeComponent;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.model.acme.AcmeModelCommandFactory;

public class TurtlebotModelCommandFactory extends AcmeModelCommandFactory {

	private static final String SET_ACTIVE_CMD = "setActive";

	@LoadOperation
	public static TurtlebotLoadModelCommand loadCommand(ModelsManager mm, String name, InputStream stream, String source) {
		return new TurtlebotLoadModelCommand (mm, name, stream, source);
	}
	
	
	public TurtlebotModelCommandFactory(TurtlebotModelInstance model) throws RainbowException {
		super(model);
	}

	@Operation(name=SET_ACTIVE_CMD)
	public SetActiveCmd setActive(IAcmeComponent comp, TurtlebotModelInstance.ActiveT enablement) {
		if (ModelHelper.getAcmeSystem(comp) != m_modelInstance.getModelInstance()) {
			throw new IllegalArgumentException("Cannot create a command for a system that is not managed by this command factory");
		}
		return new SetActiveCmd(SET_ACTIVE_CMD, (TurtlebotModelInstance )m_modelInstance, comp.getQualifiedName(), enablement.name());
	}
}
