package org.sa.rainbow.brass.p3_cp1.probes;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import org.sa.rainbow.core.ConfigHelper;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.util.RainbowLogger;
import org.sa.rainbow.translator.probes.AbstractProbe;

public class PowerModelChangeProbe extends AbstractProbe implements Runnable {

	private static final String PROBE_TYPE = "powermodelupdateprobe";
	private long m_sleepTime;
	private String m_path;
	private WatchService m_watcher;
	private WatchKey m_watchKey;
	private Thread m_watchTrhead;
	private Path m_filePath;
	private boolean m_deactivated;
	
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
		m_filePath = FileSystems.getDefault().getPath(m_path);
		if (!m_filePath.getParent().toFile().exists()) {
			RainbowLogger.error(RainbowComponentT.PROBE, MessageFormat.format("The path containing the file '{0}' does not exist", m_path), getLoggingPort(), LOGGER);
		}
		else {
			try {
				m_watcher = FileSystems.getDefault().newWatchService();
				m_watchKey = m_filePath.getParent().register(m_watcher, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
				m_deactivated = false;
				m_watchTrhead = new Thread(this);
				m_watchTrhead.start();
			} catch (IOException e) {
				RainbowLogger.error(RainbowComponentT.PROBE, "Failed to activate probe " + id(), e, getLoggingPort(),
						LOGGER);
			}
		}
		
	}
	
	@Override
	public synchronized void deactivate() {
		m_deactivated = true;
		super.deactivate();
	}

	@Override
	public void run() {
		boolean ok = true;
		while (!m_deactivated && ok) {
			try {
				final WatchKey wk = m_watcher.poll(m_sleepTime, TimeUnit.MILLISECONDS);
				if (wk != null) {
					for (WatchEvent<?> event : wk.pollEvents()) {
						if (m_filePath.getFileName().equals((Path )event.context())) {
							reportData(m_filePath.toFile().getAbsolutePath());
						}
					}
					ok = wk.reset();
				}
			} catch (InterruptedException e) {
			}
		}
	}

}
