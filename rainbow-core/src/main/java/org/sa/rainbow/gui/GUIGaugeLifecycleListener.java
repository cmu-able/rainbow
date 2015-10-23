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


import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.gauges.IGaugeIdentifier;
import org.sa.rainbow.core.gauges.IGaugeState;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IGaugeLifecycleBusPort;
import org.sa.rainbow.core.ports.IGaugeQueryPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GUIGaugeLifecycleListener implements IGaugeLifecycleBusPort {

    private final JMenu m_menu;
    private final Map<String, JMenuItem> itemMap = new HashMap<> ();

    public GUIGaugeLifecycleListener (JMenu gauges) {
        m_menu = gauges;
    }

    @Override
    public void reportCreated (final IGaugeIdentifier gauge) {
        if (!itemMap.containsKey (gauge.id ())) {
            JMenuItem item = new JMenuItem (gauge.id ());
            itemMap.put (gauge.id (), item);
            item.addActionListener (new ActionListener () {

                @Override
                public void actionPerformed (ActionEvent e) {
                    try {
                        IGaugeQueryPort qPort = RainbowPortFactory.createGaugeQueryPortClient (gauge);
                        IGaugeState state = qPort.queryGaugeState ();
                        Collection<IRainbowOperation> commands = qPort.queryAllCommands ();
                        StringBuilder info = new StringBuilder ();
                        info.append ("---- Last Opertions ----\n");
                        if (commands.isEmpty ()) {
                            info.append ("  No operations issued.\n");
                        }
                        else {
                            for (IRainbowOperation op : commands) {
                                info.append (op.toString ());
                                info.append ("\n");
                            }
                        }

                        info.append ("---- State ----\n");
                        info.append ("  ---- Operations ----\n");
                        if (state.getGaugeReports ().isEmpty ()) {
                            info.append ("    No opertions issued\n");
                        }
                        else {
                            for (IRainbowOperation op : state.getGaugeReports ()) {
                                info.append ("    ");
                                info.append (op.toString ());
                                info.append ("\n");
                            }
                        }
                        info.append ("  ---- Configuration parameters ----\n");
                        for (TypedAttributeWithValue tav : state.getConfigParams ()) {
                            info.append (tav.getName ());
                            info.append (" = ");
                            info.append (tav.getValue ().toString ());
                            info.append ("\n");
                        }
                        info.append ("  ---- Setup parameters ----\n");
                        for (TypedAttributeWithValue tav : state.getSetupParams ()) {
                            info.append (tav.getName ());
                            info.append (" = ");
                            info.append (tav.getValue ().toString ());
                            info.append ("\n");
                        }

                        ElementInformationDialog dialog = new ElementInformationDialog ();
                        dialog.setInformation (info.toString ());
                        dialog.setSize (400, 400);
                        dialog.pack ();
                        dialog.setVisible (true);

                    }
                    catch (RainbowConnectionException e1) {
                        e1.printStackTrace ();
                    }

                }
            });
            m_menu.add (item);
        }
    }

    @Override
    public void reportDeleted (IGaugeIdentifier gauge) {
        if (itemMap.containsKey (gauge.id ())) {
            JMenuItem item = itemMap.get (gauge.id ());
            m_menu.remove (item);

        }
        itemMap.remove (gauge.id ());

    }

    @Override
    public void reportConfigured (IGaugeIdentifier gauge, List<TypedAttributeWithValue> configParams) {

    }

    @Override
    public void sendBeacon (IGaugeIdentifier gauge) {

    }

    @Override
    public void dispose () {

    }

}
