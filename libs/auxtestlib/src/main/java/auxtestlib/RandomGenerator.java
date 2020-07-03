package auxtestlib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Random;

/**
 * Class used to generate random values for unit tests.
 */
public final class RandomGenerator {
	/**
	 * A random number generator.
	 */
	private static Random random = new Random();

	/**
	 * Unused constructor: utility class.
	 */
	private RandomGenerator() {
		/*
		 * Nothing to do.
		 */
	}

	/**
	 * Obtains a random number between [0, max[.
	 * 
	 * @param max the maximum value
	 * 
	 * @return the random value
	 */
	public static int randInt(int max) {
		if (max <= 0) {
			throw new IllegalArgumentException("max <= 0");
		}

		return random.nextInt(max);
	}

	/**
	 * Obtains a random number between [min, max[. It accepts min = max in which
	 * case returns that number.
	 * 
	 * @param min the minimum number
	 * @param max the maximum number
	 * 
	 * @return the random number
	 */
	public static int randInt(int min, int max) {
		if (min > max) {
			throw new IllegalArgumentException("min < max");
		}

		if (min == max) {
			return min;
		}

		return min + randInt(max - min);
	}

	/**
	 * Generates a random byte array.
	 * 
	 * @param size the array size
	 * 
	 * @return the byte array
	 */
	public static byte[] randBytes(int size) {
		if (size < 0) {
			throw new IllegalArgumentException("size < 0");
		}

		byte dt[] = new byte[size];
		for (int i = 0; i < size; i++) {
			dt[i] = (byte) randInt(256);
		}

		return dt;
	}

	/**
	 * Obtains a random date based on the actual date. The returned date is
	 * uniformly distribute in the interval [d+min,d+max[ where d is the actual
	 * date.
	 * 
	 * @param min the distance to the minimum of the interval (in seconds)
	 * @param max the distance to the maximum of the interval (in seconds)
	 * 
	 * @return the generated random date
	 */
	public static Date randDate(int min, int max) {
		if (min > max) {
			throw new IllegalArgumentException("min > max");
		}

		Date d = new Date();

		d.setTime(d.getTime() + randInt(min, max) * 1000L);
		return d;
	}

	/**
	 * Obtains a random date based on the actual date. The returned date is
	 * uniformly distribute in the interval [d+min,d+max[ where d is the actual
	 * date.
	 * 
	 * @param min the distance to the minimum of the interval (in days)
	 * @param max the distance to the maximum of the interval (in days)
	 * 
	 * @return the generated random date
	 */
	public static Date randDateDays(int min, int max) {
		if (min > max) {
			throw new IllegalArgumentException("min > max");
		}

		return randDate(min * 3600 * 24, max * 3600 * 24);
	}

	/**
	 * Returns a random value from a supplied enumeration (which cannot be an
	 * empty enumeration).
	 * 
	 * @param <E> the type of the enumeration
	 * @param enumType the class of enumeration
	 * 
	 * @return an item of the enumeration.
	 */
	public static <E extends Enum<E>> E getRandomEnumValue(Class<E> enumType) {
		if (enumType == null) {
			throw new IllegalArgumentException("enumType == null");
		}

		E cts[] = enumType.getEnumConstants();
		if (cts.length == 0) {
			throw new IllegalArgumentException("Enumeration " + enumType
					+ " is empty.");
		}

		int i = RandomGenerator.randInt(cts.length - 1);
		return cts[i];
	}

	/**
	 * Picks a random value from an array.
	 * 
	 * @param <E> the type of value
	 * @param array the array to pick values from (cannot be empty or
	 * <code>null</code>)
	 * 
	 * @return a random value from the array
	 */
	public static <E> E randArray(E[] array) {
		if (array == null) {
			throw new IllegalArgumentException("array == null");
		}

		if (array.length == 0) {
			throw new IllegalArgumentException("array.length == 0");
		}

		return array[randInt(array.length)];
	}

	/**
	 * Picks a random value from a collection.
	 * 
	 * @param <E> the type of value
	 * @param collection the collection to pick values from (cannot be empty or
	 * <code>null</code>)
	 * 
	 * @return a random value from the array
	 */
	public static <E> E randCollection(Collection<E> collection) {
		if (collection == null) {
			throw new IllegalArgumentException("collection != null");
		}

		if (collection.isEmpty()) {
			throw new IllegalArgumentException("collection.isEmpty()");
		}

		ArrayList<E> list = new ArrayList<>(collection);
		return list.get(randInt(list.size()));
	}
}
