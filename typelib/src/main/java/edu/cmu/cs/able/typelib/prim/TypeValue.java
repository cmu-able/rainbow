package edu.cmu.cs.able.typelib.prim;

import edu.cmu.cs.able.typelib.type.DataType;

/**
 * Value of a type data type (value that contains a reference to a type).
 */
public class TypeValue extends JavaObjectDataValue<DataType> {
	/**
	 * Creates a new value. 
	 * @param rtype the type referred to
	 * @param type this value's type
	 */
	protected TypeValue(DataType rtype, TypeType type) {
		super(rtype, type);
	}
	
	@Override
	public String toString() {
		return type().name() + "[" + value().absolute_hname() + "]";
	}
	
	@Override
	public TypeValue clone() throws CloneNotSupportedException {
		return new TypeValue(value(), (TypeType) type());
	}
}
