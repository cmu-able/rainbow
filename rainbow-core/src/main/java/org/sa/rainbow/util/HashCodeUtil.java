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
package org.sa.rainbow.util;



import java.lang.reflect.Array;

/**
 * Collected methods which allow easy implementation of <code>hashCode</code>.
 *
 * Example use case:
 * <pre>
 *  public int hashCode(){
 *    int result = HashCodeUtil.SEED;
 *    //collect the contributions of various fields
 *    result = HashCodeUtil.hash(result, fPrimitive);
 *    result = HashCodeUtil.hash(result, fObject);
 *    result = HashCodeUtil.hash(result, fArray);
 *    return result;
 *  }
 * </pre>
 */
public final class HashCodeUtil {

    /**
     * An initial value for a <code>hashCode</code>, to which is added contributions
     * from fields. Using a non-zero value decreases collisons of <code>hashCode</code>
     * values.
     */
    public static final int SEED = 23;

    /**
     * booleans.
     */
    public static int hash( int aSeed, boolean aBoolean ) {
        return firstTerm( aSeed ) + ( aBoolean ? 1 : 0 );
    }

    /**
     * chars.
     */
    public static int hash( int aSeed, char aChar ) {
        return firstTerm( aSeed ) + aChar;
    }

    /**
     * ints.
     */
    public static int hash( int aSeed , int aInt ) {
        /*
         * Implementation Note
         * Note that byte and short are handled by this method, through
         * implicit conversion.
         */
        return firstTerm( aSeed ) + aInt;
    }

    /**
     * longs.
     */
    public static int hash( int aSeed , long aLong ) {
        return firstTerm(aSeed)  + (int)( aLong ^ (aLong >>> 32) );
    }

    /**
     * floats.
     */
    public static int hash( int aSeed , float aFloat ) {
        return hash( aSeed, Float.floatToIntBits(aFloat) );
    }

    /**
     * doubles.
     */
    public static int hash( int aSeed , double aDouble ) {
        return hash( aSeed, Double.doubleToLongBits(aDouble) );
    }

    /**
     * <code>aObject</code> is a possibly-null object field, and possibly an array.
     *
     * If <code>aObject</code> is an array, then each element may be a primitive
     * or a possibly-null object.
     */
    public static int hash (int aSeed, Object aObject) {
        int result = aSeed;
        if ( aObject == null) {
            result = hash(result, 0);
        }
        else if ( ! isArray(aObject) ) {
            result = hash(result, aObject.hashCode());
        }
        else {
            int length = Array.getLength(aObject);
            for ( int idx = 0; idx < length; ++idx ) {
                Object item = Array.get(aObject, idx);
                //recursive call!
                result = hash(result, item);
            }
        }
        return result;
    }


    /// PRIVATE ///
    private static final int fODD_PRIME_NUMBER = 37;

    private static int firstTerm( int aSeed ){
        return fODD_PRIME_NUMBER * aSeed;
    }

    private static boolean isArray (Object aObject) {
        return aObject.getClass().isArray();
    }
}

