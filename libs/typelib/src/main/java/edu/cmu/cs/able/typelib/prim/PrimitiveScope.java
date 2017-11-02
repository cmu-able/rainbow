package edu.cmu.cs.able.typelib.prim;

import edu.cmu.cs.able.typelib.type.DataTypeScope;

/**
 * <p>Scope that contains all primitive data types. The primitive scope is
 * initialized with all data types and has methods to provide access to them.
 * More data types can be freely added to the primitive scope as long as none
 * of them conflicts with the primitive types.</p>
 * <p>In general, there will only be one primitive type scope which is the
 * root of the data type scope hierarchy. This is not mandatory through. In
 * unit tests, for example, it may be useful to use other configurations.</p>
 */
public class PrimitiveScope extends DataTypeScope {
	/**
	 * The any data type.
	 */
	private AnyType m_any;
	
	/**
	 * The boolean data type.
	 */
	private BooleanType m_bool;
	
	/**
	 * The int8 data type.
	 */
	private Int8Type m_int8;
	
	/**
	 * The int16 data type.
	 */
	private Int16Type m_int16;
	
	/**
	 * The int32 data type.
	 */
	private Int32Type m_int32;
	
	/**
	 * The int64 data type.
	 */
	private Int64Type m_int64;
	
	/**
	 * The float data type.
	 */
	private FloatType m_float;
	
	/**
	 * The double data type.
	 */
	private DoubleType m_double;
	
	/**
	 * The string data type.
	 */
	private StringType m_string;
	
	/**
	 * The Ascii data type.
	 */
	private AsciiType m_ascii;
	
	/**
	 * The time type.
	 */
	private TimeType m_time;
	
	/**
	 * The period type.
	 */
	private PeriodType m_period;
	
	/**
	 * The type type.
	 */
	private TypeType m_type;
	
	/**
	 * Creates a new, empty primitive scope.
	 */
	public PrimitiveScope() {
		super();
		
		add(m_any = new AnyType());
		add(m_bool = new BooleanType(m_any));
		add(m_int8 = new Int8Type(m_any));
		add(m_int16 = new Int16Type(m_any));
		add(m_int32 = new Int32Type(m_any));
		add(m_int64 = new Int64Type(m_any));
		add(m_float = new FloatType(m_any));
		add(m_double = new DoubleType(m_any));
		add(m_string = new StringType(m_any));
		add(m_ascii = new AsciiType(m_any));
		add(m_time = new TimeType(m_any));
		add(m_period = new PeriodType(m_any));
		add(m_type = new TypeType(m_any));
	}
	
	/**
	 * Obtains the <code>any</code> data type.
	 * @return the data type
	 */
	public synchronized AnyType any() {
		return m_any;
	}
	
	/**
	 * Obtains the <code>bool</code> data type.
	 * @return the data type
	 */
	public synchronized BooleanType bool() {
		return m_bool;
	}
	
	/**
	 * Obtains the <code>int8</code> data type.
	 * @return the data type
	 */
	public synchronized Int8Type int8() {
		return m_int8;
	}
	
	/**
	 * Obtains the <code>int16</code> data type.
	 * @return the data type
	 */
	public synchronized Int16Type int16() {
		return m_int16;
	}
	
	/**
	 * Obtains the <code>int32</code> data type.
	 * @return the data type
	 */
	public synchronized Int32Type int32() {
		return m_int32;
	}
	
	/**
	 * Obtains the <code>int64</code> data type.
	 * @return the data type
	 */
	public synchronized Int64Type int64() {
		return m_int64;
	}
	
	/**
	 * Obtains the <code>float</code> data type.
	 * @return the data type
	 */
	public synchronized FloatType float_type() {
		return m_float;
	}
	
	/**
	 * Obtains the <code>double</code> data type.
	 * @return the data type
	 */
	public synchronized DoubleType double_type() {
		return m_double;
	}
	
	/**
	 * Obtains the <code>string</code> data type.
	 * @return the data type
	 */
	public synchronized StringType string() {
		return m_string;
	}
	
	/**
	 * Obtains the <code>ascii</code> data type.
	 * @return the data type
	 */
	public synchronized AsciiType ascii() {
		return m_ascii;
	}
	
	/**
	 * Obtains the <code>time</code> data type.
	 * @return the data type
	 */
	public synchronized TimeType time() {
		return m_time;
	}
	
	/**
	 * Obtains the <code>period</code> data type.
	 * @return the data type
	 */
	public synchronized PeriodType period() {
		return m_period;
	}
	
	/**
	 * Obtains the <code>type</code> data type.
	 * @return the data type
	 */
	public synchronized TypeType type() {
		return m_type;
	}
}
