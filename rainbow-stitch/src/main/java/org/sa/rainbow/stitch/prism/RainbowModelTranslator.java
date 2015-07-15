package org.sa.rainbow.stitch.prism;

import static org.sa.rainbow.stitch.parser.StitchLexerTokenTypes.BOOLEAN;
import static org.sa.rainbow.stitch.parser.StitchLexerTokenTypes.EQ;
import static org.sa.rainbow.stitch.parser.StitchLexerTokenTypes.EXISTS;
import static org.sa.rainbow.stitch.parser.StitchLexerTokenTypes.FORALL;
import static org.sa.rainbow.stitch.parser.StitchLexerTokenTypes.GE;
import static org.sa.rainbow.stitch.parser.StitchLexerTokenTypes.GT;
import static org.sa.rainbow.stitch.parser.StitchLexerTokenTypes.IDENTIFIER;
import static org.sa.rainbow.stitch.parser.StitchLexerTokenTypes.LE;
import static org.sa.rainbow.stitch.parser.StitchLexerTokenTypes.LOGICAL_AND;
import static org.sa.rainbow.stitch.parser.StitchLexerTokenTypes.LOGICAL_NOT;
import static org.sa.rainbow.stitch.parser.StitchLexerTokenTypes.LOGICAL_OR;
import static org.sa.rainbow.stitch.parser.StitchLexerTokenTypes.LT;
import static org.sa.rainbow.stitch.parser.StitchLexerTokenTypes.NE;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.acmestudio.acme.element.IAcmeElement;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.sa.rainbow.core.models.UtilityPreferenceDescription;
import org.sa.rainbow.core.models.UtilityPreferenceDescription.UtilityAttributes;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.stitch.core.Expression;
import org.sa.rainbow.stitch.core.IScope;
import org.sa.rainbow.stitch.core.LinesAwareAST;
import org.sa.rainbow.stitch.core.ScopedEntity;
import org.sa.rainbow.stitch.core.StitchScript;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.core.StrategyNode;
import org.sa.rainbow.stitch.core.Tactic;
import org.sa.rainbow.stitch.core.Var;
import org.sa.rainbow.stitch.error.DummyStitchProblemHandler;
import org.sa.rainbow.stitch.parser.StitchLexer;
import org.sa.rainbow.stitch.parser.StitchParser;
import org.sa.rainbow.stitch.util.Tool;
import org.sa.rainbow.stitch.visitor.Stitch;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;
import antlr.debug.misc.ASTFrame;

