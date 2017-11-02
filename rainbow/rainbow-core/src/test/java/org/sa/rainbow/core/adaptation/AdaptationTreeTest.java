package org.sa.rainbow.core.adaptation;

import auxtestlib.DefaultTCase;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.ports.IRainbowReportingPort;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AdaptationTreeTest extends DefaultTCase {

    private static final class TestCaseReportingPort implements IRainbowReportingPort {
        Logger L = Logger.getLogger (TestCaseReportingPort.class);

        @Override
        public void dispose () {
        }

        @Override
        public void warn (RainbowComponentT type, String msg) {
            L.warn (composeMessage (type, msg));
        }

        String composeMessage (RainbowComponentT type, String msg) {
            return MessageFormat.format ("{0}: {1}", type.name (), msg);
        }

        @Override
        public void warn (RainbowComponentT type, String msg, Throwable e) {
            L.warn (composeMessage (type, msg), e);
        }

        @Override
        public void warn (RainbowComponentT type, String msg, Logger logger) {
            logger.warn (composeMessage (type, msg));
        }

        @Override
        public void warn (RainbowComponentT type, String msg, Throwable e, Logger logger) {
            logger.warn (composeMessage (type, msg), e);

        }

        @Override
        public void trace (RainbowComponentT type, String msg) {
            L.trace (composeMessage (type, msg));
        }

        @Override
        public void info (RainbowComponentT type, String msg) {
            L.info (composeMessage (type, msg));

        }

        @Override
        public void info (RainbowComponentT type, String msg, Logger logger) {
            logger.info (composeMessage (type, msg));

        }

        @Override
        public void fatal (RainbowComponentT type, String msg) {
            L.fatal (composeMessage (type, msg));
        }

        @Override
        public void fatal (RainbowComponentT type, String msg, Throwable e) {
            L.fatal (composeMessage (type, msg), e);
        }

        @Override
        public void fatal (RainbowComponentT type, String msg, Logger logger) {
            logger.fatal (composeMessage (type, msg));
        }

        @Override
        public void fatal (RainbowComponentT type, String msg, Throwable e, Logger logger) {
            logger.fatal (composeMessage (type, msg), e);
        }

        @Override
        public void error (RainbowComponentT type, String msg) {
            L.error (composeMessage (type, msg));

        }

        @Override
        public void error (RainbowComponentT type, String msg, Throwable e) {
            L.error (composeMessage (type, msg), e);
        }

        @Override
        public void error (RainbowComponentT type, String msg, Logger logger) {
            logger.error (composeMessage (type, msg));
        }

        @Override
        public void error (RainbowComponentT type, String msg, Throwable e, Logger logger) {
            logger.error (composeMessage (type, msg), e);
        }
    }

    private final class TestAdaptationVisitor extends DefaultAdaptationExecutorVisitor<IEvaluable> {

        private TestAdaptationVisitor (AdaptationTree<IEvaluable> adt, ThreadGroup tg, CountDownLatch done) {
            super (adt, tg, "", done, new TestCaseReportingPort ());
        }

        @Override
        protected boolean evaluate (IEvaluable adaptation) {
            return (Boolean )adaptation.evaluate (null);
        }

        @Override
        protected DefaultAdaptationExecutorVisitor<IEvaluable> spawnNewExecutorForTree (AdaptationTree<IEvaluable> adt,
                ThreadGroup g,
                CountDownLatch doneSignal) {
            return new TestAdaptationVisitor (adt, new ThreadGroup ("parallel"), doneSignal);
        }
    }

    public class ExecutableTest implements IEvaluable {

        private int     m_number;
        private boolean m_return;

        public ExecutableTest (int number) {
            m_number = number;
            m_return = true;
        }

        public ExecutableTest (int number, boolean ret) {
            m_number = number;
            m_return = ret;
        }

        @Override
        public Object evaluate (Object[] argsIn) {
            synchronized (numbers) {
                numbers.add (m_number);
            }
            return m_return;
        }

        @Override
        public long estimateAvgTimeCost () {
            return 0;
        }

    }

    private List<Integer> numbers = new LinkedList ();

    @Before
    public void setUp () throws Exception {
        numbers.clear ();
    }

    @After
    public void tearDown () throws Exception {
    }

    @Test
    public void testSequence () throws InterruptedException {
        AdaptationTree root = new AdaptationTree<> (AdaptationExecutionOperatorT.SEQUENCE);
        root.addLeaf (new ExecutableTest (1));
        root.addLeaf (new ExecutableTest (2));
        root.addLeaf (new ExecutableTest (3));

        CountDownLatch countdownLatch = new CountDownLatch (1);

        DefaultAdaptationExecutorVisitor<IEvaluable> visitor = new DefaultAdaptationExecutorVisitor<IEvaluable> (root,
                                                                                                                 new ThreadGroup ("execution"), "", countdownLatch, new TestCaseReportingPort ()) {

            @Override
            protected boolean evaluate (IEvaluable adaptation) {
                return (Boolean )adaptation.evaluate (null);
            }

            @Override
            protected DefaultAdaptationExecutorVisitor<IEvaluable>
            spawnNewExecutorForTree (AdaptationTree<IEvaluable> adt, ThreadGroup g, CountDownLatch doneSignal) {
                // TODO Auto-generated method stub
                return null;
            }

        };
        visitor.start ();
        countdownLatch.await (5, TimeUnit.SECONDS);
        assertTrue (numbers.size () == 3);
        assertTrue (numbers.get (0) == 1);
        assertTrue (numbers.get (1) == 2);
        assertTrue (numbers.get (2) == 3);
    }

    @Test
    public void testSequenceFail () throws InterruptedException {
        AdaptationTree root = new AdaptationTree<> (AdaptationExecutionOperatorT.SEQUENCE_STOP_FAILURE);
        root.addLeaf (new ExecutableTest (1));
        root.addLeaf (new ExecutableTest (2, false));
        root.addLeaf (new ExecutableTest (3));

        CountDownLatch countdownLatch = new CountDownLatch (1);

        DefaultAdaptationExecutorVisitor<IEvaluable> visitor = new TestAdaptationVisitor (root,
                                                                                          new ThreadGroup ("execution"), countdownLatch);
        visitor.start ();
        countdownLatch.await (5, TimeUnit.SECONDS);
        assertTrue (numbers.size () == 2);
        assertTrue (numbers.get (0) == 1);
        assertTrue (numbers.get (1) == 2);
    }

    @Test
    public void testSequenceSuccess () throws InterruptedException {
        AdaptationTree root = new AdaptationTree<> (AdaptationExecutionOperatorT.SEQUENCE_STOP_SUCCESS);
        root.addLeaf (new ExecutableTest (1));
        root.addLeaf (new ExecutableTest (2, false));
        root.addLeaf (new ExecutableTest (3));

        CountDownLatch countdownLatch = new CountDownLatch (1);

        DefaultAdaptationExecutorVisitor<IEvaluable> visitor = new TestAdaptationVisitor (root,
                new ThreadGroup ("test"), countdownLatch);

        visitor.start ();
        countdownLatch.await (5, TimeUnit.SECONDS);
        assertTrue (numbers.size () == 1);
        assertTrue (numbers.get (0) == 1);
    }

    @Test
    public void testParallel () throws InterruptedException {
        AdaptationTree root = new AdaptationTree<> (AdaptationExecutionOperatorT.PARALLEL);
        int number = 20;
        for (int i = 0; i < number; i++) {
            root.addLeaf (new ExecutableTest (i));
        }

        CountDownLatch countdownLatch = new CountDownLatch (1);

        DefaultAdaptationExecutorVisitor<IEvaluable> visitor = new TestAdaptationVisitor (root,
                                                                                          new ThreadGroup ("execution"), countdownLatch);
        visitor.start ();
        countdownLatch.await (5, TimeUnit.SECONDS);
        assertTrue (numbers.size () == number);
    }

}
