package auxtestlib;

import java.lang.reflect.Array;

/**
 * Class providing comparison utilities.
 */
public final class ComparisonUtils {
	/**
	 * Utility class: no constructor.
	 */
	private ComparisonUtils() {
		/*
		 * Nothing to do.
		 */
	}

	/**
	 * Checks that twu arrays are equals. Two arrays are equals if they have the
	 * same length and all their objects are equal (two objects
	 * <code>null</code> are accepts as equal).
	 * 
	 * @param a1 firsdt array
	 * @param a2 second array
	 * 
	 * @return are the array contents equal?
	 */
	public static boolean arrayEquals(Object a1, Object a2) {
		assert a1 != null;
		assert a2 != null;

		Class<?> cls1 = a1.getClass();
		Class<?> cls2 = a2.getClass();
		assert cls1.isArray();
		assert cls2.isArray();

		int len1 = Array.getLength(a1);
		int len2 = Array.getLength(a2);
		if (len1 != len2) {
			return false;
		}

		for (int i = 0; i < len1; i++) {
			Object obj1 = Array.get(a1, i);
			Object obj2 = Array.get(a2, i);
			if (obj1 == null && obj2 == null) {
				continue;
			}

			if (obj1 == null || obj2 == null) {
				return false;
			}

			if (!obj1.equals(obj2)) {
				return false;
			}
		}

		return true;
	}
}
