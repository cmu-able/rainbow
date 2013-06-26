package org.sa.rainbow.core.test;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sa.rainbow.RainbowDelegate;
import org.sa.rainbow.RainbowMaster;

import auxtestlib.CommandRunner.ProcessInterface;
import auxtestlib.JavaLauncher;

/**
 * Tests various lifecycle methods for masters and delegates by starting delegates in separate VMs and ensuring that
 * connections happen.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public class ESEBConnectionAndLifecycleSeparateVMTest {

    /**
     * The directory that the test was started in, so that it can be used subsequently to start the delegate in the same
     * place
     **/
    private static String s_currentDirectory;

    /** Sets the user path to be the right place for finding the properties file for this test **/
    protected void configureTestProperties () throws IOException {
        File basePath = new File (s_currentDirectory);
        File testMasterDir = new File (basePath, "src/test/resources/RainbowTest/eseb");
        System.setProperty ("user.dir", testMasterDir.getCanonicalPath ());
    }

    @BeforeClass
    public static void setCurrentDirectory () {
        s_currentDirectory = System.getProperty ("user.dir");
    }

    /**
     * Tests that the delegate receives configuration information after starting and connecting to the delegate
     * 
     * @throws Exception
     */
    @Test
    public void testReceivedConfigurationInfo () throws Exception {
        configureTestProperties ();
        RainbowMaster master = new RainbowMaster ();
        try {
            master.start ();

            ProcessInterface pi = launchDelegate (s_currentDirectory);

            Thread.sleep (5000);
            String logMsg = pi.getOutputText ();
            Pattern configPattern = Pattern.compile ("RD-.*: Received configuration information");
            Matcher m = configPattern.matcher (logMsg);
            assertTrue (m.find ());
        }
        finally {
            master.terminate ();
        }
    }

    /** Launches teh delegate in a new VM **/
    private ProcessInterface launchDelegate (String current) throws IOException {
        JavaLauncher launcher = new JavaLauncher ();
        ProcessInterface pi = launcher.launchJavaAsync (this.getClass ().getCanonicalName (), new File (current),
                Arrays.asList (""), 10000);
        return pi;
    }

    /**
     * Tests that the master receives a heartbeat after the delegate has connected
     * 
     * @throws Exception
     */
    @Test
    public void testHeartbeatSetup () throws Exception {

        // Heartbeat message appears in log file, so use this as a way to test
        final ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        WriterAppender wa = new WriterAppender (new SimpleLayout (), baos);
        Logger.getRootLogger ().addAppender (wa);
        Logger.getRootLogger ().setLevel (Level.ALL);

        configureTestProperties ();

        // Start a delegate and a master
        RainbowMaster master = new RainbowMaster ();
        master.start ();

        ProcessInterface pi = launchDelegate (s_currentDirectory);

        // Wait for 7 seconds before checking if heartbeat was recorded
        Thread.sleep (7000);
        String logMsg = baos.toString ();
        assertTrue (logMsg.contains ("Received heartbeat from known delegate: "));

        master.terminate ();

    }

    protected void startDelegate () throws IOException {
        BasicConfigurator.configure ();
        configureTestProperties ();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        WriterAppender wa = new WriterAppender (new SimpleLayout (), baos);
        Logger.getRootLogger ().addAppender (wa);
        Logger.getRootLogger ().setLevel (Level.ALL);
        RainbowDelegate delegate = new RainbowDelegate ();
        delegate.start ();

    }

    /**
     * Called by launchDelegate to start the delegate in a new VM
     * 
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main (String[] args) throws IOException, InterruptedException {
        setCurrentDirectory ();
        new ESEBConnectionAndLifecycleSeparateVMTest ().startDelegate ();
        Thread.sleep (10000);
    }

}
