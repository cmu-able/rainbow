package org.sa.rainbow.core;

/**
 * This interface defines one method that provides an identifier string of the
 * implementing class.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public interface Identifiable {

    /**
     * Returns an identifier string, not necessarily unique.
     * @return String  an identifier string. 
     */
    public String id ();


}
