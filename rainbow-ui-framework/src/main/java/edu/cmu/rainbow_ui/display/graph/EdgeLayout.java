/*
 * The MIT License
 *
 * Copyright 2014 CMU MSIT-SE Rainbow Team.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package edu.cmu.rainbow_ui.display.graph;

/**
 * Edge layout data structure.
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class EdgeLayout {

    private final int sourceX;
    private final int sourceY;
    private final int endX;
    private final int endY;

    /**
     * Constructor with all fields initialization.
     *
     * @param sourceX source X coordinate
     * @param sourceY source Y coordinate
     * @param endX end X coordinate
     * @param endY end Y coordinate
     */
    public EdgeLayout(int sourceX, int sourceY, int endX, int endY) {
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.endX = endX;
        this.endY = endY;
    }

    /**
     * @return the sourceX
     */
    public int getSourceX() {
        return sourceX;
    }

    /**
     * @return the sourceY
     */
    public int getSourceY() {
        return sourceY;
    }

    /**
     * @return the endX
     */
    public int getEndX() {
        return endX;
    }

    /**
     * @return the endY
     */
    public int getEndY() {
        return endY;
    }

}
