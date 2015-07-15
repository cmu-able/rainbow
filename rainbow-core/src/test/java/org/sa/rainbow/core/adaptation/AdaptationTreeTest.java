package org.sa.rainbow.core.adaptation;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;

public class AdaptationTreeTest extends DefaultTCase {

    private final class TestAdaptationVisitor extends DefaultAdaptationExecutorVisitor<IEvaluable> {
        private TestAdaptationVisitor (AdaptationTree<IEvaluable> adt, ThreadGroup tg, CountDownLatch done) {
            super (adt, tg, "", done);
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
            numbers.add (m_number);
            return m_return;
        }

        @Override
        public long estimateAvgTimeCost () {
            return 0;
        }

    }

    List<Integer> numbers = new LinkedList ();

    @Before
    public void setUp () throws Exception {
        numbers.clear ();
    }

    @After
    public void tearDown () throws Exception {
    }

    @Test
    public void testSequence () throws InterruptedException {
        AdaptationTree root = new AdaptationTree<IEvaluable> (AdaptationExecutionOperatorT.SEQUENCE);
        root.addLeaf (new ExecutableTest (1));
        root.addLeaf (new ExecutableTest (2));
        root.addLeaf (new ExecutableTest (3));

        CountDownLatch countdownLatch = new CountDownLatch (1);

        DefaultAdaptationExecutorVisitor<IEvaluable> visitor = new DefaultAdaptationExecutorVisitor<IEvaluable> (root,
                new ThreadGroup ("execution"), "", countdownLatch) {

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
        AdaptationTree root = new AdaptationTree<IEvaluable> (AdaptationExecutionOperatorT.SEQUENCE_STOP_FAILURE);
        root.addLeaf (new ExecutableTest (1));
        root.addLeaf (new ExecutableTest (2, false));
        root.addLeaf (new ExecutableTest (3));

        CountDownLatch countdownLatch = new CountDownLatch (1);

        DefaultAdaptationExecutorVisitor<IEvaluable> visitor = new TestAdaptationVisitor (root, new ThreadGroup (
                "execution"), countdownLatch);
        visitor.start ();
        countdownLatch.await (5, TimeUnit.SECONDS);
        assertTrue (numbers.size () == 2);
        assertTrue (numbers.get (0) == 1);
        assertTrue (numbers.get (1) == 2);
    }

    @Test
    public void testSequenceSuccess () throws InterruptedException {
        AdaptationTree root = new AdaptationTree<IEvaluable> (AdaptationExecutionOperatorT.SEQUENCE_STOP_SUCCESS);
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
        AdaptationTree root = new AdaptationTree<IEvaluable> (AdaptationExecutionOperatorT.PARALLEL);
        int number = 20;
        for (int i = 0; i < number; i++) {
            root.addLeaf (new ExecutableTest (i));
        }

        CountDownLatch countdownLatch = new CountDownLatch (1);

        DefaultAdaptationExecutorVisitor<IEvaluable> visitor = new TestAdaptationVisitor (root, new ThreadGroup (
                "execution"), countdownLatch);
        visitor.start ();
        countdownLatch.await (5, TimeUnit.SECONDS);
        assertTrue (numbers.size () == number);
    }

}
