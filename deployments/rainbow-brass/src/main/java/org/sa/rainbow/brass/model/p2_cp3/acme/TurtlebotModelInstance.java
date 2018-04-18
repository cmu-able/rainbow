package org.sa.rainbow.brass.model.p2_cp3.acme;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.acmestudio.acme.core.type.IAcmeEnumValue;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.element.property.IAcmePropertyValue;
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
	
	public Collection<String> getActiveComponents() {
		return getComponentEnablement("ACTIVE");
	}

	private Collection<String> getComponentEnablement(String compare) {
		Set<? extends IAcmeComponent> components = getModelInstance().getComponents();
		Set<String> active = new HashSet<String> ();
		for (IAcmeComponent c : components) {
			IAcmeProperty property = c.getProperty("enablement");
			if (property != null) {
				IAcmePropertyValue value = property.getValue();
				if (value instanceof IAcmeEnumValue) {
					IAcmeEnumValue en = (IAcmeEnumValue) value;
					if (en.getValue().equals(compare)) {
						active.add(c.getName());
					}
					
				}
			}
		}
		return active;
	}
	
	public Collection<String> getFailedComponents() {
		return getComponentEnablement("FAILED");
	}

	public Collection<String> getInactiveComponents() {
		return getComponentEnablement("INACTIVE");
	}

}
