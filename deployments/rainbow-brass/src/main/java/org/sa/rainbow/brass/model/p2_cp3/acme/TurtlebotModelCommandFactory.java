package org.sa.rainbow.brass.model.p2_cp3.acme;

import java.io.InputStream;

import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.element.IAcmeComponent;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.model.acme.AcmeModelCommandFactory;

public class TurtlebotModelCommandFactory extends AcmeModelCommandFactory {

	public static TurtlebotLoadModelCommand loadCommand(ModelsManager mm, String name, InputStream stream, String source) {
		return new TurtlebotLoadModelCommand (mm, name, stream, source);
	}
	
	
	public TurtlebotModelCommandFactory(TurtlebotModelInstance model) {
		super(model);
	}

	@Override
	protected void fillInCommandMap() {
		super.fillInCommandMap();
		m_commandMap.put("setActive".toLowerCase(), SetActiveCmd.class);
	}
	
	public SetActiveCmd setActive(IAcmeComponent comp, TurtlebotModelInstance.ActiveT enablement) {
		if (ModelHelper.getAcmeSystem(comp) != m_modelInstance.getModelInstance()) {
			throw new IllegalArgumentException("Cannot create a command for a system that is not managed by this command factory");
		}
		return new SetActiveCmd((TurtlebotModelInstance )m_modelInstance, comp.getQualifiedName(), enablement.name());
	}
}
