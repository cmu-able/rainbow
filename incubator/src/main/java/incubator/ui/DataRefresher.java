package incubator.ui;

import incubator.ctxaction.ActionContext;
import incubator.ctxaction.ContextualAction;

import javax.swing.JOptionPane;

/**
 * Class implementing an abstract data refresh feature. It will regularly
 * request to refresh the data through an abstract method that should be
 * overrided by subclasses. The main advantage of using this class instead
 * of, for example, using {@link Pinger} directly, is in its interaction
 * with visual controls to stop and change the refresh rate.
 */
public abstract class DataRefresher {
	/**
	 * Key to the action context that identifies a refreshes in context.
	 */
	private static final String CONTEXT_KEY = DataRefresher.class.getName();
	
	/**
	 * Key in the action context that identifies that the refresher is in 
	 * pause mode. Whatever is in the value of this key is irrelevant.
	 */
	private static final String PAUSED_CONTEXT_KEY = CONTEXT_KEY + ":p";
	
	/**
	 * Key in the action context that indicates that the refresher is
	 * refreshing. The value associated with this key is irrelevant.
	 */
	private static final String REFRESHING_CONTEXT_KEY = CONTEXT_KEY + ":r";
	
	/**
	 * Default refresh interval.
	 */
	private long m_default_refresh;
	
	/**
	 * Should refresh be sync with AWT?
	 */
	private boolean m_awt_sync;
	
	/**
	 * Current refresh rate, <code>0</code> means it is stopped.
	 */
	private long m_refresh;
	
	/**
	 * If <code>true</code>, it means refresh is stopped even if
	 * <code>m_refresh</code> is greater than zero.
	 */
	private boolean m_paused;
	
	/**
	 * Object receiving the pings.
	 */
	private Pinged m_pinged;
	
	/**
	 * Action context.
	 */
	private ActionContext m_context;
	
	/**
	 * Creates the new refresher.
	 * @param default_refresh the refresh interval in milliseconds,
	 * <code>0</code> means no refresh
	 * @param awt_sync should the refresh be done in sync with AWT?
	 * If so, it will be made from the AWT dispatcher thread
	 * @param context the context in which the object should be inserted
	 */
	public DataRefresher(long default_refresh, boolean awt_sync,
			ActionContext context) {
		this.m_default_refresh = default_refresh;
		this.m_awt_sync = awt_sync;
		this.m_refresh = 0;
		this.m_paused = false;
		this.m_context = context;
		context.set(PAUSED_CONTEXT_KEY, "");
		m_pinged = new Pinged() {
			@Override
			public void ping() {
				refresh();
			}
		};
		
		refresh_rate(default_refresh);
		context.set(CONTEXT_KEY, this);
	}
	
	/**
	 * Sets the refresh rate.
	 * @param refresh the refresh rate in milliseconds
	 */
	public void refresh_rate(long refresh) {
		if (refresh < 0) {
			throw new IllegalArgumentException("refresh < 0");
		}
		
		set_current_state(refresh, m_paused);
	}
	
	/**
	 * Sets whether the refresh should be stopped or not.
	 * @param paused should it pause?
	 */
	public void set_paused(boolean paused) {
		set_current_state(m_refresh, paused);
	}
	
