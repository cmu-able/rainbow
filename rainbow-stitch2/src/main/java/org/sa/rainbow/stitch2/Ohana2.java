package org.sa.rainbow.stitch2;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Nullable;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.xpath.XPath;
import org.sa.rainbow.stitch2.core.*;
import org.sa.rainbow.stitch2.error.DummyStitchProblemHandler;
import org.sa.rainbow.stitch2.error.IStitchProblem;
import org.sa.rainbow.stitch2.error.StitchProblem;
import org.sa.rainbow.stitch2.model.ModelOperator;
import org.sa.rainbow.stitch2.parser.StitchLexer;
import org.sa.rainbow.stitch2.parser.StitchParser;
import org.sa.rainbow.stitch2.util.ExecutionHistoryData;
import org.sa.rainbow.stitch2.visitor.IStitchBehavior;
import org.sa.rainbow.stitch2.visitor.Stitch;
import org.sa.rainbow.stitch2.visitor.StitchBeginEndVisitor;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Created by schmerl on 9/28/2016.
 * <p/>
 * Library manager of a Library of Stitch scripts. This is s singleton object.
 */
public class Ohana2 {
    public static boolean m_execFromMain = false;
    public static Ohana2  m_instance     = null;
    public static boolean m_isDisposed   = false;

    protected static boolean reportProblems (DummyStitchProblemHandler stitchProblemHandler) {
        Collection<IStitchProblem> problem = stitchProblemHandler.getProblems ();
        boolean reported = false;
        for (IStitchProblem p :
                problem) {
            StringBuffer out = new StringBuffer ();
            switch (p.getSeverity ()) {
                case IStitchProblem.ERROR:
                    out.append ("ERROR: ");
                    reported = true;
                    break;
                case IStitchProblem.FATAL:
                    out.append ("FATAL: ");
                    reported = true;
                    break;
                case IStitchProblem.WARNING:
                    out.append ("WARNING: ");
                    break;
                case IStitchProblem.UNKNOWN:
                    out.append ("UNKNOWN PROBLEM: ");
                    reported = true;
                    break;
            }
            out.append ("Line: " + p.getLine ());
            out.append (", Column: " + p.getColumn ());
            out.append (": " + p.getMessage ());
            System.err.println (out.toString ());
        }
        stitchProblemHandler.clearProblems ();
        return reported;
    }

    public static Ohana2 instance () {
        if (m_instance == null)
            m_instance = new Ohana2 ();
        return m_instance;
    }

    public static boolean isDisposed () {
        return m_isDisposed;
    }

    public static void cleanup () {
        m_instance = null;
        m_isDisposed = false;
    }

    private IScope                            m_rootScope                  = null;
    private ModelOperator                     m_modelOp                    = null;
    private Map<String, Set<Stitch>>          m_stitches                   = null;
    private Stitch                            m_emptyExprStitch            = null;
    private File                              m_tacticExecutionHistoryFile = null;
    private Map<String, ExecutionHistoryData> m_tacticHistoryMap           = null;
    private int                               m_updateCnt                  = 0;
    private boolean                           m_typecheckStrategies        = true;

    private Ohana2 () {
        m_rootScope = new ScopedEntity (null, "Ohana2 Stitch Root Scope", Stitch.NULL_STITCH);
        Stitch.NULL_STITCH.script = new StitchScript (m_rootScope, "Ohana Stitch Root Script", Stitch
                .NULL_STITCH);
        m_stitches = new HashMap<> ();
        ConditionTimer.instance ();
    }

    public void dispose () {
        ConditionTimer.instance ().end ();
        m_modelOp = null;
        m_rootScope = null;

        for (Set<Stitch> sL : m_stitches.values ()) {
            for (Stitch stitch : sL) {
                stitch.dispose ();
            }
            sL.clear ();
        }
        m_stitches.clear ();
        m_stitches = null;
        Ohana2.m_isDisposed = true;
    }

    public IScope getRootScope () {
        return m_rootScope;
    }

    public List<Stitch> listStitches () {
        List<Stitch> ret = new ArrayList<> (m_stitches.size ());
        for (Set<Stitch> sL : m_stitches.values ()) {
            ret.add (sL.iterator ().next ());
        }
        return ret;
    }

    public Stitch findStitch (String key) {
        final Set<Stitch> stitches = m_stitches.get (key);
        if (stitches == null || stitches.isEmpty ())
            return null;
        return stitches.iterator ().next ();
    }

    public void releaseStitch (Stitch stitch) {
        synchronized (Ohana2.class) {
            stitch.markExecuting (false);
        }
    }

    public Stitch findFreeStitch (Stitch stitch) throws IOException {
        synchronized (Ohana2.class) {
            Stitch s = Stitch.newInstance (stitch.path, stitch.stitchProblemHandler.clone (), true);
            instance ().parseFile (s);
            return s;
        }
    }

    public Stitch storeStitch (String key, Stitch m) {
        Set<Stitch> stitches = m_stitches.get (key);
        if (stitches == null) {
            stitches = new HashSet<> ();
            m_stitches.put (key, stitches);
        }
        stitches.add (m);
        return m;
    }

    public Stitch removeStitch (String key) {
        final Set<Stitch> stitches = m_stitches.get (key);
        if (stitches == null || stitches.isEmpty ()) {
            return null;
        }

        final Stitch s = stitches.iterator ().next ();
        m_stitches.remove (key);
        return s;
    }

