package incubator.modulebar;

import incubator.ctxaction.ContextualAction;
import incubator.ctxaction.ContextualActionListener;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

/**
 * Component that contains the modules and handles their activation and
 * deactivation.
 */
public class ModuleManager extends JPanel {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1;
	
	/**
	 * ID of the next module that will be added.
	 */
	private int nextId;
	
	/**
	 * Panel that will contain the current module's panel.
	 */
	private JPanel modulePanel;
	
	/**
	 * Layout used in the {@link #modulePanel} panel.
	 */
	private CardLayout layout;
	
	/**
	 * List of all modules added.
	 */
	private List<ModuleInfo> modules;
	
	/**
	 * Maps each group name to the structure with the group information.
	 */
	private Map<String, GroupInfo> taskGroups;
	
	/**
	 * Task container component.
	 */
	private JXTaskPaneContainer taskContainer;
	
	/**
	 * Currently active module (<code>null</code> if none) 
	 */
	private ModuleInfo active;
	
	/**
	 * Creates a new module manager.
	 */
	public ModuleManager() {
		setLayout(new BorderLayout());
		
		nextId = 1;
		active = null;
		layout = new CardLayout();
		modulePanel = new JPanel(layout);
		add(modulePanel, BorderLayout.CENTER);
		
		taskContainer = new JXTaskPaneContainer();
		add(taskContainer, BorderLayout.WEST);
		
		modules = new ArrayList<>();
		taskGroups = new HashMap<>();
	}
	
	/**
	 * Adds a module to the module manager. The module is added in a group. If
	 * no group exists with the given name, a new group is created.
	 * 
	 * @param module the module to add
	 * @param group the name of the group to add the module to
	 */
	public void addModule(final ApplicationModule module, String group) {
		if (module == null) {
			throw new IllegalArgumentException("module == null");
		}
		
		if (group == null) {
			throw new IllegalArgumentException("group == null");
		}
		
		for (ModuleInfo mi : modules) {
			if (mi.module == module) {
				throw new IllegalStateException("Module already added.");
			}
		}
		
		GroupInfo gi = taskGroups.get(group);
		if (gi == null) {
			gi = new GroupInfo();
			gi.pane = new JXTaskPane();
			gi.name = group;
			gi.taskCount = 0;
			gi.pane.setTitle(group);
			taskGroups.put(group, gi);
			taskContainer.add(gi.pane);
		}
		
		ModuleInfo info = new ModuleInfo();
		info.module = module;
		info.group = gi;
		info.nameInlayout = "" + nextId;
		nextId++;
		
		info.action = module.getActivationAction();
		if (info.action == null) {
			throw new IllegalArgumentException("module returned null action");
		}
		
		info.moduleInterfacePanel = module.getModulePanel();
		if (info.moduleInterfacePanel == null) {
			throw new IllegalArgumentException("module returned null panel");
		}
		
		info.cal = new ContextualActionListener() {
			@Override
			public void actionPerformed() {
				activateModule(module);
			}
		};
		
		modules.add(info);
		
		info.action.addContextualActionListener(info.cal);
		
		info.activationComponent = gi.pane.add(info.action);
		gi.taskCount++;
		modulePanel.add(info.moduleInterfacePanel, info.nameInlayout);
		
		if (modules.size() == 1) {
			activateModule(module);
		}
	}
	
	/**
	 * Removes a module from the manager.
	 * 
	 * @param module the module to remove
	 */
	public void removeModule(ApplicationModule module) {
		if (module == null) {
			throw new IllegalArgumentException("module == null");
		}
		
		ModuleInfo info = findModule(module);
		
		modules.remove(info);
		
		if (info == active) {
			module.deactivated();
			active = null;
		}
		
		modulePanel.remove(info.moduleInterfacePanel);
		info.action.removeContextualActionListener(info.cal);
		info.group.pane.remove(info.activationComponent);
		info.group.taskCount--;
		if (info.group.taskCount == 0) {
			taskContainer.remove(info.group.pane);
			taskGroups.remove(info.group.name);
		}
		
		if (active == null && modules.size() > 0) {
			activateModule(modules.get(0).module);
		}
	}
	
	/**
	 * Activates a module. Does nothing if the module is already active.
	 * 
	 * @param module the module to activate
	 */
	public void activateModule(ApplicationModule module) {
		if (module == null) {
			throw new IllegalArgumentException("module == null");
		}
		
		ModuleInfo info = findModule(module);
		
		if (info == active) {
			return;
		}
		
		/*
		 * Can be null if previously active module has been removed or when the
		 * first module is added (in those cases, activateModule is invoked
		 * at the end).
		 */
		if (active != null) {
			active.module.deactivated();
		}
		
		layout.show(modulePanel, info.nameInlayout);
		active = info;
		active.module.activated();
	}
	
	/**
	 * Obtains a module's data given the module.
	 * 
	 * @param module the module
	 * 
	 * @return the module's data
	 */
	private ModuleInfo findModule(ApplicationModule module) {
		ModuleInfo info = null;
		for (ModuleInfo mi : modules) {
			if (mi.module == module) {
				info = mi;
			}
		}
		
		if (info == null) {
			throw new IllegalStateException("module not added");
		}
		
		return info;
	}
	
	/**
	 * Structure that encapsulates information on a module group.
	 */
	private class GroupInfo {
		/**
		 * Group name.
		 */
		private String name;
		
		/**
		 * Task pane where module activation actions are.
		 */
		private JXTaskPane pane;
		
		/**
		 * Number of modules in the group.
		 */
		private int taskCount;
	}
	
	/**
	 * Structure that encapsulates information on a module.
	 */
	private class ModuleInfo {
		/**
		 * The module.
		 */
		private ApplicationModule module;
		
		/**
		 * The group the module belongs to.
		 */
		private GroupInfo group;
		
		/**
		 * Module activation action.
		 */
		private ContextualAction action;
		
		/**
		 * Listener registered in the module activation action
		 */
		private ContextualActionListener cal;
		
		/**
		 * Component generated when the action was added to the task pane.
		 */
		private Component activationComponent;
		
		/**
		 * Panel with the module's user interface.
		 */
		private JPanel moduleInterfacePanel;
		
		/**
		 * Name of the module in the card layout.
		 */
		private String nameInlayout;
	}
}
