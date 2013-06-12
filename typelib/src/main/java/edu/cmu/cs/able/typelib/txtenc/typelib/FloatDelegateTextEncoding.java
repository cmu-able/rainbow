package edu.cmu.cs.able.typelib.txtenc.typelib;

import incubator.pval.Ensure;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.prim.DoubleType;
import edu.cmu.cs.able.typelib.prim.DoubleValue;
import edu.cmu.cs.able.typelib.prim.FloatType;
import edu.cmu.cs.able.typelib.prim.FloatValue;
import edu.cmu.cs.able.typelib.txtenc.DelegateTextEncoding;
import edu.cmu.cs.able.typelib.txtenc.TextEncoding;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Text encoding that encodes and decodes float and double values.
 */
public class FloatDelegateTextEncoding implements DelegateTextEncoding {
	/**
	 * Divider to mark the end of the integer.
	 */
	private static final String DIVIDER = ";";
	
	/**
	 * Creates a new encoding.
	 */
	public FloatDelegateTextEncoding() {
	}

	@Override
	public boolean supports(DataType t) {
		Ensure.not_null(t);
		
		if (t instanceof FloatType || t instanceof DoubleType) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void encode(DataValue v, Writer w, TextEncoding enc)
			throws IOException {
		Ensure.not_null(v);
		Ensure.not_null(w);
		Ensure.not_null(enc);
		
		if (v instanceof FloatValue) {
			w.write(Float.toString(((FloatValue) v).value()));
		} else {
			Ensure.is_instance(v, DoubleValue.class);
			w.write(Double.toString(((DoubleValue) v).value()));
		}
		
		w.write(DIVIDER);
	}

	@Override
	public DataValue decode(Reader r, DataType type, DataTypeScope dts,
			TextEncoding enc) throws IOException, InvalidEncodingException {
		Ensure.not_null(r);
		Ensure.not_null(type);
		Ensure.not_null(dts);
		Ensure.not_null(enc);
		
		String number = ReaderUtils.read_until(r, DIVIDER);
		try {
			if (type instanceof FloatType) {
				return ((FloatType) type).make(Float.parseFloat(number));
			} else {
				Ensure.is_instance(type, DoubleType.class);
				return ((DoubleType) type).make(Double.parseDouble(number));
			}
		} catch (NumberFormatException e) {
			throw new InvalidEncodingException("Invalid number format: '"
					+ number + "' for type '" + type.name() + "'.", e);
		}
	}
}
