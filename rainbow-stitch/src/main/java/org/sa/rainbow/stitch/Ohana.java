/*
 * Created March 11, 2006
 */
package org.sa.rainbow.stitch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sa.rainbow.stitch.core.ConditionTimer;
import org.sa.rainbow.stitch.core.Expression;
import org.sa.rainbow.stitch.core.IScope;
import org.sa.rainbow.stitch.core.LinesAwareAST;
import org.sa.rainbow.stitch.core.MyInteger;
import org.sa.rainbow.stitch.core.ScopedEntity;
import org.sa.rainbow.stitch.core.Statement;
import org.sa.rainbow.stitch.core.StitchScript;
import org.sa.rainbow.stitch.core.Tactic;
import org.sa.rainbow.stitch.core.Var;
import org.sa.rainbow.stitch.error.DummyStitchProblemHandler;
import org.sa.rainbow.stitch.error.StitchProblem;
import org.sa.rainbow.stitch.model.ModelOperator;
import org.sa.rainbow.stitch.model.ModelRepository;
import org.sa.rainbow.stitch.parser.StitchLexer;
import org.sa.rainbow.stitch.parser.StitchParser;
import org.sa.rainbow.stitch.util.ExecutionHistoryData;
import org.sa.rainbow.stitch.util.Tool;
import org.sa.rainbow.stitch.visitor.Stitch;

import antlr.RecognitionException;
import antlr.collections.AST;
import antlr.debug.misc.ASTFrame;

/**
 * Library manager of a library of Stitch scripts (hence the Ohana "family"
 * reference). This is a singleton object.
 * 
 * @author Shang-Wen Cheng <zensoul@cs.cmu.edu>
 * @author (Modified by) Ali Almossawi <aalossaw@cs.cmu.edu> (July 23, 2006)
 */
public class Ohana {

    public static boolean m_execFromMain = false;

    private static Ohana m_instance = null;
    private static boolean m_isDisposed = false;

