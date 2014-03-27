package org.acmestudio.rainbow.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;

public class ShowSourceDialog extends Dialog {

    private final Browser m_swtBrowser;
    private StyledText m_text;

    /**
     * Create the dialog.
     * @param parentShell
     * @param swtBrowser 
     */
    public ShowSourceDialog(Shell parentShell, Browser swtBrowser) {
        super(parentShell);
        setShellStyle(SWT.RESIZE);
        m_swtBrowser = swtBrowser;
    }

    /**
     * Create contents of the dialog.
     * @param parent
     */
    @Override
    protected Control createDialogArea (Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new FillLayout(SWT.HORIZONTAL));
        
        m_text = new StyledText(container, SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
        m_text.setEditable(false);
        String text = m_swtBrowser.getText();
        m_text.setText(text);
        return container;
    }

    /**
     * Create contents of the button bar.
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar (Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize () {
        return new Point(450, 300);
    }

}
