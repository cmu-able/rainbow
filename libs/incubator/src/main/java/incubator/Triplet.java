package incubator;

import java.io.Serializable;
import java.util.Objects;

/**
 * Implementation of a triple in java.
 * @param <A> the type of the first element
 * @param <B> the type of the second element
 * @param <C> the type of the third element
 */
public class Triplet<A, B, C> implements Serializable {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The first element.
	 */
	private A m_first;

	/**
	 * The second element.
	 */
	private B m_second;

	/**
	 * The third element.
	 */
	private C m_third;

	/**
	 * Creates a new triple.
	 * @param first the first element
	 * @param second the second element
	 * @param third the third element
	 */
	public Triplet(A first, B second, C third) {
		m_first = first;
		m_second = second;
		m_third = third;
	}

	@Override
	public int hashCode() {
		return Objects.hash(m_first, m_second, m_third);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Triplet) {
			Triplet<?,?,?> otherPair = (Triplet<?,?,?>) other;
			return Objects.equals(otherPair.m_first, m_first)
					&& Objects.equals(otherPair.m_second, m_second)
					&& Objects.equals(otherPair.m_third, m_third);
		}

		return false;
	}

	@Override
	public String toString() {
		return "(" + m_first + ", " + m_second + ", " + m_third + ")";
	}

	/**
	 * Obtains the first element in the pair.
	 * @return the first element
	 */
	public A first() {
		return m_first;
	}

	/**
	 * Obtains the second element in the pair.
	 * @return the second element
	 */
	public B second() {
		return m_second;
	}

	/**
	 * Obtains the third element in the pair.
	 * @return the third element
	 */
	public C third() {
		return m_third;
	}
}
