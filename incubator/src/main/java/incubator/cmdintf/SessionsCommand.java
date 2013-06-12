package incubator.cmdintf;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Abstract command which is able to print a set of sessions.
 */
public abstract class SessionsCommand extends AbstractCommand {
	/**
	 * Creates a new command.
	 * 
	 * @param name the name of the command
	 * @param description a description for the command
	 */
	public SessionsCommand(String name, String description) {
		super(name, description);
	}

	/**
	 * Writes the given sessions in the order in which they were provided.
	 * 
	 * @param sessions the sessions to write
	 */
	protected void writeSessions(List<Session> sessions) {
		assert sessions != null;

		String[][] data = new String[sessions.size() + 1][];
		int idx = 0;
		data[idx] = new String[] { "Session ID", "Created", "Running",
				"Run time", "Last" };
		idx++;

		DateFormat df = DateFormat.getDateTimeInstance();

		for (Session s : sessions) {
			Command r = s.getRunningCommand();
			Command l = s.getLastCommand();
			data[idx] = new String[] {
					"" + s.getSessionId(),
					df.format(s.getSessionStart()),
					(r == null ? "Idle" : r.getName()),
					new Date().getTime() - s.getRunningStart().getTime()
							+ " ms", (l == null ? "None" : l.getName()) };
			idx++;
		}

		writeData(data);
	}

	/**
	 * Writes a set of sessions (sorted by session ID) to the end user.
	 * 
	 * @param unsortedSessions the sessions to show
	 */
	protected void writeSessions(Set<Session> unsortedSessions) {
		Set<Session> sessions = new TreeSet<>(new Comparator<Session>() {
			@Override
			public int compare(Session o1, Session o2) {
				return o1.getSessionId() - o2.getSessionId();
			}
		});
		sessions.addAll(unsortedSessions);

		List<Session> slist = new ArrayList<>();
		slist.addAll(sessions);
		writeSessions(slist);
	}

	/**
	 * Writes a formatted table of data.
	 * 
	 * @param data the data to write. Individual entries (except in the first
	 * line) may be <code>null</code>
	 */
	private void writeData(String[][] data) {
		assert data != null;
		assert data.length > 0;
		assert data[0] != null;
		assert data[0].length > 0;

		int[] w = new int[data[0].length];
		for (int i = 0; i < data[0].length; i++) {
			int mw = 0;
			for (int j = 0; j < data.length; j++) {
				if (data[j][i] != null) {
					int l = data[j][i].length();
					if (l > mw) {
						mw = l;
					}
				} else {
					data[j][i] = "";
				}
			}

			w[i] = mw;
		}

		StringBuffer sb = new StringBuffer();
		sb.append("+-");
		for (int i = 0; i < w.length; i++) {
			for (int j = 0; j < w[i]; j++) {
				sb.append("-");
			}

			sb.append("-+");
			if (i < w.length - 1) {
				sb.append("-");
			}
		}

		writeLine(sb.toString());
		for (int i = 0; i < data.length; i++) {
			StringBuffer l = new StringBuffer();
			l.append("| ");
			for (int j = 0; j < w.length; j++) {
				StringBuffer t = new StringBuffer();
				t.append(data[i][j]);
				while (t.length() < w[j]) {
					t.append(" ");
				}

				l.append(t.toString());
				l.append(" |");
				if (j < w.length - 1) {
					l.append(" ");
				}
			}

			writeLine(l.toString());
			if (i == 0 || i == data.length - 1) {
				writeLine(sb.toString());
			}
		}
	}
}
