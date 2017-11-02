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
package org.sa.rainbow.gui;


import org.sa.rainbow.core.ports.IEffectorLifecycleBusPort;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;
import org.sa.rainbow.translator.effectors.IEffectorIdentifier;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class GUIEffectorLifecycleListener implements IEffectorLifecycleBusPort {
    static private final SimpleDateFormat          SDF            = new SimpleDateFormat ("YYYY-MM-dd HH:mm:ss");

    private final JMenu m_menu;
    private final Map<String, JMenuItem> itemMap = new HashMap<> ();
    private final Map<String, List<EffectorInformation>> informationMap = new HashMap<> ();

    class EffectorInformation {
        final Outcome outcome;
        final List<String> args;
        final Date executed;

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
                        StringBuilder msg = new StringBuilder ();
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
