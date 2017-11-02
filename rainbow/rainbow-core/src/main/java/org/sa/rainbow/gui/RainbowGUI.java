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
/**
 * Created June 30, 2006
 */
package org.sa.rainbow.gui;

import org.apache.commons.lang.NotImplementedException;
import org.sa.rainbow.core.*;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.globals.ExitState;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.*;
import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.OperationResult;
import org.sa.rainbow.core.ports.IRainbowReportingSubscriberPort.IRainbowReportingSubscriberCallback;
import org.sa.rainbow.core.util.Pair;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;
import org.sa.rainbow.util.Util;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * The GUI for observing the Rainbow infrastructure.
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class RainbowGUI implements IDisposable, IRainbowReportingSubscriberCallback {
    // Index values
    public static final int ID_MODEL_MANAGER      = 0;
    public static final int ID_ARCH_EVALUATOR     = 1;
    public static final int ID_ADAPTATION_MANAGER = 2;
    public static final int ID_EXECUTOR           = 3;
    public static final int ID_TARGET_SYSTEM      = 4;
    public static final int ID_TRANSLATOR         = 5;
    public static final int ID_GAUGES             = 6;
    public static final int ID_ORACLE_MESSAGE     = 7;
    public static final int ID_FILLER1            = 8;
    public static final int PANEL_COUNT           = 9;  // make sure m_colors has same count

    // Layout distances and other constants
    public static final int   PANEL_MARGIN     = 10;
    public static final int   PANEL_PADDING    = 8;
    public static final int   PANEL_BORDER     = 4;
    public static final int   PANEL_ROWS       = 3;
    public static final int   PANEL_COLUMNS    = 3;
    public static final int   TEXT_ROWS        = 10;
    public static final int   TEXT_COLUMNS     = 40;
    public static final float TEXT_FONT_SIZE   = 9.0f;
    public static final int   MAX_TEXT_LENGTH  = 100000;
    /** Convenience constant: size of text field to set to when Max is exceeded. */
    public static final int   TEXT_HALF_LENGTH = 50000;

    public static void main (String[] args) {
        boolean showHelp = false;

        int lastIdx = args.length - 1;
        for (int i = 0; i <= lastIdx; i++) {
            if (args[i].equals ("-h")) {
                showHelp = true;
            } else {
                System.err.println ("Unrecognized or incomplete argument " + args[i]);
                showHelp = true;
            }
        }
        if (showHelp) {
            System.out.println ("Usage:\n" + "  system property options {default}:\n"
                                        + "    rainbow.target    name of target configuration {default}\n"
                                        + "    rainbow.config    top config directory (org.sa.rainbow.config)\n" + " " +
                                        " options: \n"
                                        + "    -h          Show this help message\n" + "    -nogui      Don't show " +
                                        "the Rainbow GUI\n" + "\n"
                                        + "Option defaults are defined in <rainbow.target>/rainbow.properties");
            System.exit (RainbowConstants.EXIT_VALUE_ABORT);
        }

        RainbowGUI gui = new RainbowGUI (null);
        gui.display ();
    }


    private JFrame m_frame = null;

    private JTextArea[] m_textAreas = null;

    private JComponent[] m_panes = null;

    private Color[] m_colors = {
            /* purple */ new Color (188, 188, 250),
            /* pink */   new Color (255, 145, 255),
            Color.RED,
            /* green */  new Color (0, 255, 64),
            Color.CYAN,
            /* orange */ new Color (255, 128, 64),
            Color.BLUE,
            Color.WHITE,
            Color.GRAY
    };

    private int[] m_order = {7, 2, 8, 3, 0, 1, 5, 4, 6};
    private IMasterCommandPort       m_master;
    private IModelDSBusPublisherPort m_dsPort;
    private IModelUSBusPort          m_usPort;

    private GUIGaugeLifecycleListener m_gaugeListener;
    private IGaugeLifecycleBusPort    m_gaugeLifecyclePort;
    private IEffectorLifecycleBusPort m_effectorListener;
    private IEffectorLifecycleBusPort m_effectorLifecyclePort;

    public RainbowGUI (IMasterCommandPort master) {
        m_master = master;
        try {
            if (m_master == null) {
//                RainbowPortFactory.createDelegateMasterConnectionPort (null);
                m_master = RainbowPortFactory.createMasterCommandPort ();
            }
            IRainbowReportingSubscriberPort reportingSubscriberPort = RainbowPortFactory
                    .createReportingSubscriberPort (this);
            reportingSubscriberPort.subscribe (EnumSet.allOf (RainbowComponentT.class),
                                               EnumSet.allOf (ReportType.class));

        } catch (RainbowConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace ();
        }
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.core.IDisposable#dispose()
     */
    @Override
    public void dispose () {
        m_frame.setVisible (false);
        m_frame.dispose ();
        m_frame = null;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.core.IDisposable#isDisposed()
     */
    @Override
    public boolean isDisposed () {
        return m_frame == null;
    }

    public void quit () {
        Rainbow.instance ().signalTerminate ();
    }

    public void forceQuit () {
        new Thread (new Runnable () {
            @Override
            public void run () {
                m_master.destroyDelegates ();
                Rainbow.instance ().signalTerminate ();
                Util.pause (IRainbowRunnable.LONG_SLEEP_TIME);
                while (Rainbow.instance ().getThreadGroup ().activeCount () > 0) {
                    try {
                        Thread.sleep (100);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace ();
                    }
                }
                System.exit (RainbowConstants.EXIT_VALUE_DESTRUCT);
            }
        }).start ();
        int ret = JOptionPane.showOptionDialog (m_frame, "Waiting for Rainbow to shutdown. Continue to wait?",
                                                "Quitting", JOptionPane.YES_NO_OPTION, JOptionPane
                                                        .INFORMATION_MESSAGE, null, null, null);

        if (ret == JOptionPane.NO_OPTION) {
            System.exit (RainbowConstants.EXIT_VALUE_ABORT);
        }
    }

    public void display () {
        if (m_frame != null) return;

        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater (new Runnable () {
            @Override
            public void run () {
                show ();
            }
        });
    }

    /**
     * Writes text to the panel without a newline.
     * @param panelID
     * @param text
     */
    public void writeTextSL (int panelID, String text) {
        if (panelID == ID_ORACLE_MESSAGE) {
            OracleStatusPanel oracleStatusPanel = (OracleStatusPanel) m_panes[panelID];
            if (oracleStatusPanel == null) return;
            ((OracleStatusPanel) m_panes[panelID]).report (text, false);
            return;
        }
        if (m_textAreas[panelID] == null) return;
        m_textAreas[panelID].append (text);
        m_textAreas[panelID].setCaretPosition (m_textAreas[panelID].getText ().length ());
    }

    public void writeText (int panelID, String text) {
        if (panelID == ID_ORACLE_MESSAGE) {
            OracleStatusPanel oracleStatusPanel = (OracleStatusPanel) m_panes[panelID];
            if (oracleStatusPanel == null) return;
            oracleStatusPanel.report (text, true);
            return;
        }
        if (m_textAreas[panelID] == null) return;
        m_textAreas[panelID].append (text + "\n");
        m_textAreas[panelID].setCaretPosition (m_textAreas[panelID].getText ().length ());
        if (m_textAreas[panelID].getText ().length () > MAX_TEXT_LENGTH) {
            m_textAreas[panelID].setText (m_textAreas[panelID].getText ().substring (TEXT_HALF_LENGTH));
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the event-dispatching thread.
     */
    private void show () {
        if (m_frame != null) return;

        // Use Window's default decorations.
        JFrame.setDefaultLookAndFeelDecorated (false);
        JDialog.setDefaultLookAndFeelDecorated (false);

        m_textAreas = new JTextArea[PANEL_COUNT];
        m_panes = new JComponent[PANEL_COUNT];

        //Create and set up the window.
        m_frame = new JFrame ("Rainbow Framework GUI - Target " + Rainbow.instance ().getProperty (RainbowConstants
                                                                                               .PROPKEY_TARGET_NAME));
        m_frame.setDefaultCloseOperation (JFrame.DO_NOTHING_ON_CLOSE);
        m_frame.addWindowListener (new WindowAdapter () {
            @Override
            public void windowClosing(WindowEvent e) {
                quit();
            }
        });

        //Create the menu bar with light gray background.
        JMenuBar menuBar = new JMenuBar();
        menuBar.setOpaque(true);
        menuBar.setBackground(Color.lightGray);

        JMenu menu = new JMenu("Oracle");
        menu.setMnemonic(KeyEvent.VK_O);
        createOracleMenu(menu);
        menuBar.add(menu);

        menu = new JMenu("Delegate");
        menu.setMnemonic(KeyEvent.VK_D);
        createDelegateMenu(menu);
        menuBar.add(menu);

        menu = new JMenu ("Info");
        menu.setMnemonic (KeyEvent.VK_I);
        createInformationMenu (menu);
        menuBar.add (menu);

        menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        createHelpMenu(menu);
        menuBar.add(menu);

        //Set the menu bar
        m_frame.setJMenuBar(menuBar);

        // add text areas to the content pane.
        Container contentPane = m_frame.getContentPane();
        contentPane.getInsets().set(10, 10, 10, 10);
        GridBagLayout gridBag = new GridBagLayout();
        contentPane.setLayout(gridBag);
        Throwable error = null;
        for (int i : m_order) {
            if (i == ID_ORACLE_MESSAGE) {
                List<String> expectedDelegateLocations;
                try {
                    expectedDelegateLocations = m_master.getExpectedDelegateLocations ();
                }
                catch (Throwable e) {
                    expectedDelegateLocations = Arrays.asList ("Error");
                    error = e;
                }
                m_panes[i] = new OracleStatusPanel (m_colors[i], expectedDelegateLocations);
            }
            else {
                m_panes[i] = createTextArea (i);
                if (m_colors[i] == Color.GRAY) {
                    m_panes[i].setVisible(false);
                }
            }
            contentPane.add (m_panes[i]);
        }
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.insets = new Insets(1,1,1,1);

        /* Proceed to set constraints in the following order:
         *  | 1 2 3 |
         *  | 4 5 6 |
         *  | 7 8 9 |
         */
        for (int i=0; i < m_order.length; ++i) {
            // anchor WEST constraint against edge to the left
            int x = i % 3;
            int y = i / 3;
            c.gridx = x;
            c.gridy = y;
            gridBag.setConstraints(m_panes[m_order[i]], c);
        }

        // Display the window.
        m_frame.pack();
        m_frame.setVisible(true);

        if (error != null) {
            JOptionPane.showMessageDialog (m_frame, "Could not connect to the master", "Connection error",
                    JOptionPane.ERROR_MESSAGE);
        }

    }

    private void createInformationMenu (final JMenu menu) {

        JMenu gauges = new JMenu ("Gauges");
        gauges.setMnemonic (KeyEvent.VK_G);
        menu.add (gauges);

        // Set up listener for gauge creation and deletion, and set create a port to the lifecycle port
        m_gaugeListener = new GUIGaugeLifecycleListener (gauges);
        try {
            m_gaugeLifecyclePort = RainbowPortFactory.createManagerLifecylePort (m_gaugeListener);
        }
        catch (RainbowConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace ();
        }

        JMenu effectors = new JMenu ("Effectors");
        gauges.setMnemonic (KeyEvent.VK_E);
        menu.add (effectors);
        m_effectorListener = new GUIEffectorLifecycleListener (effectors);
        try {
            m_effectorLifecyclePort = RainbowPortFactory.createClientSideEffectorLifecyclePort (m_effectorListener);
        }
        catch (RainbowConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace ();
        }
    }


    private JComponent createTextArea (int area) {
        m_textAreas[area] = new JTextArea(TEXT_ROWS, TEXT_COLUMNS);
        m_textAreas[area].setFont(m_textAreas[area].getFont().deriveFont(TEXT_FONT_SIZE));
        m_textAreas[area].setEditable (false);
        m_textAreas[area].setLineWrap (true);
        m_textAreas[area].setWrapStyleWord(true);
        m_textAreas[area].setAutoscrolls(true);
        m_panes[area] = new JScrollPane(m_textAreas[area],
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        m_panes[area].setAutoscrolls (true);
        Border border = BorderFactory.createMatteBorder(
                PANEL_BORDER, PANEL_BORDER, PANEL_BORDER, PANEL_BORDER,
                m_colors[area]);
        m_panes[area].setBorder(border);
        return m_panes[area];
    }

    /**
     * Creates a series of Oracle-specific menu items.
     * @param menu  the menu on which to create items.
     */
    private void createOracleMenu (JMenu menu) {
        JMenuItem item;

        // Management menu item
        item = new JMenuItem("Toggle adaptation switch");
        item.setMnemonic (KeyEvent.VK_A);
        item.setToolTipText ("Toggles whether self-adaptation is enabled, default is ON");
        item.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed (ActionEvent e) {
                boolean b = Rainbow.instance ().getRainbowMaster ().isAdaptationEnabled ();
                Rainbow.instance ().getRainbowMaster ().enableAdaptation (!b);

                //        		boolean b = !((AdaptationManager )Oracle.instance().adaptationManager())
                // .adaptationEnabled();
//        		((AdaptationManager )Oracle.instance().adaptationManager()).setAdaptationEnabled(b);
                writeText (ID_ORACLE_MESSAGE, "Adaptation switched " + (b ? "ON" : "OFF"));
            }
        });
        menu.add(item);

        item = new JMenuItem ("Identify consoles");
        item.setMnemonic (KeyEvent.VK_I);
        item.setToolTipText ("Prints a message to each console to identify it");
        item.addActionListener (new ActionListener () {

            @Override
            public void actionPerformed (ActionEvent e) {
                writeText (0, "MODEL_MANAGER");
                writeText (1, "Arch Evaluator");
                writeText (2, "Adaptation Manager");
                writeText (3, "Executor");
                writeText (4, "Target system");
                writeText (5, "Translator");
                writeText (6, "Event bus");
                writeText (7, "Management");

            }
        });
        menu.add (item);

        item = new JMenuItem("Clear consoles");
        item.setMnemonic (KeyEvent.VK_C);
        item.setToolTipText ("Clears all the GUI consoles");
        item.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed (ActionEvent e) {
                for (JTextArea textArea : m_textAreas) {
                    textArea.setText ("");
                }
            }
        });
        menu.add(item);
        menu.add(new JSeparator());
        // Termination menu item
        item = new JMenuItem("Sleep Master+Delegate");
        item.setMnemonic(KeyEvent.VK_S);
        item.setToolTipText ("Signals Oracle and all Delegates to terminate, then sleep");
        item.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed (ActionEvent e) {
                quit ();
            }
        });
        item.setEnabled (false);
        menu.add (item);
        item = new JMenuItem("Restart Master+Delegate");
        item.setMnemonic(KeyEvent.VK_R);
        item.setToolTipText ("Signals Oracle and all Delegates to terminate, then restart");
        item.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed (ActionEvent e) {
                Rainbow.instance ().signalTerminate (ExitState.RESTART);
            }
        });
        item.setEnabled (false);

        menu.add(item);
        item = new JMenuItem("Destroy Master+Delegate");
        item.setMnemonic(KeyEvent.VK_D);
        item.setToolTipText("Signals Oracle and all Delegates to terminate, then self-destruct");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                m_master.destroyDelegates ();
                Rainbow.instance ().signalTerminate (ExitState.DESTRUCT);
            }
        });
        menu.add(item);
        menu.add(new JSeparator());
        // Quit menu item
        item = new JMenuItem("Force Quit");
        item.setMnemonic(KeyEvent.VK_Q);
        item.setToolTipText("Forces the Oracle component to quit immediately");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                forceQuit();
            }
        });
        menu.add(item);
    }

    /**
     * Creates a series of Delegate-specific menu items.
     * @param menu  the menu on which to create items.
     */
    private void createDelegateMenu (JMenu menu) {
        JMenuItem item;

        // Probe start menu item
        item = new JMenuItem("Start Probes");
        item.setMnemonic (KeyEvent.VK_P);
        item.setToolTipText ("Signals all Delegates to start the probes (key: rainbow.delegate.startProbesOnInit)");
        item.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed (ActionEvent e) {
                m_master.startProbes ();
                //        		if (! Rainbow.inSimulation()) {
//                	((SystemDelegate )Oracle.instance().targetSystem()).signalStartProbes();
//        		}
            }
        });
        menu.add(item);
        // Probe kill menu item
        item = new JMenuItem("Kill Probes");
        item.setMnemonic (KeyEvent.VK_K);
        item.setToolTipText ("Signals all Delegates to kill the probes");
        item.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed (ActionEvent e) {
                m_master.killProbes ();
                //        		if (! Rainbow.inSimulation()) {
//                	((SystemDelegate )Oracle.instance().targetSystem()).signalKillProbes();
//        		}
            }
        });
        item.setEnabled (false);


        menu.add(item);
        menu.add(new JSeparator());

        // Test Effector 1 menu item
        item = new JMenuItem("T1 KillDelegate Effector");
        item.setMnemonic (KeyEvent.VK_1);
        item.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed (ActionEvent e) {
                String hostname = JOptionPane.showInputDialog (m_frame, "Please provide hostname of Delegate to kill");
                if (hostname != null && hostname.length () > 0) {
                    m_master.killDelegate (hostname);
                }
            }
        });
        menu.add(item);
        // Test Effector 2 menu item
        item = new JMenuItem("T2 Test An Effector");
        item.setMnemonic (KeyEvent.VK_2);
        item.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed (ActionEvent e) {
                String effID = JOptionPane.showInputDialog (m_frame, "Please identify Effector to test: " +
                        "'name@location' (or just 'name' for localhost)");
                if (effID == null || effID.length () == 0) {
                    writeText (ID_ORACLE_MESSAGE, "Sorry, Oracle needs to know what effector to invoke!");
                }
                Pair<String, String> namePair = Util.decomposeID (effID);
                if (namePair.secondValue () == null) {  // default to localhost
                    namePair.setSecondValue ("localhost");
                }
                String argStr = JOptionPane.showInputDialog (m_frame, "Please provide String arguments, separated by " +
                        "'|'");
                String[] args;
                if (argStr == null || argStr.length () == 0) {
                    args = new String[0];
                } else {
                    args = argStr.split ("\\s*\\|\\s*");
                }
                // run the test
                testEffector (namePair.secondValue (), namePair.firstValue (), args);
            }
        });
        menu.add(item);
        // Test an operation menu item
        item = new JMenuItem ("T3 Test An Operation");
        item.setMnemonic (KeyEvent.VK_3);
        item.addActionListener (new ActionListener () {

            @Override
            public void actionPerformed (ActionEvent e) {
                String operationName = JOptionPane.showInputDialog (m_frame,
                                                                    "Please identify the Operation to test:");
                if (operationName == null || operationName.isEmpty ()) {
                    writeText (ID_ORACLE_MESSAGE, "Sorry, Oracel needs to know what operation to invoke.");
                }

                String argStr = JOptionPane.showInputDialog (m_frame,
                                                             "Please provide string arguments, separated by ','");
                String[] args;
                if (argStr == null || argStr.isEmpty ()) {
                    args = new String[0];
                } else {
                    args = argStr.split ("\\s*,\\s*");
                }
                String modelRef = JOptionPane
                        .showInputDialog (m_frame,
                                          "Please identify the model to run the operation on: modelName:modelType (or" +
                                                  " just 'modelName' for Acme)");
                ModelReference model = Util.decomposeModelReference (modelRef);
                if (model.getModelType () == null || model.getModelType ().isEmpty ()) {
                    model = new ModelReference (model.getModelName (), "Acme");
                }

                // Publish the operation
                testOperation (model, operationName, args);
            }
        });
        menu.add (item);

        item = new JMenuItem ("T4 Change the model");
        item.setMnemonic (KeyEvent.VK_4);
        item.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed (ActionEvent e) {
                String mr = JOptionPane.showInputDialog (m_frame,
                                                         "Please identify the model to run the operation on: " +
                                                                 "modelName:modelType (or" +
                                                                 " just 'modelName' for Acme)");
                ModelReference model = Util.decomposeModelReference (mr);
                if (model.getModelType () == null || model.getModelType ().isEmpty ()) {
                    model = new ModelReference (model.getModelName (), "Acme");
                } else {
                    writeText (ID_ORACLE_MESSAGE, "Sorry, we need to know the model that will be changed");
                    return;
                }


                String operation = JOptionPane.showInputDialog (m_frame, "Please identify a model operation to test");
                if (operation == null || operation.isEmpty ()) {
                    writeText (ID_ORACLE_MESSAGE, "Sorry, we need to know what model operation to conduct");
                    return;
                }

                String argStr = JOptionPane.showInputDialog (m_frame,
                                                             "Please provide string arguments, separated by ','");
                String[] args;
                if (argStr == null || argStr.isEmpty ()) {
                    args = new String[0];
                } else {
                    args = argStr.split ("\\s*,\\s*");
                }

                testModelOperation (model, operation, args);
            }
        });

        menu.add(new JSeparator());
        // Delegate control menu item
        item = new JMenuItem("Restart Delegates");
        item.setMnemonic(KeyEvent.VK_R);
        item.setToolTipText ("Signals all the Delegates to terminate and restart");
        item.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed (ActionEvent e) {
                throw new NotImplementedException ();
//        		// issues restart to all delegates
//        		signalDelegates(ServiceConstants.SVC_CMD_RESTART);
            }
        });
        item.setEnabled (false);

        menu.add (item);
        item = new JMenuItem("Sleep Delegates");
        item.setMnemonic(KeyEvent.VK_S);
        item.setToolTipText ("Signals all the Delegates to terminate and sleep");
        item.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed (ActionEvent e) {
                throw new NotImplementedException ();
//        		// issues sleep to all delegates
//        		signalDelegates(ServiceConstants.SVC_CMD_SLEEP);
            }
        });
        item.setEnabled (false);


        menu.add(item);
        item = new JMenuItem("Destroy Delegates");
        item.setMnemonic(KeyEvent.VK_D);
        item.setToolTipText("Signals all the Delegates to terminate and the self-destruct");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                m_master.destroyDelegates ();
                //        		// issues destroy to all delegates
