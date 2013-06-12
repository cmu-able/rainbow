package edu.cmu.cs.able.typelib.txtenc.typelib;

import incubator.pval.Ensure;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import edu.cmu.cs.able.typelib.comp.OptionalDataType;
import edu.cmu.cs.able.typelib.comp.OptionalDataValue;
import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.txtenc.DelegateTextEncoding;
import edu.cmu.cs.able.typelib.txtenc.TextEncoding;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Text encoding that encodes optional data types ({@link OptionalDataType}).
 */
public class OptionalDelegateTextEncoding implements DelegateTextEncoding {
	/**
	 * Character written to mark there is a value.
	 */
	private static final char HAS_VALUE = 'v';
	
	/**
	 * Character written to mark the optional value is null.
	 */
	private static final char IS_NULL = 'n';
	
	/**
	 * Creates a new encoding.
	 */
	public OptionalDelegateTextEncoding() {
		/*
		 * Nothing to do.
		 */
	}

	@Override
	public boolean supports(DataType t) {
		Ensure.not_null(t);
		return t instanceof OptionalDataType;
	}

	@Override
	public void encode(DataValue v, Writer w, TextEncoding enc)
			throws IOException {
		Ensure.not_null(v);
		Ensure.is_instance(v, OptionalDataValue.class);
		Ensure.not_null(w);
		Ensure.not_null(enc);
		
		OptionalDataValue ov = (OptionalDataValue) v;
		if (ov.value() != null) {
			w.write(HAS_VALUE);
			enc.encode(ov.value(), w);
		} else {
			w.write(IS_NULL);
		}
	}

	@Override
	public DataValue decode(Reader r, DataType type, DataTypeScope dts,
			TextEncoding enc) throws IOException, InvalidEncodingException {
		Ensure.not_null(r);
		Ensure.not_null(type);
		Ensure.is_instance(type, OptionalDataType.class);
		Ensure.not_null(dts);
		Ensure.not_null(enc);
		
		int cr = r.read();
		if (cr == -1) {
			throw new EOFException();
		}
		
		if (cr == HAS_VALUE) {
			DataValue v = enc.decode(r, dts);
			return ((OptionalDataType) type).make(v);
		} else if (cr == IS_NULL) {
			return ((OptionalDataType) type).make(null);
		} else {
			throw new InvalidEncodingException("Invalid optional contents "
					+ "marker: " + cr + ".");
		}
	}

}