/**
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class RainbowModelTranslator {

    UtilityPreferenceDescription       utilities;
//    private UtilitiesSpecification parsedUtilities;
    private Stitch parsedStrategies;

    private StringBuffer outputBuffer = new StringBuffer();
    private StringBuffer archModuleBuffer = new StringBuffer();
    private StringBuffer tacticsBuffer = new StringBuffer();

    private static final String ENDL = System.getProperty("line.separator");

    private boolean verbose = false;

    private final static int BOOL = 0;
    private final static int INT = 1;

    private final Map<String, Integer> usedVariables = new HashMap<>();
    private final Map<String, String> variablesType = new HashMap<>();

    /**
     * Intermediate storage for new variables in the expression. If expression translation fails
     * these variables will not be added
     */
    private Map<String, Integer> newVariables = new HashMap<>();
    private OutputStream               output;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        /* Check command-line arguments */
        if (args.length < 3) {
            System.out.println("Not enough arguments.");
            System.out.println("Usage:");
            System.out.println("  RainbowModelTranslator startegiesFile utilitiesFile outputFile");
            System.exit(-1);
        }

        RainbowModelTranslator translator;
        try {
            translator = new RainbowModelTranslator(args[0], args[1], args[2]);
            translator.translate();
        } catch (Exception ex) {
            Logger.getLogger(RainbowModelTranslator.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(0);
    }

    private RainbowModelTranslator(String sf, String uf, String of) throws Exception {
        String strategyFile = sf;
        String utilitiesFile = uf;
        output = new FileOutputStream (of);

        try {
            readStrategies (strategyFile);
        } catch (FileNotFoundException | RecognitionException | TokenStreamException ex) {
            Logger.getLogger(RainbowModelTranslator.class.getName()).severe("Failed to read startegy files.");
            throw ex;
        }

        try {
            readUtilities (utilitiesFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(RainbowModelTranslator.class.getName()).severe("Failed to read utilities file.");
            throw ex;
        }
    }

    public RainbowModelTranslator (Stitch strategies, UtilityPreferenceDescription utilities, OutputStream os) {
        output = os;
        this.parsedStrategies = strategies;
        this.utilities = utilities;
    }

    private void readStrategies (String strategyFile) throws FileNotFoundException, RecognitionException,
    TokenStreamException {
        /**
         * Read
         */
        parsedStrategies = Stitch.newInstance(strategyFile, new DummyStitchProblemHandler());
        FileInputStream input = new FileInputStream(parsedStrategies.path);
        StitchLexer lexer = new StitchLexer(input);

        /**
         * Parse
         */
        StitchParser parser = new StitchParser(lexer);
        parser.setStitchProblemHandler(parsedStrategies.stitchProblemHandler);
        parser.setASTNodeClass(LinesAwareAST.class.getCanonicalName());
        parser.script();

        AST root = parser.getAST();
        if (false) {
            System.out.println(root.toStringList());
            ASTFrame frame = new ASTFrame("Stitch Tree", root);
            frame.setVisible(true);
        }

        /**
         * Construct Stitch script
         */
        IScope scope = new ScopedEntity(null, "Stitich Root Scope",
                Stitch.NULL_STITCH);
        Stitch.NULL_STITCH.script = new StitchScript(scope,
                "Stitch Root Script", Stitch.NULL_STITCH);
        parsedStrategies.walker.setBehavior(parsedStrategies.getBehavior(Stitch.SCOPER_PASS));
        parsedStrategies.walker.setASTNodeClass(LinesAwareAST.class.getName());
        parsedStrategies.walker.script(root, scope);
    }

    private void readUtilities (String utilitiesFile) throws FileNotFoundException {
//        Yaml yaml = new Yaml (new Constructor (UtilitiesSpecification.class));
//        File file = new File (utilitiesFile);
//        InputStream is = new FileInputStream (file);
//        parsedUtilities = (UtilitiesSpecification )yaml.load (is);
    }

    private void translate() throws Exception {
        outputBuffer.append("dtmc").append(ENDL).append(ENDL);

        translateRewards();
        translateUtilities();
        translateStrategies();
        translateTactics();
        makeVariables();

        // Concatenate buffers
        outputBuffer.append(ENDL);
        outputBuffer.append("module arch").append(ENDL);

        archModuleBuffer.append(ENDL);
        archModuleBuffer.append(tacticsBuffer);

        outputBuffer.append(archModuleBuffer);
        outputBuffer.append("endmodule").append(ENDL);
        outputBuffer.append(ENDL);

        /* WriteOutput */
        output.write (outputBuffer.toString ().getBytes ());
        output.flush ();
//        try (FileWriter writer = new FileWriter(outputFile)) {
//            try (BufferedWriter out = new BufferedWriter(writer)) {
//                out.write(outputBuffer.toString());
//                out.flush();
//            }
//        } catch (IOException ex) {
//            Logger.getLogger(RainbowModelTranslator.class.getName()).severe("Cannot write output file");
//            throw ex;
//        }
    }

    private void makeVariables() {
        Set<String> outTypes = new HashSet<>();
        for (String var : usedVariables.keySet()) {
            String type = variablesType.get(var);
            // Variable
            if (usedVariables.get(var) == INT) {
                archModuleBuffer.append(var).append(" : [0..MAX_").append(type).append("]");
            } else {
                archModuleBuffer.append(var).append(" : bool");
            }
            archModuleBuffer.append(" init INIT_").append(var).append(";").append(ENDL);

            if (!outTypes.contains(type)) {
                outTypes.add(type);
                // Max constants
                if (usedVariables.get(var) == INT) {
                    outputBuffer.append("const ");
                    outputBuffer.append("int ");
                    outputBuffer.append("MAX_").append(type).append(";").append(ENDL);
                }
            }

            // Init constants
            outputBuffer.append("const ");
            if (usedVariables.get(var) == INT) {
                outputBuffer.append("int ");
            } else {
                outputBuffer.append("bool ");
            }
            outputBuffer.append("INIT_").append(var).append(";").append(ENDL);
        }
    }

    int stratNum = 0;

    private void translateStrategies() {
        /**
         * To avoid name conflicts each module variable is suffixed with an unique running counter
         */

        for (Strategy strategy : parsedStrategies.script.strategies) {
            outputBuffer.append("module ").append(strategy.getName()).append(ENDL);
            outputBuffer.append("leaf").append(stratNum).append(" : bool init false;").append(ENDL);
            outputBuffer.append("end").append(stratNum).append(" :  bool init false;").append(ENDL);
            outputBuffer.append("node").append(stratNum).append(" : [0..").append(strategy.nodes.size()).append("] init 0;").append(ENDL);

            Map<String, Integer> nodeToId = new HashMap<>();
            int i = 0;
            for (String nodeName : strategy.nodes.keySet()) {
                nodeToId.put(nodeName, i++);
            }

            StrategyNode node = strategy.getRootNode();
            dumpStrategyNode(node, strategy, nodeToId);

            /**
             * We use global 'leaf' variable to track end of strategy execution
             */
            outputBuffer.append("[] (leaf").append(stratNum).append(") -> 1: (end").append(stratNum).append("'=true) & (leaf' = true);").append(ENDL);

            // TODO: Add repetition support
            outputBuffer.append("endmodule").append(ENDL).append(ENDL);

            stratNum++;
        }
    }

    private void dumpStrategyNode(StrategyNode node, Strategy strategy, Map<String, Integer> nodeToId) {
        for (String child : node.getChildren()) {
            String tactic = strategy.nodes.get(child).getTactic();
            tactic = (tactic == null) ? "" : tactic;

            Expression condition = strategy.nodes.get(child).getCondExpr();
            String conditionStr = "true";
            /**
             * TODO: only inline conditions are supported now, ie referencing to local functions is
             * nor supported
             */
            if (condition != null) {
                try {
                    newVariables = new HashMap<>();
                    conditionStr = translateConditionExpression(condition);
                    usedVariables.putAll(newVariables);
                } catch (ExpressionTranslationException ex) {
                    // Leave as true
                }
            }

            outputBuffer.append("[").append(tactic).append("]");
            outputBuffer.append("(node").append(stratNum).append("=").append(nodeToId.get(node.label())).append(")");
            outputBuffer.append(" & ").append(conditionStr);
            outputBuffer.append(" -> 1: (node").append(stratNum).append("'=").append(nodeToId.get(child)).append(")");
            if (strategy.nodes.get(child).getChildren().isEmpty()) {
                outputBuffer.append(" & (leaf").append(stratNum).append("'=true)");
            }
            outputBuffer.append(";").append(ENDL);
        }
        for (String child : node.getChildren()) {
            dumpStrategyNode(strategy.nodes.get(child), strategy, nodeToId);
        }
    }

    private void translateUtilities() {
        /* Dump curves */
        for (Map.Entry<String, UtilityAttributes> utility : utilities.getUtilities ().entrySet ()) {

//        for (Map.Entry<String, UtilityDescription> utility : utilities.utilities.entrySet()) {
            String utilityName = utility.getKey();
            outputBuffer.append("// ").append(utility.getValue().label).append(ENDL);
            outputBuffer.append ("// ").append (utility.getValue ().desc).append (ENDL);
            outputBuffer.append("// ").append(utility.getValue().mapping).append(ENDL);
            outputBuffer.append("formula ").append(utilityName).append(" = 0 ");

            boolean first = true;
            for (String segment : translateCurveSegments (utilityName + "_", utility.getValue ().values)) {
//                for (String segment : translateCurveSegments(utilityName + "_", utility.getValue().utility)) {
                if (!first) {
                    outputBuffer.append(makeIndent(8 + utilityName.length()));
                } else {
                    first = false;
                }
                outputBuffer.append(" + ").append(segment).append(ENDL);
            }

            outputBuffer.append(";");

            /* Add some space */
            outputBuffer.append(ENDL).append(ENDL);
        }

        /* Max, inits and variables */
        StringBuffer tmpOutBuffer = new StringBuffer();
        for (Map.Entry<String, UtilityAttributes> utility : utilities.getUtilities ().entrySet ()) {
//            for (Map.Entry<String, UtilityDescription> utility : parsedUtilities.utilities.entrySet()) {
            String utilityName = utility.getKey();
            Number maxValue = getMaxValue (utility.getValue ().values);
//            Integer maxValue = utility.getValue().values.lastEntry().getKey();
            outputBuffer.append("const MAX_").append(utilityName).append("_ = ").
            append(maxValue).append(";").append(ENDL);

            tmpOutBuffer.append("const INIT_").append(utilityName).append("_;").append(ENDL);

            archModuleBuffer.append(utilityName).append("_ : [0..").append(maxValue).
            append("] init INIT_").append(utilityName).append("_;").append(ENDL);
        }

        outputBuffer.append(ENDL);
        outputBuffer.append(tmpOutBuffer);

        archModuleBuffer.append(ENDL);

        outputBuffer.append(ENDL);

        for (Entry<String, Map<String, Object>> vector : utilities.attributeVectors.entrySet ()) {
//            for (Map.Entry<String, TreeMap<String, Integer>> vector : parsedUtilities.vectors.entrySet()) {
            translateVector(vector.getKey(), vector.getValue());
            outputBuffer.append(ENDL);
        }
    }

    private Number getMaxValue (Map<Number, Number> values) {
        Number max = new Integer (Integer.MIN_VALUE);
        for (Number n : values.keySet ()) {
            if (n.doubleValue () > max.doubleValue ()) {
                max = n;
            }
        }
        return max;
    }

    private List<String> translateCurveSegments (String argName, Map<Number, Number> curve) {
        List<String> res = new LinkedList<>();

        Map.Entry<Integer, Double> segments[] = curve.entrySet().toArray(new Map.Entry[curve.size()]);

        String segmentBegin = "(" + argName + " = %1$d ? %2$f : 0)";
        res.add(String.format(segmentBegin, segments[0].getKey(), segments[0].getValue()));

        // 1 - arg1, 2 - arg2, 3 - val1, 4 - val2
        String segment = "(" + argName + ">%1$d & " + argName + "<=%2$d ? "
                + "%3$f + (%4$f - %3$f) * ((" + argName + "- %1$d" + ")/(%2$d - %1$d))"
                + ":0)";

        for (int i = 0; i < segments.length - 1; i++) {
            Integer arg1 = segments[i].getKey();
            Integer arg2 = segments[i + 1].getKey();
            Double val1 = segments[i].getValue();
            Double val2 = segments[i + 1].getValue();
            res.add(String.format(segment, arg1, arg2, val1, val2));
        }

        String segmentEnd = "(" + argName + ">%1$d ? 0 : 0)";
        res.add(String.format(segmentEnd, segments[segments.length - 1].getKey()));

        return res;
    }

    private void translateVector (String vectorName, Map<String, Object> vector) {
        // 1 - utility, 2 - max value, 3 - impact
        String vectorFormat = "%1$s_ + (%3$d) >= 0 ? (%1$s_ + (%3$d) <= %2$s ? %1$s_ + (%3$d) : %2$s) : 0";

        for (Map.Entry<String, Object> impact : vector.entrySet ()) {
            String utility = impact.getKey();
            Number impactValue = (Number )impact.getValue ();
            outputBuffer.append("formula ").append(vectorName).append("_").
            append(utility).append("_").append(" = ");
            outputBuffer.append(String.format(vectorFormat, utility, "MAX_" + utility + "_", impactValue));
            outputBuffer.append(";").append(ENDL);

        }
    }

    private void translateRewards() {
        outputBuffer.append("// Utility rewards").append(ENDL);
        /**
         * Global 'leaf' variable to track end of strategy execution at the strategy tree leaf node.
         */
        outputBuffer.append("global leaf : bool init false;").append(ENDL);
        for (String rewardName : utilities.weights.keySet ()) {
//            for (String rewardName : parsedUtilities.weights.keySet()) {
            Map<String, Double> reward = utilities.weights.get (rewardName);
            String rewardFunc = "";
            for (String utilityName : reward.keySet()) {
                rewardFunc += reward.get(utilityName) + " * " + utilityName + " + ";
            }
            rewardFunc += "0.0";
            /* Initial reward */
            outputBuffer.append("rewards \"init_").append(rewardName.replaceAll(" ", "_")).append("\"").append(ENDL);
            outputBuffer.append(makeIndent(8)).append("true : ").append(rewardFunc).append(";").append(ENDL);
            outputBuffer.append("endrewards").append(ENDL);
            /* Normal reward */
            outputBuffer.append("rewards \"").append(rewardName.replaceAll(" ", "_")).append("\"").append(ENDL);
            outputBuffer.append(makeIndent(8)).append("leaf : ").append(rewardFunc).append(";").append(ENDL);
            outputBuffer.append("endrewards").append(ENDL);
            outputBuffer.append(ENDL);
        }
        outputBuffer.append(ENDL);
    }

    private String makeIndent(int n) {
        String res = "";
        for (int i = 0; i < n; i++) {
            res += " ";
        }
        return res;
    }

    private void translateTactics() {
        if (verbose) {
            tacticsBuffer.append("//").append(ENDL).append("// Begin of the tactic block.").append(ENDL);
            tacticsBuffer.append("// Use the raw tactics and automatic suggestions below").append(ENDL);
            tacticsBuffer.append("// to manually make PRISM tactic formulas.");
            tacticsBuffer.append(ENDL).append("//").append(ENDL);
        }
        for (Tactic tactic : parsedStrategies.script.tactics) {
            if (verbose) {
                tacticsBuffer.append(ENDL).append("// Raw tactic").append(ENDL).append(ENDL);
                tacticsBuffer.append("//").append(tactic.toString().replace(ENDL, ENDL + "//"));
                tacticsBuffer.append(ENDL);
                tacticsBuffer.append(ENDL).append("// Automatic suggestion").append(ENDL).append(ENDL);
            }

            String tacticName = tactic.getName();
            String tacticCondition = "";
            String expString;
            for (Expression expr : tactic.conditions) {
                try {
                    newVariables = new HashMap<>();
                    expString = translateConditionExpression(expr);
                    usedVariables.putAll(newVariables);
                } catch (ExpressionTranslationException ex) {
                    expString = "true";
                }
                tacticCondition += "(" + expString + ") & ";
            }
            tacticCondition += "true";
            String tacticFormula = "[" + tacticName + "]" + tacticCondition + " -> 1: ";
            int i = 1;
            Map<String, Object> vector = utilities.attributeVectors.get (tacticName);
            if (vector == null) {
                // No vector defined for this tactic
                tacticFormula += "true;";
            } else {
                int utilNum = vector.keySet().size();
                for (String utility : vector.keySet()) {
                    tacticFormula += "(" + utility + "_'=" + tacticName + "_" + utility + "_)";
                    if (i < utilNum) {
                        tacticFormula += " & ";
                    } else {
                        tacticFormula += ";";
                    }
                    i++;
                }
            }
            tacticsBuffer.append(tacticFormula).append(ENDL);
        }
    }

    /**
     * Exception for expression translations
     */
    private class ExpressionTranslationException extends Exception {
    }

    private String translateConditionExpression(Expression expr) throws ExpressionTranslationException {
        String res = "";
        AST ast = expr.ast();
        /**
         * TODO: replace with more general processing.
         */
        if (expr.kind == Expression.Kind.UNKNOWN && expr.expressions().size() > 0) {
            expr = expr.expressions().get(0);
        }
        if (expr.kind != Expression.Kind.QUANTIFIED) // Not supported
            throw new ExpressionTranslationException();
        if (expr.vars().size() > 1) // Not supported
            throw new ExpressionTranslationException();
        String varName = (String) expr.vars().keySet().toArray()[0];
        Var var = expr.vars().get(varName);

        // Set expression
        Expression setExpr = expr.expressions().get(0);
        // Collect elements of the set
        try {
            setExpr.evaluate(null);
        } catch (Exception ex) {
            throw new ExpressionTranslationException();
        }
        Set set;
        if (setExpr.getResult() instanceof Set) {
            set = (Set) setExpr.getResult();
        }
        else
            throw new ExpressionTranslationException();

        Set subset = new LinkedHashSet();
        if (set.size() > 0) {
            for (Object o : set) {
                if (Tool.typeMatches(var, o) && Tool.isArchEnabled(o)) {
                    // type matches AND this object in quantified set is arch
                    // enabled
                    subset.add(o);
                }
            }
        }

        // Predicate expression
        Expression predicateExpr = expr.expressions().get(1);

        String quanOp;

        switch (ast.getType()) {
        case EXISTS:
            res += "false";
            quanOp = " | ";
            break;
        case FORALL:
            res += "true";
            quanOp = " & ";
            break;
        default:
            // Not supported
            throw new ExpressionTranslationException();
        }

        for (Object elem : subset) {
            var.setValue(elem);

            String subExpr = translateAST(predicateExpr.ast(), expr);
            res += quanOp + subExpr;
        }
        return res;
    }

    private String translateAST(AST ast, IScope scope) throws ExpressionTranslationException {
        String res;

        String op;
        AST left, right;

        switch (ast.getType()) {
        case BOOLEAN:
            res = ast.getText();
            break;
        case LOGICAL_AND:
        case LOGICAL_OR:
            // Binary logic
            left = ast.getFirstChild();
            right = left.getNextSibling();
            switch (ast.getType()) {
            case LOGICAL_AND:
                op = " & ";
                break;
            case LOGICAL_OR:
                op = " | ";
                break;
            default:
                throw new ExpressionTranslationException();
            }
            res = "(" + translateAST(left, scope) + op + translateAST(right, scope) + ")";
            break;
        case IDENTIFIER:
            try {
                String id = ast.getText();
                String varId = id.substring(0, id.indexOf("."));
                String propId = id.substring(id.indexOf(".") + 1);
                Object o = scope.lookup(varId);

                if (o == null) throw new ExpressionTranslationException();

                // Only Acme elements and Models are supported
                if (o instanceof AcmeModelInstance) {
                    // Global variable
                    res = propId;
                    newVariables.put(res, INT);
                    variablesType.put(res, res);
                } else {
                    Object value = ((Var) o).getValue();
                    if (value instanceof IAcmeElement) {
                        res = ((IAcmeElement) value).getName() + "_" + propId;

                        int type = INT;

                        Object prop = ((IAcmeElement) value).lookupName(propId);
                        if (prop != null) {
                            if (prop instanceof IAcmeProperty) {
                                if ("boolean".equals(((IAcmeProperty) prop).getType().getName())) {
                                    type = BOOL;
                                }
                            }
                        }

                        newVariables.put(res, type);
                        String varType = ((Var) o).getType();
                        variablesType.put(res, varType.substring(varType.indexOf(".") + 1) + "_" + propId);

                    }
                    else
                        /**
                         * Translation of other identifiers, ie local stitch is not supported
                         */
                        throw new ExpressionTranslationException();
                }

            } catch (Exception ex) {
                throw new ExpressionTranslationException();
            }
            break;
        case LOGICAL_NOT:
            op = ast.getText();
            right = ast.getFirstChild();
            res = op + translateAST(right, scope);
            break;
        case GT:
        case GE:
        case LT:
        case LE:
        case EQ:
        case NE:
            left = ast.getFirstChild();
            right = left.getNextSibling();
            op = " " + ast.getText() + " ";
            res = "(" + translateAST(left, scope) + op + translateAST(right, scope) + ")";
            break;
        default:
            throw new ExpressionTranslationException();
        }
        return res;
    }

}
