/*
 * The MIT License
 *
 * Copyright 2014 CMU ABLE Group.
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
/**
 * Created April 26, 2006.
 */
package org.sa.rainbow.stitch.core;


/**
 * Custom implementation of Integer to support polymorphic arithmetic operation.
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class MyInteger extends MyNumber {
    private static final long serialVersionUID = 6380229820317148804L;

    /**
     * @param n  the number to use to create integer
     */
    public MyInteger(Integer n) {
        super(n);
    }

    /**
     * @param n  the number to use to create integer
     */
    public MyInteger(Long n) {
        super(n);
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.MyNumber#toJavaNumber()
     */
    @Override
    public Number toJavaNumber() {
        if (m_long > Integer.MAX_VALUE) return m_long;
        else
            return (int )m_long;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.MyNumber#plus(org.sa.rainbow.stitch.core.MyNumber)
     */
    @Override
    public MyNumber plus(MyNumber addend) {
        MyNumber result = null;
        if (addend instanceof MyInteger) {
            result = new MyInteger(m_long);
            result.m_long += addend.longValue();
        } else if (addend instanceof MyDouble) {
            result = new MyDouble((double )m_long);
            result = result.plus(addend);
        } // can't be any other subtype
        return result;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.MyNumber#minus(org.sa.rainbow.stitch.core.MyNumber)
     */
    @Override
    public MyNumber minus(MyNumber subtrahend) {
        MyNumber result = null;
        if (subtrahend instanceof MyInteger) {
            result = new MyInteger(m_long);
            result.m_long -= subtrahend.longValue();
        } else if (subtrahend instanceof MyDouble) {
            result = new MyDouble((double )m_long);
            result = result.minus(subtrahend);
        } // can't be any other subtype
        return result;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.MyNumber#times(org.sa.rainbow.stitch.core.MyNumber)
     */
    @Override
    public MyNumber times(MyNumber multipler) {
        MyNumber result = null;
        if (multipler instanceof MyInteger) {
            result = new MyInteger(m_long);
            result.m_long *= multipler.longValue();
        } else if (multipler instanceof MyDouble) {
            result = new MyDouble((double )m_long);
            result = result.times(multipler);
        } // can't be any other subtype
        return result;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.MyNumber#dividedBy(org.sa.rainbow.stitch.core.MyNumber)
     */
    @Override
    public MyNumber dividedBy(MyNumber divisor) {
        MyNumber result = null;
        if (divisor instanceof MyInteger) {
            result = new MyInteger(m_long);
            result.m_long /= divisor.longValue();
        } else if (divisor instanceof MyDouble) {
            result = new MyDouble((double )m_long);
            result = result.dividedBy(divisor);
        } // can't be any other subtype
        return result;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.MyNumber#modulus(org.sa.rainbow.stitch.core.MyNumber)
     */
    @Override
    public MyNumber modulus(MyNumber divisor) {
        MyNumber result = new MyInteger(m_long);
        if (divisor instanceof MyInteger) {
            result.m_long %= divisor.longValue();
        } else if (divisor instanceof MyDouble) {
            result.m_long %= divisor.doubleValue();
        } // can't be any other subtype
        return result;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.MyNumber#eq(org.sa.rainbow.stitch.core.MyNumber)
     */
    @Override
    public Boolean eq(MyNumber operand) {
        Boolean result = null;
        if (operand instanceof MyInteger) {
            result = new Boolean(m_long == operand.longValue());
        } else if (operand instanceof MyDouble) {
            result = new Boolean(m_long == operand.doubleValue());
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.MyNumber#lt(org.sa.rainbow.stitch.core.MyNumber)
     */
    @Override
    public Boolean lt(MyNumber operand) {
        Boolean result = null;
        if (operand instanceof MyInteger) {
            result = new Boolean(m_long < operand.longValue());
        } else if (operand instanceof MyDouble) {
            result = new Boolean(m_long < operand.doubleValue());
        }
        return result;
    }

    public MyDouble toDouble() {
        MyDouble newDouble = new MyDouble((double )m_long);
        return newDouble;
    }

}
