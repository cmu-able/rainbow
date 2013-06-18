package org.sa.rainbow.core.test;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.junit.Test;
import org.sa.rainbow.RainbowDelegate;
import org.sa.rainbow.RainbowMaster;

public class RainbowTest {


    @Test
    public void testLocalHeartbeatSetup () throws Exception {
        BasicConfigurator.configure ();

        // Heartbeat message appears in log file, so use this as a way to test
        final ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        WriterAppender wa = new WriterAppender (new SimpleLayout (), baos);
        Logger.getRootLogger ().addAppender (wa);
        Logger.getRootLogger ().setLevel (Level.ALL);

        File basePath = new File (System.getProperty ("user.dir"));
        File testMasterDir = new File (basePath, "src/test/resources/RainbowTest/master");
        System.setProperty ("user.dir", testMasterDir.getCanonicalPath ());

        // Start a delegate and a master
        RainbowMaster master = new RainbowMaster ();
        master.start ();
        RainbowDelegate delegate = new RainbowDelegate ();
        delegate.start ();

        // Wait for 7 seconds before checking if heartbeat was recorded
        Thread.sleep (7000);
        String logMsg = baos.toString ();
        assertTrue (logMsg.contains ("Received heartbeat from known delegate: " + delegate.getId ()));

        master.stop ();
        delegate.stop ();
    }


}
