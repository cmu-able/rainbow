package edu.cmu.cs.able.typelib.txtenc.typelib;

import incubator.pval.Ensure;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.enumeration.EnumerationType;
import edu.cmu.cs.able.typelib.enumeration.EnumerationValue;
import edu.cmu.cs.able.typelib.txtenc.DelegateTextEncoding;
import edu.cmu.cs.able.typelib.txtenc.TextEncoding;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Text encoding that encodes an decodes enumerations.
 */
public class EnumerationDelegateTextEncoding implements DelegateTextEncoding {
	/**
	 * Divider to place at the end of the type name marking its end.
	 */
	private static final String DIVIDER = ";";
	
	/**
	 * Creates a new encoding.
	 */
	public EnumerationDelegateTextEncoding() {
		/*
		 * Nothing to do.
		 */
	}

	@Override
	public boolean supports(DataType t) {
		Ensure.not_null(t);
		return t instanceof EnumerationType;
	}

	@Override
	public void encode(DataValue v, Writer w, TextEncoding enc)
			throws IOException {
		Ensure.not_null(v);
		Ensure.is_instance(v, EnumerationValue.class);
		Ensure.not_null(w);
		Ensure.not_null(enc);
		
		String ename = ((EnumerationValue) v).name();
		Ensure.equals(-1, ename.indexOf(DIVIDER));
		
		w.write(ename);
		w.write(DIVIDER);
	}

	@Override
	public DataValue decode(Reader r, DataType type, DataTypeScope dts,
			TextEncoding enc) throws IOException, InvalidEncodingException {
		Ensure.not_null(r);
		Ensure.not_null(type);
		Ensure.is_instance(type, EnumerationType.class);
		Ensure.not_null(dts);
		Ensure.not_null(enc);
		
		EnumerationType etype = (EnumerationType) type;
		
		String name = ReaderUtils.read_until(r, DIVIDER);
		if (!etype.has_value(name)) {
			throw new InvalidEncodingException("Enumeration type '" +
					etype.name() + "' does not contain value '" + name + "'.");
		}
		
		return etype.value(name);
	}
}
