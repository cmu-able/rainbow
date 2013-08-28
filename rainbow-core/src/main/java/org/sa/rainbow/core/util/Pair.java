package org.sa.rainbow.core.util;

import java.io.Serializable;

/**
 * A pair of objects of different types.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class Pair<T,E> implements Serializable, Comparable<Pair<T,E>>, Cloneable {

    private static final long serialVersionUID = -3528940665822993558L;

    /** First value of the pair, usually a "type" */
    private T m_t = null;
    /** Second value of the pair, usually a name or "element" */
    private E m_e = null;

    /**
     * Protected Default Constructor used for cloning.
     */
    protected Pair () {
    }

    /**
     * Main Constructor for instantiating a pair.
     */
    public Pair (T t, E e) {
        m_t = t;
        m_e = e;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        boolean rv = true;
        if (obj instanceof Pair) {
            Pair<T,E> pair = (Pair<T,E> )obj;
            if (m_t != null) {
                rv &= m_t.equals(pair.m_t);
            } else {
                rv &= m_t == pair.m_t;
            }
            if (m_e != null) {
                rv &= m_e.equals(pair.m_e);
            } else {
                rv &= m_e == pair.m_e;
            }
        } else {
            rv = false;
        }
        return rv;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        // create hashcode from the strings of the pair element objects
        StringBuilder buf = new StringBuilder();
        buf.append(m_t).append(m_e);
        return buf.toString().hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        m_t = null;
        m_e = null;
        super.finalize();
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public int compareTo(Pair<T,E> p) {
        // now we do comparison
        int rv = ((Comparable<T> )firstValue()).compareTo(p.firstValue());
        if (rv == 0) {
            rv = ((Comparable<E> )secondValue()).compareTo(p.secondValue());
        }
        return rv;
    }

    /**
     * Returns a shallow copy of this Pair of elements, meaning the returned
     * pair object would be a new and different instance, but a pair member
     * value itself is only cloned if it is also a Pair<>.
     */
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object clone () {
        Pair<T,E> clonedPair = null;
        try {
            clonedPair = (Pair<T,E> )super.clone();
            clonedPair.m_t = this.m_t;
            clonedPair.m_e = this.m_e;
        } catch (CloneNotSupportedException e) {
            // should not be true
        }
        return clonedPair;
    }

    public T firstValue () {
        return m_t;
    }

    public void setFirstValue (T t) {
        m_t = t;
    }

    public E secondValue () {
        return m_e;
    }

    public void setSecondValue (E e) {
        m_e = e;
    }
}
