/*
 * The MIT License
 *
 * Copyright 2014 CMU ABLE Group.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
