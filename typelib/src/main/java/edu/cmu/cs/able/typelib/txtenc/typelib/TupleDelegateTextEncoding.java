package edu.cmu.cs.able.typelib.txtenc.typelib;

import incubator.pval.Ensure;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.cs.able.typelib.comp.TupleDataType;
import edu.cmu.cs.able.typelib.comp.TupleDataValue;
import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.txtenc.DelegateTextEncoding;
import edu.cmu.cs.able.typelib.txtenc.TextEncoding;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Encoding that can encode and decode tuples ({@link TupleDataType}).
 */
public class TupleDelegateTextEncoding implements DelegateTextEncoding {
	/**
	 * Creates a new encoding.
	 */
	public TupleDelegateTextEncoding() {
		/*
		 * Nothing to do.
		 */
	}

	@Override
	public boolean supports(DataType t) {
		Ensure.not_null(t);
		return t instanceof TupleDataType;
	}

	@Override
	public void encode(DataValue v, Writer w, TextEncoding enc)
			throws IOException {
		Ensure.not_null(v);
		Ensure.is_instance(v, TupleDataValue.class);
		Ensure.not_null(w);
		Ensure.not_null(enc);
		
		List<DataValue> data = ((TupleDataValue) v).data();
		for (DataValue dv : data) {
			enc.encode(dv, w);
		}
	}

	@Override
	public DataValue decode(Reader r, DataType type, DataTypeScope dts,
			TextEncoding enc) throws IOException, InvalidEncodingException {
		Ensure.not_null(r);
		Ensure.not_null(type);
		Ensure.is_instance(type, TupleDataType.class);
		Ensure.not_null(dts);
		Ensure.not_null(enc);
		
		List<DataType> inner = ((TupleDataType) type).inner_types();
		List<DataValue> values = new ArrayList<>();
		for (DataType it : inner) {
			DataValue iv = enc.decode(r, dts);
			if (!it.is_instance(iv)) {
				throw new InvalidEncodingException("Expected value of type '"
						+ it.name() + "' but found value of type '"
						+ iv.type().name() + "' instead.");
			}
			
			values.add(iv);
		}
		
		return ((TupleDataType) type).make(values);
	}
}
