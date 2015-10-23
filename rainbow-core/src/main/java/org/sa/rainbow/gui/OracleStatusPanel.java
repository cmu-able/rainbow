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


import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OracleStatusPanel extends JPanel {


    private final JTextArea m_textArea;

    static final Pattern NO_HEARTBEAT = Pattern.compile ("\\[ERROR\\]: No Heartbeat from (.*)@(.*)");
    static final Pattern HEARTBEAT    = Pattern.compile ("\\[INFO\\]: Heartbeat from (.*)@(.*)");


    Map<String, JLabel>  labels       = new HashMap<> ();

    private JPanel       m_statusPane;

    /**
     * Create the panel.
     */
    public OracleStatusPanel (Color color, List<String> delegates) {
        setLayout (new BorderLayout (0, 0));

        m_statusPane = new JPanel ();
        FlowLayout flowLayout = (FlowLayout )m_statusPane.getLayout ();
        flowLayout.setAlignment (FlowLayout.LEFT);
        m_statusPane.setBackground (Color.WHITE);
        m_statusPane.setBorder (new LineBorder (new Color (0, 0, 0)));
        m_statusPane.setAlignmentX (Component.LEFT_ALIGNMENT);
        add (m_statusPane, BorderLayout.PAGE_START);

        JLabel lblNewLabel = new JLabel ("Status:");
        m_statusPane.add (lblNewLabel);

        m_textArea = new JTextArea (RainbowGUI.TEXT_ROWS, RainbowGUI.TEXT_COLUMNS);
        m_textArea.setFont (m_textArea.getFont ().deriveFont (RainbowGUI.TEXT_FONT_SIZE));
        m_textArea.setEditable (false);
        m_textArea.setLineWrap (true);
        m_textArea.setWrapStyleWord (true);
        m_textArea.setAutoscrolls (true);

        m_textArea.setAlignmentY (Component.TOP_ALIGNMENT);
        m_textArea.setAlignmentX (Component.RIGHT_ALIGNMENT);

        JScrollPane sp = new JScrollPane (m_textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setAutoscrolls (true);
        BorderFactory.createMatteBorder (RainbowGUI.PANEL_BORDER, RainbowGUI.PANEL_BORDER,
                                         RainbowGUI.PANEL_BORDER, RainbowGUI.PANEL_BORDER, color);

        add (sp);

        expectedDelegates (delegates);
    }

    public void expectedDelegates (List<String> locations) {
        for (String loc : locations) {
            JLabel label = getDelegateLabel (null, loc);
            label.setBorder (new LineBorder (Color.black));
        }
    }

    public void report (String report, boolean nl) {
        Matcher noHB = NO_HEARTBEAT.matcher (report);
        if (noHB.matches ()) {
            String did = noHB.group (1);
            String location = noHB.group (2);
            JLabel label = getDelegateLabel (did, location);
            label.setBackground (Color.red);
            return;
        }
        Matcher HB = HEARTBEAT.matcher (report);
        if (HB.matches ()) {
            JLabel label = getDelegateLabel (HB.group (1), HB.group (2));
            label.setBackground (Color.green);
            return;
        }

        m_textArea.append (report + (nl ? "\n" : ""));
        m_textArea.setCaretPosition (m_textArea.getText ().length ());
        if (m_textArea.getText ().length () > RainbowGUI.MAX_TEXT_LENGTH) {
            m_textArea.setText (m_textArea.getText ().substring (RainbowGUI.TEXT_HALF_LENGTH));
        }

    }

    JLabel getDelegateLabel (String did, String location) {
        JLabel label = labels.get (location);
        if (label == null) {
            label = new JLabel (location);
            label.setOpaque (true);
            labels.put (location, label);
            m_statusPane.add (label);
            m_statusPane.validate ();
        }
        label.setVisible (true);
        return label;
    }
}
