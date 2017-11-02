package incubator.ctxaction;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import incubator.ctxaction.ActionContext;
import incubator.ctxaction.ContextualAction;
import incubator.ctxaction.DynamicContextualAction;
import incubator.ctxaction.MenuBarBuilder;

/**
 * Equivalence class tests for the menu bar builder.
 */
public class MenuBarBuilderEqTest extends Assert {
	/**
	 * Menu bar builder created from the default action-config.properties.
	 */
	private MenuBarBuilder mbb;

	/**
	 * Default set of actions.
	 */
	private List<ContextualAction> actions;

	/**
	 * Disables the AWT thread check. Creates the default menu bar builder
	 * and the default set of actions.
	 */
	@Before
	public void setUp() {
		ActionContext.disableAwtThreadCheck();
		String bundle = MenuBarBuilderEqTest.class.getName() + "_config";
		ActionContext ctx = new ActionContext();

		mbb = new MenuBarBuilder(MenuBarBuilderEqTest.class, bundle);

		actions = new ArrayList<>();
		actions.add(new DynamicContextualAction(bundle, "a1"));
		actions.add(new DynamicContextualAction(bundle, "a2"));
		actions.add(new DynamicContextualAction(bundle, "a3"));
		actions.add(new DynamicContextualAction(bundle, "a4"));
		for (ContextualAction ca : actions) {
			ca.bind(ctx);
		}
	}

	/**
	 * Cleans up the test fixture.
	 */
	@After
	public void tearDown() {
		mbb = null;
		actions = null;
	}

	/**
	 * We can create a menu bar which has no definitions (returns an empty
	 * menu bar).
	 */
	@Test
	public void createNonExistingBar() {
		JMenuBar m0 = mbb.createMenuBar("m0", actions);
		assertNotNull(m0);
		assertEquals(0, m0.getMenuCount());
	}

	/**
	 * Creates a simple menu bar with one menu with one item.
	 */
	@Test
	public void createBasicMenuBar() {
		JMenuBar m1 = mbb.createMenuBar("m1", actions);
		assertNotNull(m1);
		assertEquals(1, m1.getMenuCount());
		JMenu menu1 = m1.getMenu(0);
		assertNotNull(menu1);
		assertEquals("M1", menu1.getText());
		assertEquals(1, menu1.getItemCount());
		JMenuItem mi1 = menu1.getItem(0);
		assertNotNull(mi1);
		assertEquals("Foo", mi1.getText());
	}

	/**
	 * Creates a complex menu bar.
	 */
	@Test
	public void createComplexMenuBar() {
		JMenuBar m2 = mbb.createMenuBar("m2", actions);
		assertNotNull(m2);

		assertEquals(2, m2.getMenuCount());
		JMenu menu1 = m2.getMenu(0);
		assertNotNull(menu1);
		assertEquals("M1", menu1.getText());

		JMenu menu2 = m2.getMenu(1);
		assertNotNull(menu2);
		assertEquals("M2", menu2.getText());
		assertEquals(4, menu2.getItemCount());
		JMenuItem m21 = menu2.getItem(0);
		assertNotNull(m21);
		assertEquals("Bar", m21.getText());
		JMenuItem m22 = menu2.getItem(1);
		assertNull(m22);
		JMenuItem m23 = menu2.getItem(2);
		assertNotNull(m23);
		assertTrue(m23 instanceof JMenu);
		JMenu menu3 = (JMenu) m23;
		JMenuItem m24 = menu2.getItem(3);
		assertNotNull(m24);
		assertEquals("Xpto", m24.getText());

		assertEquals("M3", menu3.getText());
		assertEquals(2, menu3.getItemCount());
		JMenuItem m31 = menu3.getItem(0);
		assertNull(m31);
		JMenuItem m32 = menu3.getItem(1);
		assertNotNull(m32);
		assertEquals("Glu", m32.getText());
	}
}
