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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class ElementInformationDialog extends JDialog {

    private final JPanel m_contentPanel = new JPanel ();
    private JTextArea    m_textArea;

    /**
     * Launch the application.
     */
    public static void main (String[] args) {
        try {
            ElementInformationDialog dialog = new ElementInformationDialog ();
            dialog.setDefaultCloseOperation (JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible (true);
        }
        catch (Exception e) {
            e.printStackTrace ();
        }
    }

    /**
     * Create the dialog.
     */
    public ElementInformationDialog () {
        setTitle ("Information about:");
        setBounds (100, 100, 450, 300);
        getContentPane ().setLayout (new BorderLayout ());
        m_contentPanel.setBorder (new EmptyBorder (5, 5, 5, 5));
        getContentPane ().add (m_contentPanel, BorderLayout.CENTER);
        m_contentPanel.setLayout (new GridLayout (0, 1, 0, 0));
        {
            m_textArea = new JTextArea ();
            m_textArea.setEditable (false);
            JScrollPane scrollPane = new JScrollPane (m_textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            m_contentPanel.add (scrollPane);
        }
        {
            JPanel buttonPane = new JPanel ();
            buttonPane.setLayout (new FlowLayout (FlowLayout.RIGHT));
            getContentPane ().add (buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton ("OK");
                okButton.addActionListener (new ActionListener () {

                    @Override
                    public void actionPerformed (ActionEvent e) {
                        dispose ();
                    }
                });
                buttonPane.add (okButton);
                getRootPane ().setDefaultButton (okButton);
            }
        }
        setDefaultCloseOperation (DISPOSE_ON_CLOSE);

    }

    public void setInformation (String string) {
        m_textArea.setText (string);
    }

}
