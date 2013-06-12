package edu.cmu.cs.able.typelib.txtenc.typelib;

import incubator.pval.Ensure;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.prim.Int16Type;
import edu.cmu.cs.able.typelib.prim.Int16Value;
import edu.cmu.cs.able.typelib.prim.Int32Type;
import edu.cmu.cs.able.typelib.prim.Int32Value;
import edu.cmu.cs.able.typelib.prim.Int64Type;
import edu.cmu.cs.able.typelib.prim.Int64Value;
import edu.cmu.cs.able.typelib.prim.Int8Type;
import edu.cmu.cs.able.typelib.prim.Int8Value;
import edu.cmu.cs.able.typelib.txtenc.DelegateTextEncoding;
import edu.cmu.cs.able.typelib.txtenc.TextEncoding;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Text encoding that encodes integer values.
 */
public class IntegerDelegateTextEncoding implements DelegateTextEncoding {
	/**
	 * Divider to mark the end of the integer.
	 */
	private static final String DIVIDER = ";";
	
	/**
	 * Creates a new encoding.
	 */
	public IntegerDelegateTextEncoding() {
	}

	@Override
	public boolean supports(DataType t) {
		Ensure.not_null(t);
		
		if (t instanceof Int8Type || t instanceof Int16Type
				|| t instanceof Int32Type || t instanceof Int64Type) {
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
		
		if (v instanceof Int8Value) {
			w.write(Byte.toString(((Int8Value) v).value()));
		} else if (v instanceof Int16Value) {
			w.write(Short.toString(((Int16Value) v).value()));
		} else if (v instanceof Int32Value) {
			w.write(Integer.toString(((Int32Value) v).value()));
		} else {
			Ensure.is_instance(v, Int64Value.class);
			w.write(Long.toString(((Int64Value) v).value()));
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
			if (type instanceof Int8Type) {
				return ((Int8Type) type).make(Byte.parseByte(number));
			} else if (type instanceof Int16Type) {
				return ((Int16Type) type).make(Short.parseShort(number));
			} else if (type instanceof Int32Type) {
				return ((Int32Type) type).make(Integer.parseInt(number));
			} else {
				Ensure.is_instance(type, Int64Type.class);
				return ((Int64Type) type).make(Long.parseLong(number));
			}
		} catch (NumberFormatException e) {
			throw new InvalidEncodingException("Invalid number format: '"
					+ number + "' for type '" + type.name() + "'.", e);
		}
	}
}
