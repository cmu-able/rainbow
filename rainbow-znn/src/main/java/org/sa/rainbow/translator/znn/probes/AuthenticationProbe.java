package org.sa.rainbow.translator.znn.probes;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.sa.rainbow.translator.probes.AbstractRunnableProbe;

public class AuthenticationProbe extends AbstractRunnableProbe {

    private static final String PROBE_TYPE = "authenticationprobe";
    private String              m_authenticationFile;

    public AuthenticationProbe (String id, long sleepTime) {
        super (id, PROBE_TYPE, Kind.JAVA, sleepTime);
    }

    public AuthenticationProbe (String id, long sleepTime, String[] args) {
        this (id, sleepTime);
        if (args.length == 1) {
            m_authenticationFile = args [0];
        }
    }

    @Override
    public void run () {
        Path filePath = new File (m_authenticationFile).toPath ();
        Path dir = filePath.getParent ();
        if (!dir.toFile ().exists ()) {
            LOGGER.error ("The directory to watch for the authentication file '" + m_authenticationFile + "' does not exist!");
            tallyError ();
        }
        WatchService watcher = null;
        WatchKey key = null;
        try {
            watcher = FileSystems.getDefault ().newWatchService ();

            key = dir.register (watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            Thread currentThread = Thread.currentThread ();

            while (thread () == currentThread && isActive ()) {
                try {
                    Thread.sleep (sleepTime ());
                }
                catch (InterruptedException e) {
                }
                key = watcher.poll ();
                if (key != null) {
                    for (WatchEvent<?> event : key.pollEvents ()) {
                        WatchEvent.Kind<?> kind = event.kind ();
                        if (kind == OVERFLOW) {
                            continue;
                        }
                        WatchEvent<Path> ev = (WatchEvent<Path> )event;
                        Path filename = ev.context ();
                        if (filename.equals (filePath)) {
                            if (kind == ENTRY_DELETE) {
                                reportData ("off");

                            }
                            else {
                                reportData ("on");

                            }
                        }
                    }
                }
            }
        }
        catch (IOException e1) {
            LOGGER.error ("Could not create a watcher for the authentication file '" + m_authenticationFile + "'.", e1);
            tallyError ();
        }


    }

}
