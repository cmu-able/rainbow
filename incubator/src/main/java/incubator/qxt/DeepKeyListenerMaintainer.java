package incubator.qxt;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

/**
 * Class that adds a key listener to a component and all its subcomponents.
 * It also adds and removes the listener to dynamically added (or removed)
 * subcomponents.
 */
public class DeepKeyListenerMaintainer {
	/**
	 * The root of the hierarchy.
	 */
	private final Component root;

	/**
	 * Who to inform.
	 */
	private final KeyListener listener;

	/**
	 * Hierarchy listener (receives notifications of hierarchy changes).
	 */
	private final HierarchyListener hlistener;

	/**
	 * Components where the listener is registered.
	 */
	private Set<Component> registered;

	/**
	 * Component to add itself to.
	 * 
	 * @param component the component
	 * @param listener listener to add
	 */
	public DeepKeyListenerMaintainer(Component component,
			KeyListener listener) {
		if (component == null) {
			throw new IllegalArgumentException("component == null");
		}

		if (listener == null) {
			throw new IllegalArgumentException("listener == null");
		}

		this.root = component;
		this.listener = listener;
		this.registered = new HashSet<>();
		this.hlistener = new HierarchyListener() {
			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				checkRegistration();
			}
		};

		checkRegistration();
	}

	/**
	 * Checks that registration is correct: ensures that the listener is
	 * registered on all hierarchy components and is not registered in any
	 * other component which is no longer in the hierarchy.
	 */
	private void checkRegistration() {
		Set<Component> hierarchy = computeHierarchy();

		/*
		 * See which are the new and removed components.
		 */
		Set<Component> newComponents = new HashSet<>(hierarchy);
		newComponents.removeAll(registered);
		Set<Component> removedComponents = new HashSet<>(registered);
		removedComponents.removeAll(hierarchy);

		/*
		 * Update the registration.
		 */
		for (Component c : newComponents) {
			c.addKeyListener(listener);
			if (c instanceof Container) {
				((Container) c).addHierarchyListener(hlistener);
			}
		}

		for (Component c : removedComponents) {
			c.removeKeyListener(listener);
			if (c instanceof Container) {
				((Container) c).removeHierarchyListener(hlistener);
			}
		}

		registered = hierarchy;
	}

	/**
	 * Computes the set of components in the hierarchy starting from the
	 * root component.
	 * 
	 * @return a set with all components
	 */
	private Set<Component> computeHierarchy() {
		Set<Component> hierarchy = new HashSet<>();
		addToHierarchy(hierarchy, root);
		return hierarchy;
	}

	/**
	 * Adds a component and all its subcomponents to the set of all
	 * components.
	 * 
	 * @param hierarchy the set
	 * @param c the component to add to the set (plus all its subcomponents)
	 */
	private void addToHierarchy(Set<Component> hierarchy, Component c) {
		assert hierarchy != null;
		assert c != null;

		hierarchy.add(c);

		if (c instanceof Container) {
			for (Component sc : ((Container) c).getComponents()) {
				addToHierarchy(hierarchy, sc);
			}
		}
	}

	/**
	 * Useless method to ensure checkstyle doesn't complain.
	 */
	protected void dummy() {
		/*
		 * No code here. Dummy method.
		 */
	}
}
