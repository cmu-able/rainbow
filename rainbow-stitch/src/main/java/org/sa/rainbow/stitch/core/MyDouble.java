/**
 * Created April 24, 2006.
 */
package org.sa.rainbow.stitch.core;

/**
 * Custom implementation of Double to support polymorphic arithmetic operation.
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class MyDouble extends MyNumber {
	private static final long serialVersionUID = -2470085392998295121L;

	/**
	 * @param n
	 */
	public MyDouble(Double n) {
		super(n);
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.MyNumber#toJavaNumber()
	 */
	@Override
	public Number toJavaNumber() {
		return m_double;
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.MyNumber#plus(org.sa.rainbow.stitch.core.MyNumber)
	 */
	@Override
	public MyNumber plus(MyNumber addend) {
		MyNumber result = new MyDouble(m_double);
		if (addend instanceof MyInteger) {
			result.m_double += addend.longValue();
		} else if (addend instanceof MyDouble) {
			result.m_double += addend.doubleValue();
		} // can't be any other subtype
		return result;
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.MyNumber#minus(org.sa.rainbow.stitch.core.MyNumber)
	 */
	@Override
	public MyNumber minus(MyNumber subtrahend) {
		MyNumber result = new MyDouble(m_double);
		if (subtrahend instanceof MyInteger) {
			result.m_double -= subtrahend.longValue();
		} else if (subtrahend instanceof MyDouble) {
			result.m_double -= subtrahend.doubleValue();
		} // can't be any other subtype
		return result;
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.MyNumber#times(org.sa.rainbow.stitch.core.MyNumber)
	 */
	@Override
	public MyNumber times(MyNumber multipler) {
		MyNumber result = new MyDouble(m_double);
		if (multipler instanceof MyInteger) {
			result.m_double *= multipler.longValue();
		} else if (multipler instanceof MyDouble) {
			result.m_double *= multipler.doubleValue();
		} // can't be any other subtype
		return result;
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.MyNumber#dividedBy(org.sa.rainbow.stitch.core.MyNumber)
	 */
	@Override
	public MyNumber dividedBy(MyNumber divisor) {
		MyNumber result = new MyDouble(m_double);
		if (divisor instanceof MyInteger) {
			result.m_double /= divisor.longValue();
		} else if (divisor instanceof MyDouble) {
			result.m_double /= divisor.doubleValue();
		} // can't be any other subtype
		return result;
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.MyNumber#modulus(org.sa.rainbow.stitch.core.MyNumber)
	 */
	@Override
	public MyNumber modulus(MyNumber divisor) {
		MyNumber result = null;
		if (divisor instanceof MyInteger) {
			result = new MyInteger((long )(m_double % divisor.longValue()));
		} else if (divisor instanceof MyDouble) {
			result = new MyInteger((long )(m_double % divisor.doubleValue()));
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
			result = new Boolean(m_double == operand.longValue());
		} else if (operand instanceof MyDouble) {
			result = new Boolean(m_double == operand.doubleValue());
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
			result = new Boolean(m_double < operand.longValue());
		} else if (operand instanceof MyDouble) {
			result = new Boolean(m_double < operand.doubleValue());
		}
		return result;
	}

	public MyInteger toInteger() {
		MyInteger newInteger = new MyInteger((int )m_double);
		return newInteger;
	}

}
