package org.sa.rainbow.im;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.sa.rainbow.im.parser.IMLexer;
import org.sa.rainbow.im.parser.IMParser;

public class IMTranslator {
    public static void main(String[] args) throws Exception {

        ANTLRInputStream input = new ANTLRInputStream(System.in);

        IMLexer lexer = new IMLexer(input);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        IMParser parser = new IMParser(tokens);

        ParseTree tree = parser.init();
        //System.out.println(tree.toStringTree(parser));

        ParseTreeWalker walker = new ParseTreeWalker();

        walker.walk(new IMToPRISM(), tree);
        System.out.println();
    }
}