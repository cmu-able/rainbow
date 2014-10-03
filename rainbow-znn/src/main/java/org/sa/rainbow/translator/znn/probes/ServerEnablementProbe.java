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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.sa.rainbow.translator.probes.AbstractRunnableProbe;

public class ServerEnablementProbe extends AbstractRunnableProbe {
    private static final String PROBE_TYPE = "enablementprobe";
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
            LOGGER.error (MessageFormat.format (
                    "The directory to watch for configuration changes: ''{0}'' does not exist", m_configurationFile));
            return;
        }

        Set<String> initialIPs = readBalancerFile (filePath);

        try {
            WatchService watcher = FileSystems.getDefault ().newWatchService ();
            WatchKey key = dir.register (watcher, StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_CREATE);

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
                        if (kind == StandardWatchEventKinds.ENTRY_MODIFY
                                || kind == StandardWatchEventKinds.ENTRY_CREATE) {
                            WatchEvent<Path> ev = (WatchEvent<Path> )event;
                            Path filename = ev.context ();
                            if (filename.equals (filePath.getFileName ())) {
                                Set<String> oldIPs = new HashSet<> (initialIPs);
                                initialIPs = readBalancerFile (filePath);
                                Set<String> ipsInFile = new HashSet<> (initialIPs);
                                for (Iterator i = ipsInFile.iterator (); i.hasNext ();) {
                                    String ip = (String )i.next ();
                                    if (oldIPs.contains (ip)) {
                                        i.remove ();
                                        oldIPs.remove (ip);
                                    }
                                }
                                // oldIPs now contains IPs that are no longer active
                                // ipsInFile now contains IPs that are new
                                StringBuffer report = new StringBuffer ();
                                if (!oldIPs.isEmpty ()) {
                                    for (String ip : oldIPs) {
                                        report.append ("f ");
                                        report.append (ip);
                                        report.append (" ");
                                    }
                                }
                                if (!ipsInFile.isEmpty ()) {
                                    for (String ip : ipsInFile) {
                                        report.append ("o ");
                                        report.append (ip);
                                        report.append (" ");
                                    }
                                }
                                if (report.length () > 0) {
                                    report.deleteCharAt (report.length () - 1);
                                    reportData (report.toString ());
                                }
                            }
                        }
                    }
                    watcher = FileSystems.getDefault ().newWatchService ();
                    key = dir.register (watcher, StandardWatchEventKinds.ENTRY_MODIFY,
                            StandardWatchEventKinds.ENTRY_CREATE);
                }
            }
        }
        catch (IOException e) {
            LOGGER.error (MessageFormat.format ("Could not start a watcher on: ''{0}''", m_configurationFile), e);

        }
    }

    Set<String> readBalancerFile (Path filePath) {
        Set<String> ipsInFile = new HashSet<> ();
        try (BufferedReader reader = Files.newBufferedReader (filePath, Charset.forName ("US-ASCII"))) {
            String line = null;
            while ((line = reader.readLine ()) != null) {
                String[] tokens = line.split (" ");
                if ("BalancerMember".equals (tokens[0])) {
                    tokens = tokens[1].split ("/");
                    ipsInFile.add (tokens[2]);
                }
            }
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace ();
        }
        return ipsInFile;
    }

}
