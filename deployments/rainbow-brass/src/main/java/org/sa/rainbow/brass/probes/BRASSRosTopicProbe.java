package org.sa.rainbow.brass.probes;

import org.ho.yaml.Yaml;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.util.RainbowLogger;
import org.sa.rainbow.translator.probes.AbstractProbe;
import org.sa.rainbow.translator.probes.IBashBasedScript;
import org.sa.rainbow.util.Util;

import java.io.*;
import java.util.Date;
import java.util.Map;

/**
 * Created by schmerl on 12/26/2016.
 */
public class BRASSRosTopicProbe extends AbstractProbe implements IBashBasedScript {

    private static final String PROBE_TYPE = "rostopicprobe";

    private String  m_path;
    private String  m_params;
    private Process m_process;
    
    
    public class ErrorStreamGobbler extends Thread {
    	private InputStream m_inputStream;

		public ErrorStreamGobbler(InputStream is) {
    		m_inputStream = is;
    	}
		
		@Override
			public void run() {
			try {
				InputStreamReader isr = new InputStreamReader (m_inputStream);
                BufferedReader br = new BufferedReader (isr);
                String line = br.readLine ();
                while (line != null) {
                	System.err.println(line);
                	log(line);
                	line = br.readLine();
                }
			}
		}
    }

    public class StreamGobbler extends Thread {
        private final InputStream m_inputStream;

        public StreamGobbler (InputStream is) {
            m_inputStream = is;
        }

        @Override
        public void run () {
            try {
                InputStreamReader isr = new InputStreamReader (m_inputStream);
                BufferedReader br = new BufferedReader (isr);
                String line = br.readLine ();
                while (line != null) {
                    StringBuffer yaml = new StringBuffer (line + "\n");
                    while ((line = br.readLine ()) != null && !"---".equals (line) && !line.startsWith("Frame") && !line.contains("RPY (degree)")) {
                        yaml.append (line);
                        yaml.append ("\n");
                    }
                    //Map o = (Map) Yaml.load (yaml.toString ());
//                    yaml.append("\nobserving_time: ");
//                    yaml.append(new Date ().getTime ());
                    reportData (yaml.toString ());//Yaml.dump (o, true));
                    line = br.readLine ();
                }
            } catch (IOException ioe) {
                log (ioe.getMessage ());
            }
        }
    }

    public BRASSRosTopicProbe (String id, long sleepTime) {
        super (id, PROBE_TYPE, Kind.JAVA);
    }

    public BRASSRosTopicProbe (String id, long sleepTime, String[] args) {
        this (id, sleepTime);
        if (args.length == 2) {
            m_path = args[0];
            m_params = args[1];
        }
    }


//    /**
//     * Main Constuctor that initializes the ID of this Probe.
//     *
//     * @param refID the unique identifier of the Probe
//     * @param alias the type name of the Probe
//     */
//    public BRASSRosTopicProbe (String refID, String alias, String path, String paramStr) {
//        super (refID, alias, Kind.SCRIPT);
//        m_path = path;
//        m_params = paramStr;
//    }

    @Override
    public synchronized void activate () {
        super.activate ();
        String[] cmds = new String[3];
        switch (Rainbow.instance ().environment ()) {
            case CYGWIN:
                cmds[0] = CYGWIN_BASH;
                break;
            case LINUX:
                cmds[0] = LINUX_BASH;
                Util.setExecutablePermission (m_path);
                break;
        }
        if (cmds[0] == null)
            deactivate ();
        else {
            cmds[1] = BASH_OPT;
            cmds[2] = m_path + " " + m_params;

            ProcessBuilder pb = new ProcessBuilder (cmds);
            File workDir = new File (m_path).getParentFile ();
            pb.directory (workDir);
//        pb.redirectErrorStream (true);
            try {
                m_process = pb.start ();
                StreamGobbler outputProcessor = new StreamGobbler (m_process.getInputStream ());
                ErrorStreamGobbler esg = new ErrorStreamGobbler(m_process.getErrorStream());
                outputProcessor.start ();
                esg.start();
            } catch (IOException e) {
                RainbowLogger.error (RainbowComponentT.PROBE, "Process I/O failed!", e, getLoggingPort (), LOGGER);

            }
        }
    }

    @Override
    public synchronized void deactivate () {
        if (m_process != null) {
            dumpOutput ();

            m_process.destroy ();
            log ("- Process destroyed.");
            m_process = null;
        }
        super.deactivate ();
    }

    @Override
    public boolean isAlive () {
        return true;
//        boolean alive = true;
//        if (m_process != null) {
//            if (m_process.exitValue () == 0) {
//                m_process = null;
//            } else {
//                alive = false;
//                dumpOutput ();
//
//            }
//        }
//        return alive;
    }

    private void dumpOutput () {
        log ("- STDOUT+STDERR: ----\n" + Util.getProcessOutput (m_process));
    }
}
