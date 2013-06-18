package incubator.rcli;

import incubator.dispatch.Dispatcher;

public interface Session {
	public String sid();
	public void output(String text);
	public String input();
	public Dispatcher<SessionListener> dispatcher();
	public boolean closed();
	public void close();
}
