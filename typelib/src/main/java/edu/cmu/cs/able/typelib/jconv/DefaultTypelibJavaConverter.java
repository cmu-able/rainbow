package edu.cmu.cs.able.typelib.jconv;

import incubator.pval.Ensure;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;


/**
 * Typelib/Java value converter that automatically adds all default rules
 * at creation.
 */
public class DefaultTypelibJavaConverter extends TypelibJavaConverter {
	/**
	 * Creates a new converter.
	 */
	private DefaultTypelibJavaConverter() {
	}
	
	/**
	 * Creates a new converter.
	 * @param pscope the primitive scope to draw primitive types from
	 * @return the converter
	 */
	public static DefaultTypelibJavaConverter make(PrimitiveScope pscope) {
		Ensure.not_null(pscope);
		
		DefaultTypelibJavaConverter c = new DefaultTypelibJavaConverter();
		c.add(new PrimitiveValuesConversionRule(pscope));
		c.add(new OptionaValuesConvertionRule());
		c.add(new SetConversionRule());
		c.add(new ListConversionRule());
		c.add(new MapConversionRule());
		return c;
	}
}
