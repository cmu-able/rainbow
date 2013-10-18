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
		if (m_long > Integer.MAX_VALUE) {  // convert to long
			return m_long;
		} else {
			return (int )m_long;
		}
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
