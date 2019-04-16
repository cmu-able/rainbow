package org.sa.rainbow.brass.p3_cp1.probes;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.MessageFormat;

import org.sa.rainbow.core.ConfigHelper;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.util.RainbowLogger;
import org.sa.rainbow.translator.probes.AbstractProbe;

public class PowerModelChangeProbe extends AbstractProbe {

	private static final String PROBE_TYPE = "powermodelupdateprobe";
	private Object m_sleepTime;
	private String m_path;
	private FileWatcher m_fw;

	public PowerModelChangeProbe(String id, long sleepTime) {
		super(id, PROBE_TYPE, Kind.JAVA);
		m_sleepTime = sleepTime;
	}
	
	public PowerModelChangeProbe(String id, long sleepTime, String args) {
		this(id, sleepTime);
		m_path = ConfigHelper.convertToAbsolute(args);
	}
	
	@Override
	public synchronized void activate() {
		super.activate();
		Path fullPath = FileSystems.getDefault().getPath(m_path);
		if (!fullPath.getParent().toFile().exists()) {
			RainbowLogger.error(RainbowComponentT.PROBE, MessageFormat.format("The path containing the file '{0}' does not exist", m_path), getLoggingPort(), LOGGER);
		}
		else {
			m_fw = new FileWatcher(m_path, () -> reportData(fullPath.toAbsolutePath().toString()));
			m_fw.start();
		}
	}
	
	@Override
	public synchronized void deactivate() {
		m_fw.cancel();
		super.deactivate();
	}

}
