package org.sa.rainbow.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.junit.Test;

public class UtilTest {

   

    @Test
    public void testEvalTokensReplacements () {
        Properties properties = new Properties ();
        properties.setProperty ("XXX", "testSucceeds");

        assertNull (Util.evalTokens (null, properties));
        assertEquals (Util.evalTokens ("xxx", null), "xxx");

        String testCase = "${XXX}";

        // Replaces one instance
        String t1 = Util.evalTokens (testCase, properties);
        assertEquals (t1, "testSucceeds");

        // Does not replace test case insensitively
        testCase = "${xxx}";
        assertEquals (Util.evalTokens (testCase, properties), "${xxx}");

        // Replaces multiple of the same
        testCase = "${XXX}aaa${XXX}";
        assertEquals (Util.evalTokens (testCase, properties), "testSucceedsaaatestSucceeds");

        // Replaces multiple different
        testCase = "${XXX}/${yYy}";
        properties.setProperty ("yYy", "test2");
        assertEquals (Util.evalTokens (testCase, properties), "testSucceeds/test2");

        testCase = "Leave this alone";
        assertEquals (Util.evalTokens (testCase, properties), "Leave this alone");

    }

    @Test
    public void testEvalTokensBadFormats () {
        Properties props = new Properties ();
        props.setProperty ("XXX", "testSucceeds");
        props.setProperty ("yYy", "123");

        // Mismatched brackets should be the same
        String test = "${XXX";
        assertEquals (Util.evalTokens (test, props), "${XXX");

        test = "${XX${XXX}}";
        assertEquals (Util.evalTokens (test, props), "${XX${XXX}}");

        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        WriterAppender wa = new WriterAppender (new SimpleLayout (), baos);
        Util.LOGGER.addAppender (wa);

        test = "${UNKNOWN}";
        assertEquals (Util.evalTokens (test, props), "${UNKNOWN}");
        String logMsg = baos.toString ();
        assertTrue (logMsg.contains ("Undefined") && logMsg.contains ("ERROR"));
    }

}
