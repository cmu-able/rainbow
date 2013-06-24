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
import org.junit.Test;
import org.sa.rainbow.RainbowDelegate;
import org.sa.rainbow.RainbowMaster;

import auxtestlib.CommandRunner.ProcessInterface;
import auxtestlib.JavaLauncher;

public class ESEBConnectionAndLifecycleSeparateVMTest {

    protected void configureTestProperties () throws IOException {
        File basePath = new File (System.getProperty ("user.dir"));
        File testMasterDir = new File (basePath, "src/test/resources/RainbowTest/eseb");
        System.setProperty ("user.dir", testMasterDir.getCanonicalPath ());
    }

    @Test
    public void testReceivedConfigurationInfo () throws Exception {
        String current = System.getProperty ("user.dir");
        configureTestProperties ();
        RainbowMaster master = new RainbowMaster ();
        try {
            master.start ();

            JavaLauncher launcher = new JavaLauncher ();
            ProcessInterface pi = launcher
                    .launchJavaAsync (this.getClass ().getCanonicalName (), new File (current),
                            Arrays.asList (""), 10000);

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

    public static void main (String[] args) throws IOException, InterruptedException {
        new ESEBConnectionAndLifecycleSeparateVMTest ().startDelegate ();
        Thread.sleep (10000);
    }

}
