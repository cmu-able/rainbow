package org.sa.rainbow.gui;

import org.jetbrains.annotations.NotNull;
import org.sa.rainbow.core.Rainbow.ExitState;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.ports.IMasterCommandPort;
import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.core.ports.IRainbowReportingSubscriberPort;
import org.sa.rainbow.core.ports.IRainbowReportingSubscriberPort.IRainbowReportingSubscriberCallback;
import org.sa.rainbow.core.ports.RainbowPortFactory;

import java.util.EnumSet;

public class CommandLineUI {

    static class Keyin {

        //*******************************
        //   support methods
        //*******************************
        //Method to display the user's prompt string
        public static void printPrompt (String prompt) {
            System.out.print (prompt + " ");
            System.out.flush ();
        }

        //Method to make sure no data is available in the
        //input stream
        public static void inputFlush () {
            int dummy;
            int bAvail;

            try {
                while ((System.in.available ()) != 0) {
                    dummy = System.in.read ();
                }
            }
            catch (java.io.IOException e) {
                System.out.println ("Input error");
            }
        }

        //********************************
        //  data input methods for
        //string, int, char, and double
        //********************************
        @NotNull
        public static String inString (String prompt) {
            inputFlush ();
            printPrompt (prompt);
            return inString ();
        }

        @NotNull
        public static String inString () {
            int aChar;
            String s = "";
            boolean finished = false;

            while (!finished) {
                try {
                    aChar = System.in.read ();
                    if (aChar < 0 || (char )aChar == '\n') {
                        finished = true;
                    }
                    else if ((char )aChar != '\r') {
                        s = s + (char )aChar; // Enter into string
                    }
                }

                catch (java.io.IOException e) {
                    System.out.println ("Input error");
                    finished = true;
                }
            }
            return s;
        }

        public static int inInt (String prompt) {
            while (true) {
                inputFlush ();
                printPrompt (prompt);
                try {
                    return Integer.valueOf (inString ().trim ());
                }

                catch (NumberFormatException e) {
                    System.out.println ("Invalid input. Not an integer");
                }
            }
        }

        public static char inChar (String prompt) {
            int aChar = 0;

            inputFlush ();
            printPrompt (prompt);

            try {
                aChar = System.in.read ();
            }

            catch (java.io.IOException e) {
                System.out.println ("Input error");
            }
            inputFlush ();
            return (char )aChar;
        }

        public static double inDouble (String prompt) {
            while (true) {
                inputFlush ();
                printPrompt (prompt);
                try {
                    return Double.valueOf (inString ().trim ());
                }

                catch (NumberFormatException e) {
                    System.out.println ("Invalid input. Not a floating point number");
                }
            }
        }
    }

    public static void main (String[] args) throws RainbowConnectionException {
        IMasterCommandPort master = RainbowPortFactory.createMasterCommandPort ();
        IRainbowReportingSubscriberPort reportingSubscriberPort = RainbowPortFactory
                .createReportingSubscriberPort (new IRainbowReportingSubscriberCallback () {

                    @Override
                    public void report (RainbowComponentT component, @NotNull ReportType type, String message) {
                        if (type == ReportType.ERROR || type == ReportType.FATAL || type == ReportType.WARNING) {
                            System.out.println (type.name () + ": " + message);
                        }
                        if (component == RainbowComponentT.GAUGE) {
                            System.out.println ("Gauge: " + message);
                        }
                    }
                });
        reportingSubscriberPort.subscribe (EnumSet.allOf (RainbowComponentT.class), EnumSet.allOf (ReportType.class));
        boolean exit = false;
        while (!exit) {
            System.out.println ("====================");
            System.out.println ("|  Rainbow Control |");
            System.out.println ("====================");
            System.out.println ("| Options:         |");
            System.out.println ("|  1. Start Probes |");
            System.out.println ("|  2. Terminate    |");
            System.out.println ("|  3. Enable Adapt |");
            System.out.println ("|  4. Disable Adapt|");
            System.out.println ("|  5. Exit         |");
            int choice = Keyin.inInt ("Select option: ");
            switch (choice) {
            case 1:
                master.startProbes ();
                break;
            case 2:
                master.terminate (ExitState.DESTRUCT);
                break;
            case 3:
                master.enableAdaptation (true);
                break;
            case 4:
                master.enableAdaptation (false);
                break;
            case 5:
                exit = true;
                break;
            }
        }
        System.exit (1);
    }

}