    public ArrayList<ArrayList<ParseTree>> parseFile (Stitch stitch) throws IOException {
        FileInputStream fin = new FileInputStream (stitch.path);
        return parseInput (fin, stitch);
    }

    public ArrayList<ArrayList<ParseTree>> parseInput (InputStream input, final Stitch stitch) throws IOException {
        CharStream i = new ANTLRInputStream (input);

        StitchLexer lexer = new StitchLexer (i);
        lexer.setTokenFactory (new CommonTokenFactory ());
        TokenStream tokens = new CommonTokenStream (lexer);
        StitchParser parser = new StitchParser (tokens);

        ANTLRErrorListener errReporter = new BaseErrorListener () {
            @Override
            public void syntaxError (@NotNull Recognizer<?, ?> recognizer, @Nullable Object offendingSymbol, int
                    line, int charPositionInLine, @NotNull String msg, @Nullable RecognitionException e) {
                StitchProblem problem = new StitchProblem (new org.sa.rainbow.stitch2.error.RecognitionException (msg,
                                                                                                                  null, line, charPositionInLine),
                                                           StitchProblem.ERROR);
                stitch.stitchProblemHandler.setProblem (problem);
            }
        };
        lexer.addErrorListener (errReporter);
        parser.addErrorListener (errReporter);
        StitchParser.ScriptContext script = parser.script ();
        String tacticPath = "/script/tactic";
        final Collection<ParseTree> definedTactics = XPath.findAll (script, tacticPath, parser);
//        if (this.m_typecheckStrategies) {
//            IScope typecheckingScope = new ScopedEntity (null, "Typechecking root scope", new StitchState (Stitch
//
// .NULL_STITCH));
//            IStitchBehavior tcb = stitchState.getBehavior (Stitch.TYPECHECKER_PASS);
//
//            StitchBeginEndVisitor walker = new StitchBeginEndVisitor (tcb, typecheckingScope);
//            tcb.stitchState ().pushScope (typecheckingScope);
//            walker.visit (script);
//        }
        IStitchBehavior sb = stitch.getBehavior (Stitch.SCOPER_PASS);
        sb.stitch ().setScope (m_rootScope);
        StitchBeginEndVisitor walker = new StitchBeginEndVisitor (sb, m_rootScope);
        walker.visit (script);

//        if (m_typecheckStrategies) {
//            IStitchBehavior tcb = stitch.getBehavior (Stitch.TYPECHECKER_PASS);
//            StitchBeginEndVisitor tcWalker = new StitchBeginEndVisitor (tcb, m_rootScope);
//            tcWalker.visit (script);
//        }

        ArrayList<ArrayList<ParseTree>> al = new ArrayList<> ();
        ArrayList<ParseTree> alRoot = new ArrayList<> ();
        alRoot.add (script);
        al.add (alRoot);
        al.add (new ArrayList<ParseTree> (definedTactics));
        return al;
    }

    public Expression parseExpressionString (String exprStr) {
        if (m_emptyExprStitch == null) {
            URL url = Ohana2.class.getResource ("emptyExprScript.s");
            m_emptyExprStitch = Stitch.newInstance (url.getPath (), new DummyStitchProblemHandler ());
            try {
                instance ().parseInput (Ohana2.class.getResourceAsStream ("emptyExprScript.s"), m_emptyExprStitch);
            } catch (IOException e) {
                e.printStackTrace ();
            }
        }

        Statement stmt = null;
        final List<IScope> children = m_emptyExprStitch.script.getChildren ();
        synchronized (children) {
            for (IScope s : children) {
                if (s instanceof Statement) {
                    stmt = (Statement) s;
                    break;
                }
            }
        }

        if (stmt == null) return null;

        // Parse expression using the statement scope
        stmt.expressions ().clear ();
        m_emptyExprStitch.pushScope (stmt);
        StitchLexer lexer = new StitchLexer (new UnbufferedCharStream (new StringReader (exprStr)));
        lexer.setTokenFactory (new CommonTokenFactory ());
        TokenStream tokens = new UnbufferedTokenStream<CommonToken> (lexer);
        StitchParser parser = new StitchParser (tokens);
        ANTLRErrorListener errReporter = new BaseErrorListener () {
            @Override
            public void syntaxError (@NotNull Recognizer<?, ?> recognizer, @Nullable Object offendingSymbol, int
                    line, int charPositionInLine, @NotNull String msg, @Nullable RecognitionException e) {
                StitchProblem problem = new StitchProblem (new org.sa.rainbow.stitch2.error.RecognitionException (msg,
                                                                                                                  null, line, charPositionInLine),
                                                           StitchProblem.ERROR);
                m_emptyExprStitch.stitchProblemHandler.setProblem (problem);
            }
        };
        lexer.addErrorListener (errReporter);
        parser.addErrorListener (errReporter);
        final StitchParser.ExpressionContext expression = parser.expression ();
        IStitchBehavior sb = m_emptyExprStitch.getBehavior (Stitch.SCOPER_PASS);
        StitchBeginEndVisitor walker = new StitchBeginEndVisitor (sb, stmt);
        walker.visit (expression);

        return stmt.expressions ().get (0);
    }

    public ModelOperator modelOperator () {
        if (m_modelOp == null) return ModelOperator.NO_OP;
        return m_modelOp;
    }

    public void setModelOperator (ModelOperator op) {
        m_modelOp = op;
    }
}
