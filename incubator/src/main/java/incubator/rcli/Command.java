package incubator.rcli;

public interface Command {
	boolean process_cmd(String line, Session s, boolean human_readable);
}
