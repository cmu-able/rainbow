package incubator.ctxaction;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import incubator.ctxaction.ContextualAction;
import incubator.ctxaction.MenuBarBuilder;

/**
 * Robustness tests for the menu bar builder.
 */
public class MenuBarBuilderRbTest extends Assert {
	/**
	 * Creates a menu bar builder with no class given in the constructor.
	 */
	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void creationWithNoClass() {
		new MenuBarBuilder(null, null);
	}

	/**
	 * Creates a menu bar without giving the menu bar name.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void createMenuBarWithNoName() {
		MenuBarBuilder mbb = new MenuBarBuilder(MenuBarBuilderRbTest.class,
				null);
		mbb.createMenuBar(null, new ArrayList<ContextualAction>());
	}

	/**
	 * Creates a menu bar without giving the action collection.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void createMenuBarWithNoActions() {
		MenuBarBuilder mbb = new MenuBarBuilder(MenuBarBuilderRbTest.class,
				null);
		mbb.createMenuBar("foo", null);
	}
}
