package edu.cmu.cs.able.typelib.txtenc.typelib;

import edu.cmu.cs.able.typelib.AsciiEncoding;
import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.prim.AsciiType;
import edu.cmu.cs.able.typelib.prim.AsciiValue;
import edu.cmu.cs.able.typelib.prim.StringType;
import edu.cmu.cs.able.typelib.prim.StringValue;
import edu.cmu.cs.able.typelib.txtenc.DelegateTextEncoding;
import edu.cmu.cs.able.typelib.txtenc.TextEncoding;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;
import edu.cmu.cs.able.typelib.type.DataValue;
import incubator.pval.Ensure;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Text encoding that encodes and decodes strings (including ASCII strings).
 */
public class StringDelegateTextEncoding implements DelegateTextEncoding {
	/**
	 * Divider to place at the end of the string marking the end of the string.
	 */
	private static final String DIVIDER = ";";
	
	/**
	 * Creates a new encoding.
	 */
	public StringDelegateTextEncoding() {
	}

	@Override
	public boolean supports(DataType t) {
		Ensure.not_null(t);

		return t instanceof AsciiType || t instanceof StringType;
	}

	@Override
	public void encode(DataValue v, Writer w, TextEncoding enc)
			throws IOException {
		Ensure.not_null(v);
		Ensure.not_null(w);
		Ensure.not_null(enc);
		
		if (v instanceof AsciiValue) {
			w.write(URLEncoder.encode(((AsciiValue) v).value(), "UTF-8"));
		} else {
			Ensure.is_instance(v, StringValue.class);
			w.write(URLEncoder.encode(((StringValue) v).value(), "UTF-8"));
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
		
		String str = ReaderUtils.read_until(r, DIVIDER);
		try {
			str = URLDecoder.decode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			/*
			 * UTF-8 is always supported.
			 */
			Ensure.unreachable();
		} catch (IllegalArgumentException e) {
			throw new InvalidEncodingException("Invalid encoded string '"
					+ str + "'.", e);
		}
		
		if (type instanceof AsciiType) {
			if (!AsciiEncoding.is_ascii(str)) {
				throw new InvalidEncodingException("Ascii string has non-ascii "
						+ "characters.");
			}
			
			return ((AsciiType) type).make(str);
		} else {
			Ensure.is_instance(type, StringType.class);
			return ((StringType) type).make(str);
		}
	}
}
