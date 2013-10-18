/**
 * Created April 24, 2006.
 */
package org.sa.rainbow.stitch.core;

import org.acmestudio.acme.core.type.IAcmeFloatValue;
import org.acmestudio.acme.core.type.IAcmeIntValue;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.sa.rainbow.stitch.util.Tool;

/**
 * Custom implementation of Number to support polymorphic arithmetic operation.
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public abstract class MyNumber extends Number {

	private Number m_number = null;
	protected double m_double = 0.0;  // a hack, store the most precise possible
	protected long m_long = 0L;  // a hack, store the most precise possible

	/**
	 * Support IAcmeProperty value for Float and Int
	 */
	public static MyNumber newNumber (IAcmeProperty prop) {
		MyNumber num = null;
		if (prop.getValue() instanceof IAcmeIntValue) {
			num = new MyInteger(((IAcmeIntValue )prop.getValue()).getValue());
		} else if (prop.getValue() instanceof IAcmeFloatValue) {
			num = new MyDouble((double )((IAcmeFloatValue )prop.getValue()).getValue());
		} else {  // unhandled type
			Tool.logger().error("Unsupported IAcmeProperty value type! " + prop.getValue());
		}

		return num;
	}

	/**
	 * Default constructor
	 */
	public MyNumber (Number n) {
		m_number = n;
		if (n instanceof Integer || n instanceof Long) {
			m_long = n.longValue();
		} else if (n instanceof Float || n instanceof Double) {
			m_double = n.doubleValue();
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString () {
		String s = "";
		if (m_number instanceof Integer || m_number instanceof Long) {
			s = String.valueOf(m_long);
		} else if (m_number instanceof Float || m_number instanceof Double) {
			s = String.valueOf(m_double);
		}
		return s;
	}

	/* (non-Javadoc)
	 * @see java.lang.Number#intValue()
	 */
	@Override
	public int intValue () {
		return (int )m_long;
	}

	/* (non-Javadoc)
	 * @see java.lang.Number#longValue()
	 */
	@Override
	public long longValue () {
		return m_long;
	}

	/* (non-Javadoc)
	 * @see java.lang.Number#floatValue()
	 */
	@Override
	public float floatValue () {
		return (float )m_double;
	}

	/* (non-Javadoc)
	 * @see java.lang.Number#doubleValue()
	 */
	@Override
	public double doubleValue () {
		return m_double;
	}

	public abstract Number toJavaNumber ();

	public abstract MyNumber plus (MyNumber addend);
	public abstract MyNumber minus (MyNumber subtrahend);
	public abstract MyNumber times (MyNumber multipler);
	public abstract MyNumber dividedBy (MyNumber divisor);
	public abstract MyNumber modulus (MyNumber divisor);

	public MyNumber incr () {
		if (this instanceof MyDouble) {
			++m_double;
		} else {
			++m_long;
		}
		return this;
	}

	public MyNumber decr () {
		if (this instanceof MyDouble) {
			--m_double;
		} else {
			--m_long;
		}
		return this;
	}

	public MyNumber negate () {
		if (this instanceof MyDouble) {
			m_double = -m_double;
		} else {
			m_long = -m_long;
		}
		return this;
	}

	public abstract Boolean eq (MyNumber operand);
	public abstract Boolean lt (MyNumber operand);

	public Boolean ne (MyNumber operand) {
		return ! eq(operand);
	}

	public Boolean le (MyNumber operand) {
		return lt(operand) || eq(operand);
	}

	public Boolean gt (MyNumber operand) {
		return ! le(operand);
	}

	public Boolean ge (MyNumber operand) {
		return gt(operand) || eq(operand);
	}

}
