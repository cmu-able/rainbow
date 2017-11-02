package edu.cmu.cs.able.typelib.txtenc.typelib;

import incubator.pval.Ensure;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.prim.BooleanType;
import edu.cmu.cs.able.typelib.prim.BooleanValue;
import edu.cmu.cs.able.typelib.txtenc.DelegateTextEncoding;
import edu.cmu.cs.able.typelib.txtenc.TextEncoding;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Text encoding that encodes boolean values.
 */
public class BooleanDelegateTextEncoding implements DelegateTextEncoding {
	/**
	 * The divider to signal the end of the type.
	 */
	private static final String DIVIDER = ";";
	
	/**
	 * How to encode a true value.
	 */
	private static final String TRUE = "true";

	/**
	 * How to encode a false value.
	 */
	private static final String FALSE = "false";
	
	/**
	 * Creates a new encoding.
	 */
	public BooleanDelegateTextEncoding() {
	}

	@Override
	public boolean supports(DataType t) {
		Ensure.not_null(t);
		return t instanceof BooleanType;
	}

	@Override
	public void encode(DataValue v, Writer w, TextEncoding enc)
			throws IOException {
		Ensure.not_null(v);
		Ensure.is_instance(v, BooleanValue.class);
		Ensure.not_null(w);
		Ensure.not_null(enc);
		
		if (((BooleanValue) v).value()) {
			w.write(TRUE);
		} else {
			w.write(FALSE);
		}
		
		w.write(DIVIDER);
	}

	@Override
	public DataValue decode(Reader r, DataType type, DataTypeScope dts,
			TextEncoding enc) throws IOException, InvalidEncodingException {
		Ensure.not_null(r);
		Ensure.not_null(type);
		Ensure.is_instance(type, BooleanType.class);
		Ensure.not_null(dts);
		Ensure.not_null(enc);
		
		String v = ReaderUtils.read_until(r, DIVIDER);
		if (v.equals(TRUE)) {
			return ((BooleanType) type).make(true);
		} else if (v.equals(FALSE)) {
			return ((BooleanType) type).make(false);
		} else {
			throw new InvalidEncodingException("Invalid boolean encoding: '"
					+ v + "'.");
		}
	}
}
