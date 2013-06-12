package incubator.ctxaction;

import java.util.Collection;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JToolBar;

import org.jdesktop.swingx.JXButton;

/**
 * Class capable of creating toolbars. The toolbar configuration is read
 * from the configuration file. The configuration format is:
 * 
 * <pre>
 * toolbar.foo.1: action id for this menu item
 * toolbar.foo.2: action id for this menu item
 * </pre>
 */
public class ToolbarBuilder extends ActionOrganizer {
	/**
	 * Creates a toolbar builder. The bundle where the bar configuration is
	 * read from is based on the same rules as the bundles for the
	 * contextual action.
	 * 
	 * @param clazz the class name used to determine the resource bundle
	 * (not used if <code>bundle</code> is not <code>null</code>
	 * @param bundle an optional bundle name
	 */
	public ToolbarBuilder(Class<?> clazz, String bundle) {
		super(clazz, bundle);
	}

	/**
	 * Creates a toolbar.
	 * 
	 * @param name the toolbar name
	 * @param actions the collections of actions available
	 * 
	 * @return the toolbar
	 */
	public JToolBar createToolbar(String name,
			Collection<ContextualAction> actions) {
		if (name == null) {
			throw new IllegalArgumentException("name == null");
		}

		if (actions == null) {
			throw new IllegalArgumentException("actions == null");
		}

		JToolBar tb = new JToolBar();

		List<String> items = readList("toolbar." + name);
		for (String i : items) {
			if (i.equals("-")) {
				tb.addSeparator();
				continue;
			}

			ContextualAction ca = findAction(i, actions);
			if (ca == null) {
				tb.add(new JLabel("Missing action '" + i + "'"));
			} else {
				JXButton button = new JXButton(ca);
				tb.add(button);
			}
		}

		return tb;
	}
}