//        		signalDelegates(ServiceConstants.SVC_CMD_STOP);
            }
        });
        menu.add(item);
        menu.add(new JSeparator());

        // RainDropD control
        item = new JMenuItem("Awaken RainDropD...");
        item.setMnemonic(KeyEvent.VK_A);
        item.setToolTipText ("Given a hostname, awakens the Delegate RainDrop Daemon on that host");
        item.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed (ActionEvent e) {
                throw new NotImplementedException ();
//        		String hostname = JOptionPane.showInputDialog(m_frame, "Please provide a hostname with a sleeping
// RainDropD");
//        		if (hostname != null && hostname.length() > 0) {
//        			RemoteControl.waker(hostname, RemoteControl.WAKER_RESTART);
//        		}
            }
        });
        item.setEnabled (false);

        menu.add (item);
        item = new JMenuItem("Kill RainDropD...");
        item.setMnemonic(KeyEvent.VK_L);
        item.setToolTipText ("Given a hostname, kills the Delegate RainDropD on that host");
        item.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed (ActionEvent e) {
                throw new NotImplementedException ();
//        		String hostname = JOptionPane.showInputDialog(m_frame, "Please provide a hostname with a sleeping
// RainDropD");
//        		if (hostname != null && hostname.length() > 0) {
//        			RemoteControl.waker(hostname, RemoteControl.WAKER_KILL);
//        		}
            }
        });
        item.setEnabled (false);

        menu.add(item);

    }


    /**
     * Creates the help menu items.
     * @param menu  the menu on which to create items.
     */
    private void createHelpMenu (JMenu menu) {
        JMenuItem item;

        item = new JMenuItem("Software Update...");
        item.setMnemonic(KeyEvent.VK_U);
        item.setToolTipText("Allows the update of the Oracle and RainbowDelegate software components");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
//        		String hostname = JOptionPane.showInputDialog(m_frame,
//        				"Please provide a hostname to send update software.\n'*' for all delegate hosts.");
//        		if (hostname != null && hostname.length() > 0) {
//            		final JFileChooser fc = new JFileChooser(Rainbow.instance().getBasePath());
//            		int rv = fc.showDialog(m_frame, "Select File");
//            		if (rv == JFileChooser.APPROVE_OPTION) {
//            			File file = fc.getSelectedFile();
//        				writeText(ID_ORACLE_MESSAGE, "Attempting remote update on " + hostname + " with " + file.getAbsolutePath());
//                		RemoteControl.updater(hostname, file.getParentFile(), file.getName());
//            		}
//        		}
                throw new NotImplementedException ();
            }
        });
        item.setEnabled (false);

        menu.add(item);
        menu.add(new JSeparator());
        item = new JMenuItem("About");
        item.setMnemonic(KeyEvent.VK_A);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                JOptionPane.showMessageDialog(m_frame, "Will be available soon...", "No Help", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        menu.add(item);
    }

    private void signalDelegates (String cmd) {
//		if (! Rainbow.inSimulation()) {
//			String[] locs = ((SystemDelegate )Oracle.instance().targetSystem()).delegateLocations();
//			for (String loc : locs) {
//				if (loc.equals(Rainbow.property(Rainbow.PROPKEY_MASTER_LOCATION))) {
//                    continue;
//                }
//				writeText(ID_ORACLE_MESSAGE, "Signalling RainbowDelegate@" + loc + " to " + cmd);
//				RemoteControl.restarter(loc, cmd);
//			}
//		}
    }


    // GUI invoked test methods
    private void testModelOperation (ModelReference model, String operation, String[] args) {
        OperationRepresentation or = new OperationRepresentation (operation, model, args[0], Arrays.copyOfRange (args,
                                                                                                                 1,
                                                                                                                 args
                                                                                                                         .length));
        or.setOrigin ("GUI");
        if (m_usPort == null) {
            try {
                m_usPort = RainbowPortFactory.createModelsManagerClientUSPort (new Identifiable () {
                    @Override
                    public String id () {
                        return "GUI";
                    }
                });
                m_usPort.updateModel (or);
            } catch (RainbowConnectionException e) {
                writeText (ID_ORACLE_MESSAGE, "Failed to publish the operation to the model");
            }
        }
    }


    private void testEffector (String target, String effName, String[] args) {
        String message = "Testing Effector " + effName + "@" + target + Arrays.toString (args);
        writeText (ID_EXECUTOR, message);
        Outcome outcome = m_master.testEffector (target, effName, Arrays.asList (args));
        JOptionPane.showMessageDialog (m_frame, message + " - outcome: " + outcome);
        writeText (ID_EXECUTOR, message + " - outcome: " + outcome);
//        writeText(ID_EXECUTOR, "Testing Effector " + effName + Arrays.toString(args));
//        IEffector.Outcome outcome = Rainbow.instance().sysOpProvider().execute(effName, target, args);
//        writeText(ID_EXECUTOR, "  - outcome: " + outcome);
    }

    private void testOperation (ModelReference modelRef, String opName, String[] args) {
        OperationRepresentation or = new OperationRepresentation (opName, modelRef,
                args[0], Arrays.copyOfRange (args, 1, args.length));
        if (m_dsPort == null) {
            try {
                m_dsPort = RainbowPortFactory.createModelDSPublishPort (new Identifiable () {


                    @Override
                    public String id () {
                        return "UI";
                    }
                });

            }
            catch (RainbowConnectionException e) {
                writeText (ID_ORACLE_MESSAGE, "Failed to publish the operation.");
            }
        }
        OperationResult result = m_dsPort.publishOperation (or);
        String msg = modelRef.toString () + "." + opName + Arrays.toString (args) + " - returned "
                + result.result.name () + ": "
                + result.reply;
        writeText (ID_ORACLE_MESSAGE,
                msg);
        JOptionPane.showMessageDialog (m_frame, msg);

    }

    @Override
    public void report (RainbowComponentT component, ReportType type, String message) {
//        public static final int ID_MODEL_MANAGER = 0;
//        public static final int ID_ARCH_EVALUATOR = 1;
//        public static final int ID_ADAPTATION_MANAGER = 2;
//        public static final int ID_EXECUTOR = 3;
//        public static final int ID_TARGET_SYSTEM = 4;
//        public static final int ID_TRANSLATOR = 5;
//        public static final int ID_EVENT_BUSES = 6;
//        public static final int ID_ORACLE_MESSAGE = 7;
        String msg = MessageFormat.format ("[{0}]: {1}", type.toString (), message);

        Util.dataLogger ().info (msg);
        int panel = ID_ORACLE_MESSAGE;
        switch (component) {
        case ADAPTATION_MANAGER:
            panel = ID_ADAPTATION_MANAGER;
            break;
        case ANALYSIS:
            panel = ID_ARCH_EVALUATOR;
            break;
        case DELEGATE:
        case MASTER:
            panel = ID_ORACLE_MESSAGE;
            break;
        case EFFECTOR:
        case EFFECTOR_MANAGER:
            panel = ID_TRANSLATOR;
            break;
        case EXECUTOR:
            panel = ID_EXECUTOR;
            break;
        case GAUGE:
        case GAUGE_MANAGER:
            panel = ID_GAUGES;
            break;
        case MODEL:
            panel = ID_MODEL_MANAGER;
            break;
        case PROBE:
        case PROBE_MANAGER:
            panel = ID_TARGET_SYSTEM;
            break;
        case SELECTOR:
            panel = ID_ADAPTATION_MANAGER;
            break;
        }
        writeText (panel, msg);
    }

}
