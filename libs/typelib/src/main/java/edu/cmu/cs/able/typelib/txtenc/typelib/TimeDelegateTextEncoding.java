package edu.cmu.cs.able.typelib.txtenc.typelib;

import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.prim.PeriodType;
import edu.cmu.cs.able.typelib.prim.PeriodValue;
import edu.cmu.cs.able.typelib.prim.TimeType;
import edu.cmu.cs.able.typelib.prim.TimeValue;
import edu.cmu.cs.able.typelib.txtenc.DelegateTextEncoding;
import edu.cmu.cs.able.typelib.txtenc.TextEncoding;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;
import edu.cmu.cs.able.typelib.type.DataValue;
import incubator.pval.Ensure;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * Text encoding that encodes integer values.
 */
public class TimeDelegateTextEncoding implements DelegateTextEncoding {
	/**
	 * Divider to mark the end of the integer.
	 */
	private static final String DIVIDER = ";";
	
	/**
	 * Creates a new encoding.
	 */
	public TimeDelegateTextEncoding() {
	}

	@Override
	public boolean supports(DataType t) {
		Ensure.not_null(t);

		return t instanceof TimeType || t instanceof PeriodType;
	}

	@Override
	public void encode(DataValue v, Writer w, TextEncoding enc)
			throws IOException {
		Ensure.not_null(v);
		Ensure.not_null(w);
		Ensure.not_null(enc);
		
		if (v instanceof TimeValue) {
			w.write(Long.toString(((TimeValue) v).value()));
		} else {
			Ensure.is_instance(v, PeriodValue.class);
			w.write(Long.toString(((PeriodValue) v).value()));
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
			if (type instanceof TimeType) {
				return ((TimeType) type).make(Long.parseLong(number));
			} else {
				Ensure.is_instance(type, PeriodType.class);
				return ((PeriodType) type).make(Long.parseLong(number));
			}
		} catch (NumberFormatException e) {
			throw new InvalidEncodingException("Invalid number format: '"
					+ number + "' for type '" + type.name() + "'.", e);
		}
	}
}