	/**
	 * Sets the current state, in terms of refresh rate and pause state.
	 * @param refresh the new refresh rate in milliseconds
	 * @param paused should refresh be stopped?
	 */
	private void set_current_state(long refresh, boolean paused) {
		boolean is_running = this.m_refresh > 0 && !this.m_paused;
		boolean should_run = refresh > 0 && !paused;
		
		boolean need_shutdown = (is_running && !should_run);
		boolean need_startup = (!is_running && should_run);
		
		if (is_running && should_run && this.m_refresh != refresh) {
			need_shutdown = true;
			need_startup = true;
		}
		
		if (need_shutdown) {
			/*
			 * We move from an active refresh state to a "pause" state so
			 * we need to stop the ping.
			 */
			Pinger.unping(m_pinged);
		}
		
		if (need_startup) {
			/*
			 * We move from a pause state to an active refresh state so we
			 * need to register the pinged.
			 */
			Pinger.ping(m_pinged, refresh, m_awt_sync?
					Pinger.AWT_SYNC : Pinger.NORMAL);
		}
		
		this.m_refresh = refresh;
		this.m_paused = paused;
		
		if (should_run) {
			m_context.set(REFRESHING_CONTEXT_KEY, "");
			m_context.clear(PAUSED_CONTEXT_KEY);
		} else {
			m_context.clear(REFRESHING_CONTEXT_KEY);
			m_context.set(PAUSED_CONTEXT_KEY, "");
		}
	}
	
	/**
	 * Obtains the actual refresh rate.
	 * @return the current refresh rate in milliseconds
	 */
	public long refresh_rate() {
		return m_refresh;
	}
	
	/**
	 * Obtains the default refresh rate.
	 * @return the default rate in milliseconds
	 */
	public long default_refresh() {
		return m_default_refresh;
	}
	
	/**
	 * Checks whether refresh is stopped or not. This method may return
	 * <code>false</code> but refresh being halted because the refresh rate
	 * is <code>0</code>.
	 * @return is the refresh stopped?
	 */
	public boolean is_paused() {
		return m_paused;
	}
	
	/**
	 * Refresh the data.
	 */
	public abstract void refresh();
	
	/**
	 * Action that refreshes data.
	 */
	public static final class PauseRefreshAction extends ContextualAction {
		/**
		 * Version for serialization.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new action.
		 */
		public PauseRefreshAction() {
			/*
			 * Nothing to do.
			 */
		}

		@Override
		protected boolean isValid(ActionContext context) {
			DataRefresher r = (DataRefresher) context.get(CONTEXT_KEY);
			if (r == null) {
				return false;
			}
			
			if (r.is_paused() || r.refresh_rate() == 0) {
				return false;
			}
			
			return true;
		}

		@Override
		public void perform(ActionContext context) {
			DataRefresher r = (DataRefresher) context.get(CONTEXT_KEY);
			r.set_paused(true);
		}
	}
	
	/**
	 * Action that resumes refreshing.
	 */
	public static final class ResumeRefreshAction extends ContextualAction {
		/**
		 * Version for serialization.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new action.
		 */
		public ResumeRefreshAction() {
		}

		@Override
		protected boolean isValid(ActionContext context) {
			DataRefresher r = (DataRefresher) context.get(CONTEXT_KEY);
			if (r == null) {
				return false;
			}
			
			if (!r.is_paused() || r.refresh_rate() == 0) {
				return false;
			}
			
			return true;
		}

		@Override
		public void perform(ActionContext context) {
			DataRefresher r = (DataRefresher) context.get(CONTEXT_KEY);
			r.set_paused(false);
		}
	}
	
	/**
	 * Action that changes the refresh interval.
	 */
	public static final class SetRefreshIntervalAction
			extends ContextualAction {
		/**
		 * Version for serialization.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new action.
		 */
		public SetRefreshIntervalAction() {
		}

		@Override
		protected boolean isValid(ActionContext context) {
			DataRefresher r = (DataRefresher) context.get(CONTEXT_KEY);
			if (r == null) {
				return false;
			}
			
			return true;
		}

		@Override
		public void perform(ActionContext context) {
			DataRefresher r = (DataRefresher) context.get(CONTEXT_KEY);
			String v = JOptionPane.showInputDialog("New refresh interval "
					+ "in milliseconds: ", "" + r.refresh_rate());
			if (v != null) {
				try {
					int vint = Integer.parseInt(v);
					r.refresh_rate(vint);
				} catch (NumberFormatException e) {
					/*
					 * Bah, the user doesn't know how to input numbers.
					 * He won't be able to change the refresh rate if he
					 * keeps doing this...
					 */
				}
			}
		}
	}
}
