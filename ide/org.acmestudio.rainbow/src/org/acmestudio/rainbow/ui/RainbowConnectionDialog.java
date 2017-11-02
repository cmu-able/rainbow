package org.acmestudio.rainbow.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.acmestudio.ui.util.Listeners;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.sa.rainbow.core.models.IModelInstanceProvider;
import org.sa.rainbow.core.ports.eseb.ESEBModelChangeBusSubscriptionPort;

public class RainbowConnectionDialog extends TitleAreaDialog {
    private Text m_hostTxt;
    private Text m_portTxt;
    private Text m_infoTxt;
    private Button m_btnConnect;
    protected ESEBModelChangeBusSubscriptionPort m_connectionPort;
    protected IModelInstanceProvider             m_modelProvider;
    private String                               m_host;
    private short                                m_port;

    /**
     * Create the dialog.
     * @param parentShell
     */
    public RainbowConnectionDialog(Shell parentShell) {
        super(parentShell);
    }

    /**
     * Create contents of the dialog.
     * @param parent
     */
    @Override
    protected Control createDialogArea (Composite parent) {
        setMessage("Enter the host and port of the running Rainbow communication bus.");
        setTitle("Connect to Rainbow");
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayout(new FormLayout());
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        Label lblHost = new Label(container, SWT.NONE);
        FormData fd_lblHost = new FormData();
        fd_lblHost.top = new FormAttachment(0, 10);
        fd_lblHost.left = new FormAttachment(0, 10);
        lblHost.setLayoutData(fd_lblHost);
        lblHost.setText("Host:");

        Label lblPort = new Label(container, SWT.NONE);
        FormData fd_lblPort = new FormData();
        fd_lblPort.top = new FormAttachment(lblHost, 6);
        fd_lblPort.left = new FormAttachment(0, 10);
        lblPort.setLayoutData(fd_lblPort);
        lblPort.setText("Port:");

        m_hostTxt = new Text(container, SWT.BORDER);
        FormData fd_text = new FormData();
        fd_text.right = new FormAttachment(lblHost, 256, SWT.RIGHT);
        fd_text.top = new FormAttachment(0, 4);
        fd_text.left = new FormAttachment(lblHost, 20);
        m_hostTxt.setLayoutData(fd_text);
        ModifyListener modifyListener = new ModifyListener() {

            @Override
            public void modifyText (ModifyEvent e) {
                verifyInput();
            }
        };
        m_hostTxt.addModifyListener(modifyListener);

        m_portTxt = new Text(container, SWT.BORDER);
        FormData fd_text_1 = new FormData();
        fd_text_1.right = new FormAttachment(m_hostTxt, 0, SWT.RIGHT);
        fd_text_1.top = new FormAttachment(m_hostTxt, 6);
        fd_text_1.left = new FormAttachment(m_hostTxt, 0, SWT.LEFT);
        m_portTxt.setLayoutData(fd_text_1);
        m_portTxt.addModifyListener(modifyListener);
        m_portTxt.addVerifyListener(new Listeners.NumberVerifier(0, Short.MAX_VALUE));

        m_btnConnect = new Button(container, SWT.NONE);
        FormData fd_btnConnect = new FormData();
        fd_btnConnect.bottom = new FormAttachment(m_portTxt, 0, SWT.BOTTOM);
        fd_btnConnect.left = new FormAttachment(m_portTxt, 24);
        m_btnConnect.setLayoutData(fd_btnConnect);
        m_btnConnect.setText("Connect");
        m_btnConnect.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected (SelectionEvent e) {
                try {
                    m_connectionPort = new ESEBModelChangeBusSubscriptionPort(m_hostTxt.getText(), Short.parseShort(m_portTxt
                            .getText()), m_modelProvider);
                    getButton(IDialogConstants.OK_ID).setEnabled(true);
                }
                catch (NumberFormatException | IOException e1) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    e1.printStackTrace(new PrintStream(baos));
                    m_infoTxt.setText("Could not connect to Rainbow:\n" + baos.toString());
                }
            }
        });

        m_infoTxt = new Text(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
        m_infoTxt.setEditable(false);
        FormData fd_text_2 = new FormData();
        fd_text_2.top = new FormAttachment(m_portTxt, 6);
        fd_text_2.left = new FormAttachment(0, 10);
        fd_text_2.right = new FormAttachment(0, 434);
        fd_text_2.bottom = new FormAttachment(100, -10);
        m_infoTxt.setLayoutData(fd_text_2);

        m_btnConnect.setEnabled(false);
        return area;

    }

    protected void verifyInput () {
        if (!m_portTxt.getText().trim().isEmpty() && !m_hostTxt.getText().trim().isEmpty()) {
            m_btnConnect.setEnabled (true);
        }
    }

    /**
     * Create contents of the button bar.
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar (Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize () {
        return new Point(450, 300);
    }

    @Override
    protected void okPressed () {
        m_host = m_hostTxt.getText();
        m_port = Short.parseShort(m_portTxt.getText());
        super.okPressed();
    }

    public String getHost () {
        return m_host;
    }

    public short getPort () {
        return m_port;
    }
}
