package org.sa.rainbow.core.ports;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;

import org.sa.rainbow.core.RainbowDelegate;

public abstract class AbstractDelegateConnectionPort implements IRainbowMasterConnectionPort {

    protected RainbowDelegate m_delegate;

    public AbstractDelegateConnectionPort (RainbowDelegate delegate) {
        m_delegate = delegate;
    }

    public void report (ReportType type, String msg) {
        report (m_delegate.getId (), type, msg);
    }
    
    public void report (ReportType type, String msg, Throwable t) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        PrintStream ps = new PrintStream (baos);
        t.printStackTrace (ps);
        ps.close ();
        report (m_delegate.getId (), type, MessageFormat.format ("{0}.\nException: {1}\n{2}", msg, t.getMessage (), baos.toString ()));
    }

}
