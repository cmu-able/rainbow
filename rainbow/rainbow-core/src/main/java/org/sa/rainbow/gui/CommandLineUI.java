package org.sa.rainbow.gui;

import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.globals.ExitState;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.*;
import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.core.ports.IRainbowReportingSubscriberPort.IRainbowReportingSubscriberCallback;
import org.sa.rainbow.core.util.Pair;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort;
import org.sa.rainbow.util.Util;

import java.util.Arrays;
import java.util.EnumSet;

public class CommandLineUI {

    private static IModelUSBusPort          m_usPort;
    private static IModelDSBusPublisherPort m_dsPort;

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

            try {
                while ((System.in.available ()) != 0) {
                    dummy = System.in.read ();
                }
            } catch (java.io.IOException e) {
                System.out.println ("Input error");
            }
        }

        //********************************
        //  data input methods for
        //string, int, char, and double
        //********************************

        public static String inString (String prompt) {
            inputFlush ();
            printPrompt (prompt);
            return inString ();
        }


        public static String inString () {
            int aChar;
            String s = "";
            boolean finished = false;

            while (!finished) {
                try {
                    aChar = System.in.read ();
                    if (aChar < 0 || (char) aChar == '\n') {
                        finished = true;
                    } else if ((char) aChar != '\r') {
                        s = s + (char) aChar; // Enter into string
                    }
                } catch (java.io.IOException e) {
                    System.out.println ("Input error");
                    finished = true;
                }
            }
            return s;
        }

        public static Integer inInt (String prompt) {
            while (true) {
                inputFlush ();
                printPrompt (prompt);
                try {
                    return Integer.valueOf (inString ().trim ());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }

        public static char inChar (String prompt) {
            int aChar = 0;

            inputFlush ();
            printPrompt (prompt);

            try {
                aChar = System.in.read ();
            } catch (java.io.IOException e) {
                System.out.println ("Input error");
            }
            inputFlush ();
            return (char) aChar;
        }

        public static double inDouble (String prompt) {
            while (true) {
                inputFlush ();
                printPrompt (prompt);
                try {
                    return Double.valueOf (inString ().trim ());
                } catch (NumberFormatException e) {
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
            System.out.println ("|  5. Change Model |");
            System.out.println ("|  6. Test effector |");
            System.out.println ("|  7. Test operation |");
            System.out.println ("|  0. Exit         |");
            Integer choice = Keyin.inInt ("Select option: ");
            if (choice != null)
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
                        getAndAffectModelChange ();
                        break;
                    case 6:
                        getAndTestEffector (master);
                        break;
                    case 7:
                        getAndTestOperation (master);
                        break;
                    case 0:
                        exit = true;
                        break;
                }
        }
        System.exit (1);
    }

    private static void getAndTestOperation (IMasterCommandPort master) {
        String opName = Keyin.inString ("Please identify the Operation to test:");
        if (opName == null || opName.isEmpty ()) {
            System.out.println ("The operation must be specified");
            return;
        }
        final String argStr = Keyin.inString ("Please provide String arguments, separated by " +
                                                      "'|'");
        String[] args;
        if (argStr == null || argStr.isEmpty ())
            args = new String[0];
        else
            args = argStr.split ("\\s*\\|\\s*");
        String modelRef = Keyin.inString ("Please identify the model to run the operation on: \nmodelName:modelType " +
                                                  "(or just 'modelName' for Acme)");

        ModelReference model = Util.decomposeModelReference (modelRef);
        if (model.getModelType () == null || model.getModelType ().isEmpty ()) {
            model = new ModelReference (model.getModelName (), "Acme");
        }
        OperationRepresentation or = new OperationRepresentation (opName, model, args[0], Arrays.copyOfRange (args,
                                                                                                              1, args.length));
        if (m_dsPort == null) {
            try {
                m_dsPort = RainbowPortFactory.createModelDSPublishPort (new Identifiable () {
                    @Override
                    public String id () {
                        return "Console";
                    }
                });
            } catch (RainbowConnectionException e) {
                System.out.println ("Failed to publish the operation");
                return;
            }
        }
        IModelDSBusPublisherPort.OperationResult result = m_dsPort.publishOperation (or);
        String msg = modelRef.toString () + "." + opName + Arrays.toString (args) + " - returned "
                + result.result.name () + ": "
                + result.reply;
        System.out.println (msg);
    }


    private static void getAndTestEffector (IMasterCommandPort master) {
        final String effId = Keyin.inString ("Please identify Effector to test (name@location): ");
        if (effId == null || effId.isEmpty ()) {
            System.out.println ("An effector must be specified");
            return;
        }
        Pair<String, String> namePair = Util.decomposeID (effId);
        if (namePair.secondValue () == null)
            namePair.setSecondValue ("localhost");
        final String argStr = Keyin.inString ("Please provide String arguments, separated by " +
                                                      "'|'");
        String[] args;
        if (argStr == null || argStr.isEmpty ())
            args = new String[0];
        else
            args = argStr.split ("\\s*\\|\\s*");
        String message = "Testing Effector " + namePair.firstValue () + "@" + namePair.secondValue () + Arrays
                .toString (args);
        System.out.println (message);
        IEffectorExecutionPort.Outcome o = master.testEffector (namePair.secondValue (), namePair.firstValue (),
                                                                Arrays.asList (args));
        System.out.println (message + " - outcome: " + o);
    }

    private static void getAndAffectModelChange () {
        final String model = Keyin.inString ("Model to change:");
        ModelReference mr = Util.decomposeModelReference (model);
        if (mr.getModelType () == null || mr.getModelType ().isEmpty ()) {
            mr = new ModelReference (mr.getModelName (), "Acme");
        }
        if (mr.getModelName () == null || mr.getModelName ().isEmpty ()) {
            System.out.println ("The name of the model must be specified");
            return;
        }
        final String operation = Keyin.inString ("Operation to perform:");
        if (operation == null || operation.isEmpty ()) {
            System.out.println ("The operation must be specified");
            return;

        }
        final String parameters = Keyin.inString ("Operation parameters (comma separated):");
        String[] args;
        if (parameters == null || parameters.isEmpty ()) {
            args = new String[0];
        } else {
            args = parameters.split ("\\s*,\\s*");
        }

        OperationRepresentation or = new OperationRepresentation (operation, mr, args[0], Arrays.copyOfRange (args,
                                                                                                              1, args
                                                                                                                      .length));
        or.setOrigin ("Console");
        try {
            if (m_usPort == null) {
                m_usPort = RainbowPortFactory.createModelsManagerClientUSPort (new Identifiable () {
                    @Override
                    public String id () {
                        return "Console";
                    }
                });
                System.out.println ("Sending: " + or.toString ());
                m_usPort.updateModel (or);
            }
        } catch (RainbowConnectionException e) {
            System.out.println ("Could not send operation " + or.toString ());
        }


    }

}
