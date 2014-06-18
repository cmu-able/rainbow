package org.sa.rainbow.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.sa.rainbow.core.ports.IEffectorLifecycleBusPort;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;
import org.sa.rainbow.translator.effectors.IEffectorIdentifier;

public class GUIEffectorLifecycleListener implements IEffectorLifecycleBusPort {
    static private final SimpleDateFormat          SDF            = new SimpleDateFormat ("YYYY-MM-dd HH:mm:ss");

    private JMenu                                  m_menu;
    Map<String, JMenuItem>                         itemMap        = new HashMap<> ();
    private Map<String, List<EffectorInformation>> informationMap = new HashMap<> ();

    class EffectorInformation {
        Outcome      outcome;
        List<String> args;
        Date         executed;

        EffectorInformation (Outcome o, List<String> a, Date d) {
            outcome = o;
            args = a;
            executed = d;

        }
    }

    public GUIEffectorLifecycleListener (JMenu menu) {
        m_menu = menu;
    }

    @Override
    public void reportCreated (final IEffectorIdentifier effector) {
        if (!itemMap.containsKey (effector.id ())) {
            JMenuItem item = new JMenuItem (effector.id ());
            item.addActionListener (new ActionListener () {

                @Override
                public void actionPerformed (ActionEvent e) {
                    List<EffectorInformation> i = informationMap.get (effector.id ());
                    if (i == null) {
                        JOptionPane.showMessageDialog (m_menu.getComponent (), "The effector has not been executed");
                    }
                    else {
                        StringBuffer msg = new StringBuffer ();
                        msg.append (MessageFormat.format ("Execution history for: {0}\n", effector.id ()));
                        for (EffectorInformation ei : i) {
                            msg.append (SDF.format (ei.executed));
                            msg.append (": Outcome: ");
                            msg.append (ei.outcome.name ());
                            msg.append (", arguments: ");
                            msg.append (Arrays.toString (ei.args.toArray ()));
                            msg.append ("\n");
                        }
                        ElementInformationDialog dialog = new ElementInformationDialog ();
                        dialog.setInformation (msg.toString ());
                        dialog.setVisible (true);
                    }

                }
            });
        }
    }

    @Override
    public void reportDeleted (IEffectorIdentifier effector) {
        JMenuItem item = itemMap.get (effector.id ());
        if (item != null) {
            itemMap.remove (effector.id ());
            m_menu.remove (item);
        }
    }

    @Override
    public void reportExecuted (IEffectorIdentifier effector, Outcome outcome, List<String> args) {
        EffectorInformation ei = new EffectorInformation (outcome, args, new Date ());
        List<EffectorInformation> info = informationMap.get (effector.id ());
        if (info == null) {
            info = new LinkedList<> ();
            informationMap.put (effector.id (), info);
        }
        info.add (0, ei);
    }

    @Override
    public void dispose () {
        // TODO Auto-generated method stub

    }

}