    /**
     * @param args
     */
    public static void main(String[] args) {
        m_execFromMain = true;

        DummyStitchProblemHandler stitchProblemHandler = new DummyStitchProblemHandler();
        instance().setModelRepository(new CommandLineModelRepository());
        if (args.length > 0) {
            if (args[0].equals("-e")) { // evaluate next arg as expression
                // string
                if (args.length < 2) { // complain
                    System.err
                    .println("Usage:\tOhana -e \"expression\"\n\t| Ohana <list of stitch scripts>\n\t| Ohana  (parse from input stream)");
                    System.exit (-1);
                }
                // munge expression string
                String exprStr = args[1] + ";"; // append ending semicolon
                exprStr = exprStr.replace("{0}", "_arg_0");
                // now parse and then evaluate the expression
                Expression expr = instance().parseExpressionString(exprStr);
                Var v = new Var();
                v.scope = expr.parent();
                v.name = "_arg_0";
                v.setType("float"); // assume float...
                v.setValue(2);
                expr.addVar(v.name, v);
                Object rv = expr.evaluate(null);
                System.out.println("Got result: " + rv);
            } else { // arguments should be stitch script paths
                for (String s : args) {
                    try {
                        Stitch stitch = Stitch.newInstance(s,
                                stitchProblemHandler);
                        instance().parseFile(stitch);
                        if (reportedProblems (stitchProblemHandler)) {
                            continue;
                        }
                        instance().evaluateScript(stitch);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            Stitch stitch = Stitch.newInstance(null, stitchProblemHandler);
            instance().parseInput(new DataInputStream(System.in), stitch);
            instance().evaluateScript(stitch);
        }
        Collection<StitchProblem> problem = stitchProblemHandler.getProblems();
        reportedProblems(stitchProblemHandler);
        instance().dispose();
    }

    protected static boolean reportedProblems(
            DummyStitchProblemHandler stitchProblemHandler) {
        Collection<StitchProblem> problem = stitchProblemHandler.getProblems();
        boolean reported = false;
        for (StitchProblem p : problem) {
            StringBuffer out = new StringBuffer();
            switch (p.getSeverity()) {
            case StitchProblem.ERROR:
                out.append("ERROR: ");
                reported = true;
                break;
            case StitchProblem.WARNING:
                out.append("WARNING: ");
                break;
            case StitchProblem.FATAL:
                out.append("FATAL ERROR: ");
                reported = true;
                break;
            case StitchProblem.UNKNOWN:
                out.append("UNKNOWN PROBLEM: ");
                reported = true;
                break;
            }
            out.append("Line: " + p.getLine());
            out.append(", ");
            out.append(" Column: " + p.getColumn());
            out.append(": " + p.getMessage());
            System.err.println(out.toString());
        }
        stitchProblemHandler.clearProblems();
        return reported;
    }

    public static Ohana instance() {
        if (m_instance == null) {
            m_instance = new Ohana();
        }
        return m_instance;
    }

    public static boolean isDisposed() {
        return m_isDisposed;
    }

    /**
     * Cleans up the static state of Ohana for additional rounds of runs.
     */
    public static void cleanup() {
        m_instance = null;
        m_isDisposed = false;
    }

    private IScope m_rootScope = null;
    private ModelRepository m_modelRepo = null;
    private ModelOperator m_modelOp = null;
    // Singleton map of script path to Stitch's
    private Map<String, Stitch> m_stitches = null;
    // stitch object used for evaluating standalone expressions
    private Stitch m_emptyExprStitch = null;
    // file reference for storing tactic execution profile
    private File m_tacticExecutionHistoryFile = null;
    // map of tactic to execution duration
    private Map<String, ExecutionHistoryData> m_tacticHistoryMap = null;
    // count of execution duration updates
    private int m_updateCnt = 0;

    private boolean m_typecheckStrategies = true;

    /**
     * Private Constructor for singleton class.
     */
    private Ohana() {
        m_rootScope = new ScopedEntity(null, "Ohana Stitich Root Scope",
                Stitch.NULL_STITCH);
        Stitch.NULL_STITCH.script = new StitchScript(m_rootScope,
                "Ohana Stitch Root Script", Stitch.NULL_STITCH);
        m_stitches = new HashMap<String, Stitch>();
        ConditionTimer.instance(); // initializes ConditionTimer
    }

    public void dispose() {
        ConditionTimer.instance().end();
        // store the history data
        saveExecutionHistoryToFile();
        m_tacticExecutionHistoryFile = null;
        if (m_tacticHistoryMap != null) {
            m_tacticHistoryMap.clear();
            m_tacticHistoryMap = null;
        }
        m_modelRepo = null;
        m_modelOp = null;
        m_rootScope = null;
        // in the end, clean up the stitchs
        for (Stitch stitch : m_stitches.values()) {
            stitch.dispose();
        }
        m_stitches.clear();
        m_stitches = null;

        Ohana.m_isDisposed = true;
    }

    public IScope getRootScope() {
        return m_rootScope;
    }

    /**
     * @return list of stored stitches
     */
    public List<Stitch> listStitches() {
        return new ArrayList<Stitch>(m_stitches.values());
    }

    public Stitch findStitch(String key) {
        return m_stitches.get(key);
    }

    public Stitch storeStitch(String key, Stitch m) {
        return m_stitches.put(key, m);
    }

    public Stitch removeStitch(String key) {
        return m_stitches.remove(key);
    }

    public ArrayList<ArrayList<AST>> parseFile(Stitch stitch)
            throws FileNotFoundException {
        FileInputStream fin = new FileInputStream(stitch.path);
        ArrayList<ArrayList<AST>> a1 = parseInput(fin, stitch);

        return a1;
    }

    public ArrayList<ArrayList<AST>> parseInput(InputStream input, Stitch stitch) {
        StitchLexer lexer = new StitchLexer(input);
        // [From ALI] incorporated stitchProblemHandler into parser
        StitchParser parser = new StitchParser(lexer);
        parser.setStitchProblemHandler(stitch.stitchProblemHandler);
        parser.setASTNodeClass(LinesAwareAST.class.getCanonicalName());

        AST root = null; // ALI: ADDED
        ArrayList<AST> definedTactics = null; // ALI: ADDED

        try {
            parser.setASTNodeClass(LinesAwareAST.class.getName());
            parser.script();
            root = parser.getAST();
            definedTactics = parser.getDefinedTactics();
            if (m_execFromMain) {
                System.out.println(root.toStringList());
                ASTFrame frame = new ASTFrame("Stitch Tree", root);
                frame.setVisible(true);
            }

            if (this.m_typecheckStrategies) {
                IScope typecheckingScope = new ScopedEntity(null,
                        "Typechecking root scope", Stitch.NULL_STITCH);
                // stitch.walker.setBehavior(stitch.getBehavior(Stitch.SCOPER_PASS));
                // stitch.walker.setASTNodeClass(LinesAwareAST.class.getName());
                // stitch.walker.script(root, typecheckingScope);

                stitch.walker.setBehavior(stitch
                        .getBehavior(Stitch.TYPECHECKER_PASS));
                stitch.walker.script(root, typecheckingScope);
            }

            stitch.walker.setBehavior(stitch.getBehavior(Stitch.SCOPER_PASS));
            stitch.walker.setASTNodeClass(LinesAwareAST.class.getName());
            stitch.walker.script(root, m_rootScope);
            if (m_execFromMain) {
                if (Tool.logger().isInfoEnabled()) {
                    Tool.logger().info(stitch.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // July 23
        ArrayList<ArrayList<AST>> al = new ArrayList<ArrayList<AST>>();
        ArrayList<AST> alRoot = new ArrayList<AST>();
        alRoot.add(root);
        al.add(alRoot);
        al.add(definedTactics);

        return al;
    }

    /**
     * This method is a hacky approach to evaluating a standalone expression
     * from any context outsidef of a Stitch script.
     * 
     * @param exprStr
     *            the expression to parse
     * @return Expression the Expression object for immediate evaluation
     */
    public Expression parseExpressionString(String exprStr) {
        if (m_emptyExprStitch == null) {
            URL url = Ohana.class.getResource("emptyExprScript.s");
            m_emptyExprStitch = Stitch.newInstance(url.getPath(),
                    new DummyStitchProblemHandler());
            // Initially, parse a placeholder Stitch script to build a script
            // scope
            try {
                instance().parseFile(m_emptyExprStitch);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null; // problematic if this happens...
            }
        }

        // First, find the placeholder statement in the placeholder script
        Statement stmt = null;
        for (IScope s : m_emptyExprStitch.script.getChildren()) {
            if (s instanceof Statement) {
                stmt = (Statement) s;
                break;
            }
        }
        if (stmt == null) return null;

        // Parse expression using the statement scope
        stmt.expressions().clear(); // clear out prev expression
        m_emptyExprStitch.pushScope(stmt);
        StitchLexer lexer = new StitchLexer(new StringReader(exprStr));
        StitchParser parser = new StitchParser(lexer);
        parser.setStitchProblemHandler(m_emptyExprStitch.stitchProblemHandler);
        try {
            parser.setASTNodeClass(LinesAwareAST.class.getName());
            parser.expression();
            m_emptyExprStitch.walker.setBehavior(m_emptyExprStitch
                    .getBehavior(Stitch.SCOPER_PASS));
            m_emptyExprStitch.walker.setASTNodeClass(LinesAwareAST.class
                    .getName());
            m_emptyExprStitch.walker.expr(parser.getAST());
            // debug output
            // AST results = m_emptyExprStitch.walker.getAST();
            // DumpASTVisitor visitor = new DumpASTVisitor();
            // visitor.visit(results);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stmt.expressions().get(0);
    }

    public ModelRepository modelRepository() {
        if (m_modelRepo == null)
            return ModelRepository.NULL_REPO;
        return m_modelRepo;
    }

    public void setModelRepository(ModelRepository repo) {
        m_modelRepo = repo;
    }

    public ModelOperator modelOperator() {
        if (m_modelOp == null)
            return ModelOperator.NO_OP;
        return m_modelOp;
    }

    public void setModelOperator(ModelOperator op) {
        m_modelOp = op;
    }

    /**
     * Records to database the supplied execution duration for the tactic
     * identified by the supplied fully-qualified name.
     * 
     * @param qualifiedName
     *            fully-qualified name of tactic
     * @param dur
     *            execution duration in milliseconds
     */
    public void recordTacticDuration(String qualifiedName, long dur) {
        if (m_tacticExecutionHistoryFile == null) { // lazy init
            m_tacticExecutionHistoryFile = modelRepository()
                    .tacticExecutionHistoryFile();
            // load file and populate hash map
            m_tacticHistoryMap = new LinkedHashMap<String, ExecutionHistoryData>();
            loadExecutionHistoryFromFile();
        }
        if (m_tacticHistoryMap == null) throw new RuntimeException(
                "Tactic execution history map mysteriously FAILED to initialize!");
        ExecutionHistoryData datum = m_tacticHistoryMap.get(qualifiedName);
        if (datum == null) {
            // new tactic
            datum = new ExecutionHistoryData();
            datum.setIdentifier(qualifiedName);
            m_tacticHistoryMap.put(qualifiedName, datum);
        }
        datum.addDurationSample(dur);

        // write tactic duration records to file every 10th update
        if (++m_updateCnt % 10 == 0) {
            saveExecutionHistoryToFile();
        }
    }

    /**
     * Returns the ExecutionHistoryData for the named tactic.
     * 
     * @param qualifiedName
     *            fully-qualified tactic name to get history data
     * @return ExecutionHistoryData object from which to access execution data
     *         stats.
     */
    public ExecutionHistoryData getTacticExecutionHistory(String qualifiedName) {
        if (m_tacticHistoryMap == null) return null;
        return m_tacticHistoryMap.get(qualifiedName);
    }

    /**
     * Loads execution history data from file.
     */
    private void loadExecutionHistoryFromFile() {
        if (m_tacticExecutionHistoryFile == null || m_tacticHistoryMap == null)
            return;

        try {
            m_tacticExecutionHistoryFile.createNewFile(); // create in case
            BufferedReader br = new BufferedReader(new FileReader(
                    m_tacticExecutionHistoryFile));
            /*
             * Each history line from file is expected to fit the pattern:
             * <identifier> <integer sampleSize> <double mean> <double variance>
             * <long min> <long max>
             */
            Pattern p = Pattern
                    .compile("^(\\S+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(\\d+)$");
            String line = null;
            while ((line = br.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.matches()) {
                    String iden = m.group(1);
                    ExecutionHistoryData datum = new ExecutionHistoryData(iden,
                            Integer.parseInt(m.group(2)), Double.parseDouble(m
                                    .group(3)), Double.parseDouble(m.group(4)),
                                    Long.parseLong(m.group(5)), Long.parseLong(m
                                            .group(6)));
                    m_tacticHistoryMap.put(iden, datum);
                } else {
                    Tool.error("Tactic history formatting error? " + line,
                            null, null);
                }
            }
            br.close();
        } catch (IOException e) {
            Tool.error("Failed opening/reading tactic execution hisotry file!",
                    e, null, null);
        }
    }

    /**
     * Saves execution history from internal map to Rainbow model designated
     * file.
     */
    private void saveExecutionHistoryToFile() {
        if (m_tacticExecutionHistoryFile == null || m_tacticHistoryMap == null)
            return;

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(
                    m_tacticExecutionHistoryFile));
            for (ExecutionHistoryData ehd : m_tacticHistoryMap.values()) {
                bw.write(ehd.toString());
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            Tool.error(
                    "Failed creating/writing tactic execution hisotry file!",
                    e, null, null);
        }
    }

    /**
     * A debugging method that evaluates the tactics in a Stitch script.
     * 
     * @param stitch
     * @throws RecognitionException
     */
    private void evaluateScript(Stitch stitch) {
        // try evaluating a tactic
        stitch.walker.setBehavior(stitch.getBehavior(Stitch.EVALUATOR_PASS));
        for (Tactic tactic : stitch.script.tactics) {
            Object[] args = new Object[tactic.args.size()];
            for (int i = 0; i < args.length; ++i) {
                args[i] = new MyInteger(10); // no typechecking yet...
            }
            tactic.evaluate(args);
        }
        if (m_execFromMain) {
            if (Tool.logger().isInfoEnabled()) {
                Tool.logger().info(stitch.toString());
            }
        }
    }

}
