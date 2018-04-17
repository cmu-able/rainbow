package org.sa.rainbow.brass.probes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;

import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.util.RainbowLogger;
import org.sa.rainbow.translator.probes.AbstractProbe;

public class TargetWaypointProbe extends AbstractProbe implements Runnable {
    private static final String PROBE_TYPE = "targetwaypointprobe";
	private String m_path;
	
	private WatchService m_watcher;
	private WatchKey m_watchKey;
	private Thread m_watchThread;
	private boolean m_deactivated;
	private Path m_filePath;
	private long m_sleepTime;

	public TargetWaypointProbe(String id, long sleepTime) {
		super(id, PROBE_TYPE, Kind.JAVA);
		m_sleepTime = sleepTime;
	}
	
	public TargetWaypointProbe(String id, long sleepTime, String[] args) {
		this(id, sleepTime);
		if (args.length == 1) {
			m_path = args[0];
		}
	}
	
	@Override
	public synchronized void activate() {
		super.activate();
		m_filePath = FileSystems.getDefault().getPath(m_path);
		if (!m_filePath.getParent().toFile().exists()) {
			RainbowLogger.error(RainbowComponentT.PROBE, "The path that should contain the file '" + m_path + "' does not exist", getLoggingPort(), LOGGER);
		}
		else {
			try {
				m_watcher = FileSystems.getDefault().newWatchService();
				m_watchKey = m_filePath.getParent().register(m_watcher, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE); 
				m_deactivated = false;
				m_watchThread = new Thread(this);
				m_watchThread.start();
			} catch (IOException e) {
				RainbowLogger.error(RainbowComponentT.PROBE, "Failed to activate probe " + id(), e, getLoggingPort(), LOGGER);
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
						if (m_filePath.equals((Path )event.context())) {
							try (BufferedReader reader = new BufferedReader(new FileReader(m_filePath.toFile()))) {
								StringBuilder builder = new StringBuilder();
								String line = null;
								String ls = System.getProperty("line.separator");
								while ((line = reader.readLine()) != null) {
									builder.append(line);
									builder.append(ls);
								}
								reportData(builder.toString());
							} catch (IOException e) {
								RainbowLogger.error(RainbowComponentT.PROBE, "Probe '" + id() + "' failed to read file '" + m_path + "'", e, getLoggingPort(), LOGGER);
							}
						}
					}
					
					ok = wk.reset();
				}
			} catch (InterruptedException e) {
			}
		}
	}

}
