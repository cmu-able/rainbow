package org.sa.rainbow.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

public class OracleStatusPanel extends JPanel {

    private JTextArea    m_textArea;

    static final Pattern NO_HEARTBEAT = Pattern.compile ("\\[ERROR\\]: No Heartbeat from (.*)@(.*)");
    static final Pattern HEARTBEAT    = Pattern.compile ("\\[INFO\\]: Heartbeat from (.*)@(.*)");

    Map<String, JLabel>  labels       = new HashMap<> ();

    private JPanel       m_statusPane;

    /**
     * Create the panel.
     */
    public OracleStatusPanel (Color color) {
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
        Border border = BorderFactory.createMatteBorder (RainbowGUI.PANEL_BORDER, RainbowGUI.PANEL_BORDER,
                RainbowGUI.PANEL_BORDER, RainbowGUI.PANEL_BORDER, color);

        add (sp);
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
        JLabel label = labels.get (did);
        if (label == null) {
            label = new JLabel (location);
            label.setOpaque (true);
            labels.put (did, label);
            m_statusPane.add (label);
            m_statusPane.validate ();
        }
        label.setVisible (true);
        return label;
    }
}
