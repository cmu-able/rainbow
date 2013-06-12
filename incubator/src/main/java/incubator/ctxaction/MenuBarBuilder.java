package incubator.ctxaction;

import java.util.Collection;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * <p>
 * Class used to create a menu bar from a set of contextual actions. The
 * builder reads a configuration file and reads the following conventions:
 * </p>
 * 
 * <pre>
 * menu.foo.name: Foo menu
 * menu.foo.icons-on: true
 * menu.foo.1: action id for this menu item
 * menu.foo.2: action id for this menu item
 * menu.foo.3: -
 * menu.foo.4: action if for this menu item
 * menu.bar.name: Bar menu
 * menu.bar.1: action id for this menu item
 * menu.bar.2: (subbar)
 * menu.subbar.name: Subbar
 * menu.subbar.1: action id for this menu item
 * menubar.main.1: foo
 * menubar.main.2: bar
 * </pre>
 * 
 * <p>
 * This class will only structure and configure the actions, it will not
 * create the actions. The available actions are passed as collections. If
 * some action is referred to but not available, a dummy menu item will be
 * created to signal.
 * </p>
 * <p>
 * This class will not detect circular definitions of menus and will hang
 * during initialization.
 * </p>
 */
public class MenuBarBuilder extends ActionOrganizer {

	/**
	 * Creates a new menu bar builder. The bundle where the bar
	 * configuration is read from is based on the same rules as the bundles
	 * for the contextual action.
	 * 
	 * @param clazz the class name used to determine the resource bundle
	 * (not used if <code>bundle</code> is not <code>null</code>
	 * @param bundle an optional bundle name
	 */
	public MenuBarBuilder(Class<?> clazz, String bundle) {
		super(clazz, bundle);
	}

	/**
	 * Creates a menu bar.
	 * 
	 * @param name the menu bar name
	 * @param actions the list of actions that will be used to create the
	 * menu bar
	 * 
	 * @return the menu bar
	 */
	public JMenuBar createMenuBar(String name,
			Collection<ContextualAction> actions) {
		if (actions == null) {
			throw new IllegalArgumentException("actions == null");
		}

		if (name == null) {
			throw new IllegalArgumentException("name == null");
		}

		JMenuBar mbar = new JMenuBar();

		/*
		 * Read the IDs of all menus and add them to the menu bar.
		 */
		List<String> menuIdList = readList("menubar." + name);
		for (String menuId : menuIdList) {
			JMenu menu = readMenu(menuId, actions);
			mbar.add(menu);
		}

		return mbar;
	}

	/**
	 * Reads a menu.
	 * 
	 * @param menuId the ID of the menu
	 * @param actions the actions available
	 * 
	 * @return the menu
	 */
	private JMenu readMenu(String menuId,
			Collection<ContextualAction> actions) {
		String prefix = "menu." + menuId;
		String menuName = getConfig(prefix + ".name", null);
		boolean iconsOn = false;
		String iconsOnOrder = getOptionalConfig(prefix + ".icons-on");
		if ("true".equals(iconsOnOrder)) {
			iconsOn = true;
		}

		JMenu menu = new JMenu(menuName);

		List<String> itemIds = readList(prefix);
		for (String itemId : itemIds) {
			if (itemId.equals("-")) {
				menu.addSeparator();
			} else if (itemId.startsWith("(") && itemId.endsWith(")")) {
				itemId = itemId.substring(1, itemId.length() - 1);
				JMenu subMenu = readMenu(itemId, actions);
				menu.add(subMenu);
			} else {
				JMenuItem item = readMenuItem(itemId, iconsOn, actions);
				menu.add(item);
			}
		}

		return menu;
	}

	/**
	 * Reads a menu item.
	 * 
	 * @param id the menu item id (which is also the action ID)
	 * @param defaultIconOn are icons on by default?
	 * @param actions the collection of known actions
	 * 
	 * @return the menu item
	 */
	private JMenuItem readMenuItem(String id, boolean defaultIconOn,
			Collection<ContextualAction> actions) {
		ContextualAction action = findAction(id, actions);

		if (action == null) {
			JMenuItem mi = new JMenuItem("Missing action '" + id + "'");
			mi.setEnabled(false);
			return mi;
		}

		JMenuItem mItem = action.createJMenuItem(defaultIconOn);
		return mItem;
	}
}
