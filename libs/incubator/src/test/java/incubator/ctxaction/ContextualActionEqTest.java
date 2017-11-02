package incubator.ctxaction;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import incubator.ctxaction.ActionContext;
import incubator.ctxaction.ContextualAction;

/**
 * Equivalence class tests for the {@link ContextualAction} class.
 */
public class ContextualActionEqTest extends Assert {
	/**
	 * Disables AWT thread checking.
	 */
	@Before
	public void setup() {
		ActionContext.disableAwtThreadCheck();
	}

	/**
	 * Creates a custom action. Checks that the action is created,
	 * validation and occurs correctly. Creates a menu item and a button and
	 * verifies that both contain the correct name, icon and tool tip.
	 * 
	 * @throws Exception test failed
	 */
	@Test
	public void createCustomAction() throws Exception {
		final String res[] = new String[1];
		ActionContext ac = new ActionContext();
		ContextualAction ca = new ContextualAction(
				"incubator.ctxaction.test-1",
				"foo") {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean isValid(ActionContext context) {
				return context.get("foo") != null;
			}

			@Override
			protected void perform(ActionContext context) {
				res[0] = (String) context.get("foo");
			}
		};
		ca.bind(ac);

		assertEquals("fooid", ca.getId());

		assertFalse(ca.isEnabled());
		ac.set("foo", "bar");
		assertTrue(ca.isEnabled());

		ca.actionPerformed(new ActionEvent("foo", 1, "foo"));
		assertEquals("bar", res[0]);

		JMenuItem mi = ca.createJMenuItem(true);
		assertEquals("Test menu item", mi.getText());
		assertEquals("Description of menu item", mi.getToolTipText());

		KeyStroke ks = KeyStroke.getKeyStroke(Character.valueOf('t'),
				InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK);
		assertEquals(ks, mi.getAccelerator());

		assertEquals(KeyEvent.VK_T, mi.getMnemonic());
		Icon icon = mi.getIcon();
		assertNotNull(icon);
		assertEquals(16, icon.getIconWidth());
		assertEquals(16, icon.getIconHeight());

		JButton b = ca.createJButton(true);
		assertEquals("Test menu item", b.getText());
		assertEquals("Description of menu item", b.getToolTipText());

		assertEquals(KeyEvent.VK_T, b.getMnemonic());
		icon = b.getIcon();
		assertNotNull(icon);
		assertEquals(16, icon.getIconWidth());
		assertEquals(16, icon.getIconHeight());
	}

	/**
	 * Creates an action determining automatically the name of the resource
	 * bundle and name of the class.
	 * 
	 * @throws Exception test failed
	 */
	@Test
	public void createActionWithAutoBundleAndName() throws Exception {
		ActionContext ac = new ActionContext();

		MyContextualAction ca = new MyContextualAction();
		ca.bind(ac);

		assertNull(ca.getId());

		JMenuItem mi = ca.createJMenuItem(true);
		assertEquals("Test menu item", mi.getText());
	}
	
	/**
	 * When an action is executed, all listeners are informed.
	 * 
	 * @throws Exception test failed
	 */
	@Test
	public void executingActionInformsListeners() throws Exception {
		ActionContext ac = new ActionContext();

		MyContextualAction ca = new MyContextualAction();
		ca.bind(ac);
		
		/**
		 * Test listener that will register if invoked.
		 */
		class MyListener implements ContextualActionListener {
			/**
			 * How many times has the listener been invoked?
			 */
			private int invoked = 0;

			@Override
			public void actionPerformed() {
				invoked++;
			}
		}
		
		MyListener ml = new MyListener();
		
		ca.addContextualActionListener(ml);
		assertEquals(0, ml.invoked);
		
		ac.set("foo", "foo");
		assertTrue(ca.isEnabled());
		assertEquals(0, ml.invoked);
		
		ca.actionPerformed(new ActionEvent(new Object(), 1, "foo"));
		assertEquals(1, ml.invoked);
	}

	/**
	 * Test action.
	 */
	private static class MyContextualAction extends ContextualAction {
		/**
		 * Version for serialization.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new action.
		 */
		MyContextualAction() {
			super();
		}

		@Override
		protected boolean isValid(ActionContext context) {
			return context.get("foo") != null;
		}

		@Override
		protected void perform(ActionContext context) {
			/*
			 * Nothing to do.
			 */
		}
	}
}
