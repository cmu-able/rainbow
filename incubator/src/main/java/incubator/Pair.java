package incubator;

/**
 * Implementation of a pair in java. Adapted from <a href=
 * "http://stackoverflow.com/questions/156275/what-is-the-equivalent-of-the-c-pairl-r-in-java"
 * >here</a>.
 * 
 * @param <A> the type of the first element
 * @param <B> the type of the second element
 */
public class Pair<A, B> {
	/**
	 * The first element.
	 */
	private A first;

	/**
	 * The second element.
	 */
	private B second;

	/**
	 * Creates a new pair.
	 * @param first the first element
	 * @param second the second element
	 */
	public Pair(A first, B second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public int hashCode() {
		int hashFirst = first != null ? first.hashCode() : 0;
		int hashSecond = second != null ? second.hashCode() : 0;

		return (hashFirst + hashSecond) * hashSecond + hashFirst;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Pair) {
			Pair<?,?> otherPair = (Pair<?,?>) other;
			return ((this.first == otherPair.first
					|| (this.first != null
					&& otherPair.first != null
					&& this.first.equals(otherPair.first)))
					&& (this.second == otherPair.second
					|| (this.second != null
					&& otherPair.second != null
					&& this.second.equals(otherPair.second))));
		}

		return false;
	}

	@Override
	public String toString() {
		return "(" + first + ", " + second + ")";
	}

	/**
	 * Obtains the first element in the pair.
	 * @return the first element
	 */
	public A first() {
		return first;
	}

	/**
	 * Obtains the second element in the pair.
	 * @return the second element
	 */
	public B second() {
		return second;
	}
}