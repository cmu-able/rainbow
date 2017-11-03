package org.sa.rainbow.stitch;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Pair;

/**
 * Created by schmerl on 9/28/2016.
 */
public class Stitch2TokenFactory implements TokenFactory<CommonToken> {
    CharStream input;

    public Stitch2TokenFactory (CharStream input) {
        this.input = input;
    }


    @Override
    public CommonToken create (@NotNull Pair<TokenSource, CharStream> source, int type, String text, int channel, int
            start,
                               int
                                       stop,
                               int line, int charPosistionInLine) {
        CommonToken t = new CommonToken (source, type, channel, start, stop);
        t.setCharPositionInLine (charPosistionInLine);
        t.setLine (line);
        return t;
    }

    @Override
    public CommonToken create (int type, String text) {
        return new CommonToken (type, text);
    }
}
