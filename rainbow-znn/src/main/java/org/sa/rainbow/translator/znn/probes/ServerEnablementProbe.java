package org.sa.rainbow.translator.znn.probes;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.text.MessageFormat;

import org.sa.rainbow.translator.probes.AbstractRunnableProbe;

public class ServerEnablementProbe extends AbstractRunnableProbe {
    private static final String PROBE_TYPE = "serverenablement";
    private String              m_configurationFile;

    public ServerEnablementProbe (String id, long sleepTime) {
        super (id, PROBE_TYPE, Kind.JAVA, sleepTime);
    }

    public ServerEnablementProbe (String id, long sleepTime, String[] args) {
        this (id, sleepTime);
        if (args.length == 1) {
            m_configurationFile = args[0];
        }
    }

    @Override
    public void run () {
        Path filePath = new File (m_configurationFile).toPath ();
        Path dir = filePath.getParent ();
        if (!dir.toFile ().exists ()) {
            LOGGER.error (MessageFormat.format ("The directory to watch for configuration changes: ''{0}'' does not exist", m_configurationFile));
            return;
        }

        try {
            WatchService watcher = FileSystems.getDefault ().newWatchService ();
            WatchKey key = dir.register (watcher, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);

            Thread currentThread = Thread.currentThread ();
            while (thread () == currentThread && isActive ()) {
                try {
                    Thread.sleep (sleepTime ());
                }
                catch (InterruptedException e) {}
                key = watcher.poll ();
                if (key != null) {
                    for (WatchEvent<?> event : key.pollEvents ()) {
                        WatchEvent.Kind<?> kind = event.kind ();
                        if (kind == StandardWatchEventKinds.ENTRY_MODIFY || kind == StandardWatchEventKinds.ENTRY_CREATE) {
                            WatchEvent<Path> ev = (WatchEvent<Path> )event;
                            Path filename = ev.context ();
                            if (filename.equals (filePath)) {
                                try (BufferedReader reader = Files.newBufferedReader (filePath, Charset.forName ("US-ASCII"))) {
                                    String line = null;
//                                while ((line = reader.readLine ()))
                                }
                                catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }

                            }
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            LOGGER.error (MessageFormat.format ("Could not start a watcher on: ''{0}''", m_configurationFile), e);

        }
    }

}
