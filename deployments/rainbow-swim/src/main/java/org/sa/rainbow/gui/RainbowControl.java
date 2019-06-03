package org.sa.rainbow.gui;

import java.text.MessageFormat;
import java.util.EnumSet;

import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.globals.ExitState;
import org.sa.rainbow.core.ports.IMasterCommandPort;
import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.core.ports.IRainbowReportingSubscriberPort;
import org.sa.rainbow.core.ports.IRainbowReportingSubscriberPort.IRainbowReportingSubscriberCallback;
import org.sa.rainbow.core.ports.RainbowPortFactory;

public class RainbowControl {

    private static void usage() {
    	String usage = MessageFormat.format(
    			"Usage: {0} command\n\tcommand='{startprobes|killprobes|terminate}'", RainbowControl.class.getSimpleName());
    	System.out.println(usage);
    }
    
    public static void main (String[] args) throws RainbowConnectionException {
    	if (args.length != 1) {
    		usage();
    		return;
    	}
    	
        IMasterCommandPort master = RainbowPortFactory.createMasterCommandPort ();
        IRainbowReportingSubscriberPort reportingSubscriberPort = RainbowPortFactory
                .createReportingSubscriberPort (new IRainbowReportingSubscriberCallback () {

                    @Override
                    public void report (RainbowComponentT component, ReportType type, String message) {
                        if (type == ReportType.ERROR || type == ReportType.FATAL || type == ReportType.WARNING) {
                            System.out.println (type.name () + ": " + message);
                        }
                        if (component == RainbowComponentT.GAUGE) {
                            System.out.println ("Gauge: " + message);
                        }
                    }
                });
        reportingSubscriberPort.subscribe (EnumSet.allOf (RainbowComponentT.class), EnumSet.allOf (ReportType.class));
        
        if (args[0].equals("startprobes")) {
            master.startProbes ();
        } else if (args[0].equals("killprobes")) {
        	master.killProbes();
        } else if (args[0].equals("terminate")) {
            master.terminate (ExitState.DESTRUCT);
        } else {
        	usage();
        }
        System.exit (1);
    }
}
