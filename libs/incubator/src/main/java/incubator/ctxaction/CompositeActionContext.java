package incubator.ctxaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * <p>
 * Action context which is populated by concatenating several action
 * contexts. Action contexts are concatenated in order and if there are
 * repeated context items the first one to define the items will be the
 * final one.
 * </p>
 * <p>
 * This class is an action context so it can only be used from within the
 * AWT event dispatcher thread.
 * </p>
 * <p>
 * Individual items can be added to the composite action context directly
 * taking precedence over items that come from sub-contexts.
 * </p>
 */
public class CompositeActionContext extends ActionContext {
	/**
	 * The context providers.
	 */
	private final List<ActionContext> contexts;

	/**
	 * The listener that is added to the contexts to know when to rebuild
	 * the global context.
	 */
	private final ActionContextListener acl;

	/**
	 * Action context containing this context's data.
	 */
	private final ActionContext internal;

	/**
	 * Creates a new empty context.
	 */
	public CompositeActionContext() {
		contexts = new ArrayList<>();
		internal = new ActionContext();
		acl = new ActionContextListener() {
			@Override
			public void contextChanged(ActionContext context) {
				rebuildContext();
			}
		};

		internal.addActionContextListener(acl);
	}

	/**
	 * Adds a new context to the list.
	 * 
	 * @param ac the action context
	 */
	public void addActionContext(ActionContext ac) {
		if (ac == null) {
			throw new IllegalArgumentException("ac == null");
		}

		checkAwtThread();

		contexts.add(ac);
		ac.addActionContextListener(acl);
		rebuildContext();
	}

	/**
	 * Removes an action context from the list.
	 * 
	 * @param ac the action context to remove
	 */
	public void removeActionContext(ActionContext ac) {
		if (ac == null) {
			throw new IllegalArgumentException("ac == null");
		}

		if (!contexts.contains(ac)) {
			throw new IllegalStateException("action context is not "
					+ "registered.");
		}

		checkAwtThread();

		ac.removeActionContextListener(acl);
		contexts.remove(ac);
		rebuildContext();
	}

	/**
	 * Rebuilds the action context.
	 */
	private void rebuildContext() {
		Map<String, Object> context = new HashMap<>();
		for (ListIterator<ActionContext> it = contexts.listIterator(contexts
				.size()); it.hasPrevious();) {
			ActionContext ac = it.previous();
			context.putAll(ac.getAll());
		}

		context.putAll(internal.getAll());

		super.redefine(context);
	}

	/**
	 * Clears only the values directly set on this context (does not clear
	 * sub contexts).
	 */
	@Override
	public void clear() {
		internal.clear();
	}

	@Override
	public void clear(String key) {
		internal.clear(key);
	}

	@Override
	public void redefine(ActionContext context) {
		internal.redefine(context);
	}

	@Override
	public void redefine(Map<String, Object> data) {
		internal.redefine(data);
	}

	@Override
	public void redefine(String[] keys, Object[] values) {
		internal.redefine(keys, values);
	}

	@Override
	public void set(String key, Object value) {
		internal.set(key, value);
	}
}
