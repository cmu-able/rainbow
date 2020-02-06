package org.acmestudio.rainbow.ui.part;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Text;

public class ValuePromptDialog extends Dialog {
    private Text m_text;
    private String m_textValue;

    /**
     * Create the dialog.
     * @param parentShell
     */
    public ValuePromptDialog(Shell parentShell) {
        super(parentShell);
    }

    /**
     * Create contents of the dialog.
     * @param parent
     */
    @Override
    protected Control createDialogArea (Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new FormLayout());
        
        Label lblEnterValue = new Label(container, SWT.NONE);
        FormData fd_lblEnterValue = new FormData();
        fd_lblEnterValue.top = new FormAttachment(0, 10);
        fd_lblEnterValue.left = new FormAttachment(0, 10);
        lblEnterValue.setLayoutData(fd_lblEnterValue);
        lblEnterValue.setText("Enter value:");
        
        m_text = new Text(container, SWT.BORDER);
        FormData fd_text = new FormData();
        fd_text.top = new FormAttachment(0, 10);
        fd_text.left = new FormAttachment(lblEnterValue, 6);
        fd_text.right = new FormAttachment(100, -10);
        m_text.setLayoutData(fd_text);

        return container;
    }

    /**
     * Create contents of the button bar.
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar (Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize () {
        return new Point(450, 115);
    }
    
    @Override
    protected void okPressed () {
        m_textValue = m_text.getText();
        super.okPressed();
    }

    public String getValue () {
        return m_textValue;
    }
}
